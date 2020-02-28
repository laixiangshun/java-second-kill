package com.java.secondskill.servers;

import com.java.secondskill.beans.OrderInfo;
import com.java.secondskill.beans.SeckillOrder;
import com.java.secondskill.beans.User;
import com.java.secondskill.redis.SeckillKey;
import com.java.secondskill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SeckillService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 获取秒杀结果
     */
    public long getSeckillResult(Long userId, Long goodsId) {
        SeckillOrder order = orderService.getOrderByUserIdGoodsId(userId, goodsId);
        if (Objects.nonNull(order)) {
            return order.getOrderId();
        }
        boolean over = getGoodsOver(goodsId);
        if (over) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * 判断秒杀失败商品是否存在
     */
    private boolean getGoodsOver(Long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver, "" + goodsId);
    }

    /**
     * 保证这三个操作，减库存 下订单 写入秒杀订单是一个事物
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo seckill(User user, GoodsVo goodsVo) {
        boolean reduceStock = goodsService.reduceStock(goodsVo);
        if (reduceStock) {
            return orderService.createOrder(user, goodsVo);
        } else {
            setGoodsOver(goodsVo.getId());
            return null;
        }
    }

    /**
     * redis中记录秒杀失败的商品
     */
    private boolean setGoodsOver(Long goodsId) {
        return redisService.set(SeckillKey.isGoodsOver, "" + goodsId, true);
    }
}
