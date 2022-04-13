package com.seckill.mapper;

import com.seckill.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.vo.GoodsVo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-02-21
 */
@Repository
public interface GoodsMapper extends BaseMapper<Goods> {
    /**
     * 功能描述: 获取商品列表
     */
    List<GoodsVo> findGoodsVo();

    /**
     * 功能描述: 获取商品详情
     */
    GoodsVo findGoodsVoByGoodsId(Long goodsId);
}
