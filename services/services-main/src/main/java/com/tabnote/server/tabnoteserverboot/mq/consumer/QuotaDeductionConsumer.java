package com.tabnote.server.tabnoteserverboot.mq.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import static com.tabnote.server.tabnoteserverboot.define.MQName.*;

@Component
public class QuotaDeductionConsumer {

    private static final Logger log = LoggerFactory.getLogger(QuotaDeductionConsumer.class);

    private AiServiceInterface aiService;

    @Autowired
    public void setAiService(AiServiceInterface aiService) {
        this.aiService = aiService;
    }

    @RabbitListener(queues = {QUEUE_NAME})
    public void processMessages(String data, Message message, Channel channel) {
        log.info("接收到消息: " + data);
        action(data, message, channel);
    }

    @RabbitListener(queues = {QUEUE_BACKUP})
    public void processBackUpMessages(String data, Message message, Channel channel) {
        log.info("备份队列接收到消息: " + data);
        action(data, message, channel);
    }

    @RabbitListener(queues = {QUEUE_DEAD})
    public void processDeadMessages(String data, Message message, Channel channel) {
        log.info("死信队列接收到消息: " + data);
        deadQueue(data, message, channel);
    }


    public void action(String data, Message message, Channel channel) {
        //获取当前信息的tag
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //获取当前消息是否为重复投递过的
        boolean redelivered = message.getMessageProperties().getRedelivered();

        try {
            JSONObject json = JSONObject.parseObject(data);
            aiService.useQuota(json.getInteger("quota"), json.getString("user_id"), json.getString("idempotence_id"));
            channel.basicAck(deliveryTag, true);
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage());
            log.error("主键重复");
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                if (redelivered) {
                    //最后一个参数取值决定是否重新投递这个消息
//                    channel.basicNack(deliveryTag, false, false);
                    //重新投递到死信队列
//                    quotaDeductionPublisher.publishToDeadQueue(data);
                    channel.basicReject(deliveryTag, false);

                } else {
                    channel.basicNack(deliveryTag, false, true);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void deadQueue(String data, Message message, Channel channel) {
        //获取当前信息的tag
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            JSONObject json = JSONObject.parseObject(data);
            aiService.useQuota(json.getInteger("quota"), json.getString("user_id"), json.getString("idempotence_id"));
            channel.basicAck(deliveryTag, true);
        } catch (DuplicateKeyException e) {
            log.error(e.getMessage());
            log.error("主键重复");
        } catch (Exception e) {
            log.error(e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
