package com.seckill.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import com.google.gson.Gson;
import com.seckill.interceptor.ApiLimit;
import com.seckill.config.UserContext;
import com.seckill.entity.SeckillMessage;
import com.seckill.entity.User;
import com.seckill.feign.GoodsClient;
import com.seckill.rabbitmq.MQSender;
import com.seckill.service.OrderService;
import com.seckill.service.SeckillOrderService;
import com.seckill.utils.JsonUtil;
import com.seckill.utils.RedisUtil;
import com.seckill.vo.GoodsVo;
import comment.RespBean;
import comment.RespBeanEnum;
import comment.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@Slf4j
@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MQSender mqSender;

    //布隆过滤器
    private BloomFilter<Long> bloomFilter = BloomFilter.create(Funnels.longFunnel(),10);

    private Map<Long, Boolean> EmptyStockMap = new HashMap<>();

    @Autowired
    private RedisScript script;

    /** 进行秒杀购买商品
     * 吞吐量：lua脚本后 639.7/sec
     * @param goodsId
     * @return
     */

    @ApiLimit
    @PostMapping("/{path}/doSeckill2")
    @ResponseBody
    public RespBean doSeckill2(@PathVariable String path, Long goodsId) {
        User user = UserContext.getUser();
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        //布隆过滤器消除访问不在商品id的请求
        if(!bloomFilter.mightContain(goodsId)){
            return RespBean.error(RespBeanEnum.GOOD_NOT_EXIST);
        }
        //内存中的map进行标记秒杀商品已经售空
        //内存标记,减少Redis访问
        if (EmptyStockMap.get(goodsId)) {
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //检验秒杀路径
        boolean check = orderService.checkPath(user,goodsId,path);
        if (!check) {
            return RespBean.error(RespBeanEnum.REQUEST_ILLEGAL);
        }


        //判断是否重复抢购
        String seckillOrderJson = (String) valueOperations.get("order:" +
                user.getId() + ":" + goodsId);
        if (!StringUtils.isEmpty(seckillOrderJson)) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //分布式锁防止同个用户同时请求多次
        Boolean ifAbsent = valueOperations.setIfAbsent("order:" + user.getId() + ":" + goodsId, "1",
                1,TimeUnit.SECONDS);
        if (!ifAbsent) {
            return RespBean.error(RespBeanEnum.REPEATE_ERROR);
        }
        //预减库存
        //使用redis+lua脚本保证操作的原子性 预减库存
        Long stock = (Long) redisTemplate.execute
                (script, Collections.singletonList("seckillGoods:" + goodsId), Collections.EMPTY_LIST);
        if (stock < 0) {
            //内存中的map进行标记秒杀商品已经售空
            EmptyStockMap.put(goodsId,true);
            redisTemplate.delete("order:" + user.getId() + ":" + goodsId);
            valueOperations.set("seckillGoods:" + goodsId, 0);
            return RespBean.error(RespBeanEnum.EMPTY_STOCK);
        }
        // 请求入队，立即返回排队中
        SeckillMessage message = new SeckillMessage(user, goodsId);
        mqSender.sendSecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }




    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsClient.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for(GoodsVo goodsVo : list) {
//        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(),
                    goodsVo.getStockCount());
            bloomFilter.put(goodsVo.getId());
            //内存中的map进行标记秒杀商品未售空
            EmptyStockMap.put(goodsVo.getId(), false);
        }
//        });
    }

    /**
     * 获取秒杀结果
     *
     * @param goodsId
     * @return orderId:成功，-1：秒杀失败，0：排队中
     */
    @GetMapping( "/result")
    @ResponseBody
    public RespBean getResult(HttpServletRequest request,Long goodsId) {
        String userJson = request.getHeader("user");
        User user = new Gson().fromJson(userJson, User.class);
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
      * 获取秒杀地址
      *
      * @param goodsId
      * @return
      */
    @GetMapping("/path")
    @ResponseBody
    public RespBean getPath(HttpServletRequest request, Long goodsId,String captcha) {
        String userJson = request.getHeader("user");
        User user = new Gson().fromJson(userJson, User.class);
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        boolean check = orderService.checkCaptcha(user, goodsId, captcha);
        if (!check){
            return RespBean.error(RespBeanEnum.ERROR_CAPTCHA);
        }
        String str = orderService.createPath(user, goodsId);
        return RespBean.success(str);
    }

    @GetMapping("/captcha")
    public void verifyCode(HttpServletRequest request, Long goodsId, HttpServletResponse response) {
        String userJson = request.getHeader("user");
        User user = new Gson().fromJson(userJson, User.class);
        if (null == user || goodsId < 0){
            throw new GlobalException(RespBeanEnum.REQUEST_ILLEGAL);
        }
        // 设置请求头为输出图片类型
        response.setContentType("image/jpg");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        //生成验证码，将结果放入redis
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(130, 32,4, 30);
        redisTemplate.opsForValue().set("captcha:"+user.getId()+":"+goodsId,lineCaptcha.getCode(),300, TimeUnit.SECONDS);
        try {
            BufferedImage image = lineCaptcha.getImage();
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            outputStream.close();
        } catch (IOException e) {
            log.error("验证码生成失败",e.getMessage());
        }
    }
}
