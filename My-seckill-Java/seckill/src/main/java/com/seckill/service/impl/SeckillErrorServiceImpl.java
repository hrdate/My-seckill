package com.seckill.service.impl;

import com.seckill.entity.SeckillError;
import com.seckill.entity.SeckillMessage;
import com.seckill.entity.User;
import com.seckill.mapper.SeckillErrorMapper;
import com.seckill.rabbitmq.MQSender;
import com.seckill.service.SeckillErrorService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.seckill.service.UserService;
import com.seckill.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ……hrdate……
 * @since 2022-03-10
 */
@Service
public class SeckillErrorServiceImpl extends ServiceImpl<SeckillErrorMapper, SeckillError> implements SeckillErrorService {
    @Autowired
    SeckillErrorService seckillErrorService;
    @Autowired
    UserService userService;
    @Autowired
    private MQSender mqSender;

    // cron [秒] [分] [小时] [日] [月] [周] [年]
    @Scheduled(cron = "0 */1 * * * ?",zone = "Asia/Shanghai")
    public void SeckillErrorCornd() {
        int count = seckillErrorService.count();
        if(count > 0) {
            List<SeckillError> seckillErrorList = seckillErrorService.list();
            for (int i = 0; i < seckillErrorList.size(); i++) {
                SeckillError seckillError = seckillErrorList.get(i);
                User user = userService.getById(seckillError.getUserId());
                Long goodsId  = seckillError.getGoodsId();
                // 请求入队，立即返回排队中
                SeckillMessage message = new SeckillMessage(user, goodsId);
                mqSender.sendsecKillMessage(JsonUtil.object2JsonStr(message));
                // 重新提交后，删除数据库中原下单错误的信息
                seckillErrorService.removeById(seckillError.getId());
            }
        }
    }
}
