package com.java.secondskill.rabbitmq;

import com.java.secondskill.beans.SeckillOrder;
import com.java.secondskill.beans.User;
import com.java.secondskill.servers.GoodsService;
import com.java.secondskill.servers.OrderService;
import com.java.secondskill.servers.SeckillService;
import com.java.secondskill.utils.StringUtils;
import com.java.secondskill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * RabbitMQ 接收消息
 */
@Component
public class MqReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MqReceiver.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private GoodsService goodsService;

    @RabbitListener(queues = {MqConfig.TOPIC_QUEUE1})
    public void receiveTopic1(String message) {
        logger.info("topic queue1 receive message :" + message);
    }

    @RabbitListener(queues = {MqConfig.TOPIC_QUEUE2})
    public void receiveTopic2(String message) {
        logger.info("topic queue2 receive message:" + message);
    }

    @RabbitListener(queues = {MqConfig.QUEUE})
    public void receive(String message) {
        SeckillMessage seckillMessage = StringUtils.stringToBean(message, SeckillMessage.class);
        User user = seckillMessage.getUser();
        long goodsId = seckillMessage.getGoodsId();

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stockCount = goods.getStockCount();
        if (stockCount <= 0) {
            return;
        }
        //判断重复秒杀
        SeckillOrder order = orderService.getOrderByUserIdGoodsId(user.getId(), goodsId);
        if (Objects.nonNull(order)) {
            return;
        }
        //减库存
        seckillService.seckill(user, goods);
    }
}
