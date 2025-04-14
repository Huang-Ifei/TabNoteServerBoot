package com.tabnote.server.tabnoteserverboot.mq.publisher;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.MQMessages;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.tabnote.server.tabnoteserverboot.define.MQName.EXCHANGE_DIRECT;
import static com.tabnote.server.tabnoteserverboot.define.MQName.ROUTING_KEY;

@Component
public class QuotaDeductionPublisher {

    private RabbitTemplate rabbitTemplate;

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private MQMessages mqMessages;

    @Autowired
    public void setMQMessages(MQMessages mqMessages) {
        this.mqMessages = mqMessages;
    }

    public void publish(String message) {
        System.out.println("向RabbitMQ发送信息："+message);
        UUID uuid = UUID.randomUUID();
        mqMessages.addMessage(uuid.toString(),message,EXCHANGE_DIRECT);
        rabbitTemplate.convertAndSend(EXCHANGE_DIRECT, ROUTING_KEY, message,new CorrelationData(uuid.toString()));
    }

    public void quotaCost(String id,int cost){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("quota",cost);
        jsonObject.put("user_id",id);
        jsonObject.put("timestamp",System.currentTimeMillis());
        this.publish(jsonObject.toString());
    }
}
