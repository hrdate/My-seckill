package com.goodservice.controller;


import com.goodservice.entity.User;
import com.goodservice.service.GoodsService;
import com.goodservice.vo.DetailVo;
import com.goodservice.vo.GoodsVo;
import com.google.gson.Gson;
import comment.RespBean;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {


    @Autowired
    private GoodsService goodsService;
    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/toList")
    public RespBean toList() {
        //Redis中获取页面，如果不为空，直接返回页面

        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        return  RespBean.success(goodsVo);
    }

    /**
     * 功能描述: 跳转商品详情页
     */
    @RequestMapping("/toDetail2/{goodsId}")
    public RespBean toDetail(@PathVariable Long goodsId,HttpServletRequest request) {
        GoodsVo goodsVo = goodsService.findGoodsVoByGoodsId(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        //秒杀状态
        int secKillStatus = 0;
        //秒杀倒计时
        int remainSeconds = 0;
        //秒杀还未开始
        if (nowDate.before(startDate)) {
            remainSeconds = ((int) ((startDate.getTime() - nowDate.getTime()) / 1000));
        } else if (nowDate.after(endDate)) {
            //	秒杀已结束
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            //秒杀中
            secKillStatus = 1;
            remainSeconds = 0;
        }
        DetailVo detailVo = new DetailVo();
        String userJson = request.getHeader("user");
        User user = new Gson().fromJson(userJson, User.class);
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return RespBean.success(detailVo);
    }
    @GetMapping("/feign/toList")
    List<GoodsVo> findGoodsVo(){
        return goodsService.findGoodsVo();
    }

    @GetMapping("/feign/{id}")
    GoodsVo findGoodsVoByGoodsId(@PathVariable("id")long id){
        return goodsService.findGoodsVoByGoodsId(id);
    }

}
