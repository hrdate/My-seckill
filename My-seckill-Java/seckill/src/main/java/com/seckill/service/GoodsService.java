package com.seckill.service;

import com.seckill.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.seckill.vo.GoodsVo;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
public interface GoodsService extends IService<Goods> {

    /**
     * 功能描述: 获取商品列表
     */
    List<GoodsVo> findGoodsVo();


    /**
     * 功能描述: 获取商品详情
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
