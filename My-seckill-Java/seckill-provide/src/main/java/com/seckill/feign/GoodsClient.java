package com.seckill.feign;

import com.seckill.vo.GoodsVo;
import comment.RespBean;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "good-service")
public interface GoodsClient {

    @GetMapping("/goods/feign/toList")
    List<GoodsVo> findGoodsVo();

    @GetMapping("/goods/feign/{id}")
    GoodsVo findGoodsVoByGoodsId(@PathVariable("id")long id);
}
