package com.goodservice.service;

import com.goodservice.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.goodservice.vo.GoodsVo;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-05-05
 */
public interface GoodsService extends IService<Goods> {

    List<GoodsVo> findGoodsVo();



    GoodsVo findGoodsVoByGoodsId(Long goodsId);

}
