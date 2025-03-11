package com.tabnote.server.tabnoteserverboot.mq.consumer;

import com.alibaba.fastjson2.JSONObject;
import com.rabbitmq.client.Channel;
import com.tabnote.server.tabnoteserverboot.services.inteface.AiServiceInterface;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import static com.tabnote.server.tabnoteserverboot.define.MQName.QUEUE_BACKUP;
import static com.tabnote.server.tabnoteserverboot.define.MQName.QUEUE_NAME;

@Component
public class QuotaDeductionConsumer {

    private AiServiceInterface aiService;

    @Autowired
    public void setAiService(AiServiceInterface aiService) {
        this.aiService = aiService;
    }

    @RabbitListener(queues = {QUEUE_NAME})
    public void processMessages(String data, Message message, Channel channel) {
        System.out.println("接收到消息: " + data);
        action(data, message, channel);
    }

    @RabbitListener(queues = {QUEUE_BACKUP})
    public void processBackUpMessages(String data, Message message, Channel channel) {
        System.out.println("备份队列接收到消息: " + data);
        action(data, message, channel);
    }


    public void action(String data, Message message, Channel channel){
        //获取当前信息的tag
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //获取当前消息是否为重复投递过的
        boolean redelivered = message.getMessageProperties().getRedelivered();

        try {
            JSONObject json = JSONObject.parseObject(data);
            aiService.useQuota(json.getInteger("quota"), json.getString("user_id"));
            channel.basicAck(deliveryTag, true);
        }catch (Exception e){
            e.printStackTrace();
            try {
                if (redelivered){
                    //最后一个参数取值决定是否重新投递这个消息
                    channel.basicNack(deliveryTag,false,false);

                    File file = new File("aaa_QuotaCostProblemData");
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(file,true);
                    fileWriter.write(data+"\n");
                    fileWriter.flush();
                    fileWriter.close();
                }else {
                    channel.basicNack(deliveryTag,false,true);
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
