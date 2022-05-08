package com.goodservice.controller;


import com.goodservice.entity.Goods;
import com.goodservice.entity.User;
import com.goodservice.service.GoodsService;
import com.goodservice.vo.DetailVo;
import com.goodservice.vo.GoodsVo;
import com.google.gson.Gson;
import comment.RespBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-05-05
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    private GoodsService goodsService;


    @GetMapping(value = "/toList")
    public RespBean toList() {
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        return RespBean.success(goodsVo);
    }

    @GetMapping("/feign/toList")
    public List<GoodsVo> feinToList() {
        List<GoodsVo> goodsVo = goodsService.findGoodsVo();
        return goodsVo;
    }


    /**
     * 功能描述: 跳转商品详情页
     */
    @GetMapping("/toDetail2/{goodsId}")
    public RespBean toDetail(@PathVariable Long goodsId,HttpServletRequest request, HttpServletResponse response) {
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
        User user = new Gson().fromJson(request.getHeader("user"), User.class);
        detailVo.setUser(user);
        detailVo.setGoodsVo(goodsVo);
        detailVo.setSecKillStatus(secKillStatus);
        detailVo.setRemainSeconds(remainSeconds);
        return RespBean.success(detailVo);
    }

    @GetMapping("/feign/{id}")
    public Goods findGoodsById(long id){
        return goodsService.findGoodsVoByGoodsId(id);
    }



}
