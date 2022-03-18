package com.seckill.controller;


import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.seckill.comment.RespBean;
import com.seckill.comment.RespBeanEnum;
import com.seckill.comment.exception.GlobalException;
import com.seckill.config.AccessLimit;
import com.seckill.config.ApiLimit;
import com.seckill.entity.Order;
import com.seckill.entity.SeckillMessage;
import com.seckill.entity.SeckillOrder;
import com.seckill.entity.User;
import com.seckill.rabbitmq.MQSender;
import com.seckill.service.GoodsService;
import com.seckill.service.OrderService;
import com.seckill.service.SeckillOrderService;
import com.seckill.utils.JsonUtil;
import com.seckill.utils.RedisUtil;
import com.seckill.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
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
    private GoodsService goodsService;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private OrderService orderService;
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
     * @param user
     * @param goodsId
     * @return
     */

    @AccessLimit
    @ApiLimit
    @PostMapping("/{path}/doSeckill2")
    @ResponseBody
    public RespBean doSeckill2(@PathVariable String path, User user, Long goodsId) {
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
        mqSender.sendsecKillMessage(JsonUtil.object2JsonStr(message));
        return RespBean.success(0);
    }

//    /**
//     * 进行秒杀购买商品，已弃用
//     *
//     * @param model
//     * @param user
//     * @param goodsId
//     * @return
//     */
//    @RequestMapping("/doSeckill")
//    public String doSeckill(Model model, User user, Long goodsId) {
//        if (user == null) {
//            log.info("未登录请求秒杀，跳转登录界面");
//            return "login";
//        }
//        model.addAttribute("user", user);
//        GoodsVo goods = goodsService.findGoodsVoByGoodsId(goodsId);
//        //判断库存
//        if (goods.getStockCount() < 1) {
//            model.addAttribute("errmsg", RespBeanEnum.EMPTY_STOCK.getMessage());
//            return "seckillFail";
//        }
//        //判断是否重复抢购
//        SeckillOrder seckillOrder = seckillOrderService.getOne(new
//                LambdaQueryWrapper<SeckillOrder>()
//                .eq(SeckillOrder::getUserId, user.getId()).eq(SeckillOrder::getGoodsId, goodsId));
//        if (seckillOrder != null) {
//            model.addAttribute("errmsg", RespBeanEnum.REPEATE_ERROR.getMessage());
//            return "seckillFail";
//        }
//        Order order = orderService.seckill(user, goods);
//        model.addAttribute("order",order);
//        model.addAttribute("goods",goods);
//        return "orderDetail";
//    }
//


    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.findGoodsVo();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        list.forEach(goodsVo -> {
            redisTemplate.opsForValue().set("seckillGoods:" + goodsVo.getId(),
                    goodsVo.getStockCount());
            bloomFilter.put(goodsVo.getId());
            //内存中的map进行标记秒杀商品未售空
            EmptyStockMap.put(goodsVo.getId(), false);
        });
    }

    /**
     * 获取秒杀结果
     *
     * @param user
     * @param goodsId
     * @return orderId:成功，-1：秒杀失败，0：排队中
     */
    @GetMapping( "/result")
    @ResponseBody
    public RespBean getResult(User user, Long goodsId) {
        if (user == null) {
            return RespBean.error(RespBeanEnum.SESSION_ERROR);
        }
        Long orderId = seckillOrderService.getResult(user, goodsId);
        return RespBean.success(orderId);
    }

    /**
      * 获取秒杀地址
      *
      * @param user
      * @param goodsId
      * @return
      */
    @AccessLimit(second = 5, maxCount = 5, needLogin = true)
    @GetMapping("/path")
    @ResponseBody
    public RespBean getPath(User user, Long goodsId,String captcha) {
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
    public void verifyCode(User user, Long goodsId, HttpServletResponse response) {
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
