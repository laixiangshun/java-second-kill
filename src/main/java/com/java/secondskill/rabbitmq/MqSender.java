package com.java.secondskill.rabbitmq;

import com.java.secondskill.utils.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MqSender {
    @Autowired
    private AmqpTemplate amqpTemplate;

    public void sendSeckillMessage(SeckillMessage message) {
        String msg = StringUtils.beanToString(message);
        amqpTemplate.convertAndSend(MqConfig.QUEUE, msg);
    }
}
