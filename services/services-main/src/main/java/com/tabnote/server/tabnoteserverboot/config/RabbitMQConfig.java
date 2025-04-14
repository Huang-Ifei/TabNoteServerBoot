package com.tabnote.server.tabnoteserverboot.config;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.MQMessages;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

import static com.tabnote.server.tabnoteserverboot.define.MQName.*;
import static com.tabnote.server.tabnoteserverboot.define.MQName.ROUTING_KEY;

@Configuration
public class RabbitMQConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

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

    private AiServiceInterface aiService;

    @Autowired
    public void setAiService(AiServiceInterface aiService) {
        this.aiService = aiService;
    }

    //PostConstruct注解是Java的一个标准注解，当对象创建之后立即执行
    @PostConstruct
    public void initRabbitTemplate() {
        //在RabbitTemplate对象中传入RabbitMQConfig类自身作为ConfirmCallback和ReturnsCallback的对象
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        //这是发送到交换机成功或失败的时候调用这个方法
        System.out.println("发送是否成功: " + ack);
        System.out.println("原因: " + cause);
        String id = correlationData.getId().toString();
        System.out.println("message id：" + id);
        if (ack) {
            //发送成功
            mqMessages.removeMessage(id);
            System.out.println("删除暂存的已发送至交换机信息:" + id);
        } else {
            String ms = mqMessages.getMessage(id);

            if (ms != null && mqMessages.getTryTime(id) < 2) {
                if (mqMessages.getAimExchange(id).equals(EXCHANGE_DIRECT)) {
                    System.out.println("发送到备份交换机");
                    rabbitTemplate.convertAndSend(EXCHANGE_BACKUP, ROUTING_BACKUP, ms, new CorrelationData(id));
                }
            } else if (ms != null && mqMessages.getTryTime(id) >= 2) {
                //备份交换机失败尝试直接写入
                if (mqMessages.getAimExchange(id).equals(EXCHANGE_DIRECT)) {
                    JSONObject json = JSONObject.parseObject(ms);
                    aiService.useQuota(json.getInteger("quota"), json.getString("user_id"));
                    mqMessages.removeMessage(id);
                }
            }
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        //这是发送到队列失败的时候调用的方法
        System.out.println("应答码: " + returnedMessage.getReplyCode());
        System.out.println("描述：" + returnedMessage.getReplyText());
        System.out.println("使用的交换机：" + returnedMessage.getExchange());
        System.out.println("使用的路由键：" + returnedMessage.getRoutingKey());

        String message = new String(returnedMessage.getMessage().getBody());

        //第一次尝试
        if (returnedMessage.getExchange().equals(EXCHANGE_DIRECT)) {
            UUID uuid = UUID.randomUUID();
            //无论是否是备份AIMEXCHANGE都应该是原交换池
            mqMessages.addMessage(uuid.toString(), message, EXCHANGE_DIRECT);
            rabbitTemplate.convertAndSend(EXCHANGE_BACKUP, ROUTING_BACKUP, message, new CorrelationData(uuid.toString()));
        } else if (returnedMessage.getExchange().equals(EXCHANGE_BACKUP)) {
            //如果发送到备份队列依旧失败尝试直接写入
            JSONObject json = JSONObject.parseObject(message);
            aiService.useQuota(json.getInteger("quota"), json.getString("user_id"));
        }
    }
}
