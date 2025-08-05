package com.tabnote.server.tabnoteserverboot.mq.publisher;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.MQMessages;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tabnote.server.tabnoteserverboot.define.MQName.*;

@Component
public class QuotaDeductionPublisher {

    private static final Logger log = LoggerFactory.getLogger(QuotaDeductionPublisher.class);

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

    private StringRedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private VipMapper vipMapper;

    @Autowired
    public void setVipMapper(VipMapper vipMapper) {
        this.vipMapper = vipMapper;
    }

    @Autowired
    @Qualifier("persistentMessagePostProcessor")
    private MessagePostProcessor persistentProcessor;

    @Autowired
    @Qualifier("nonPersistentMessagePostProcessor")
    private MessagePostProcessor nonPersistentProcessor;

    public RankAndQuota getQuotaAndRank(String id) {
        try {
            String sr = redisTemplate.opsForValue().get("RANK:" + id);
            String sq = redisTemplate.opsForValue().get("QUOTA:" + id);
            if (sr != null && sq != null) {
                RankAndQuota raq = new RankAndQuota();
                raq.setRank(Integer.parseInt(sr));
                raq.setQuota(Integer.parseInt(sq));
                return raq;
            } else {
                throw new Exception("no redis cache");
            }
        } catch (Exception e) {
            if (e.getMessage().equals("no redis")) {
                RankAndQuota rankAndQuota = vipMapper.selectRankByUserId(id);
                redisTemplate.opsForValue().set("QUOTA:" + id, rankAndQuota.getQuota() + "", 450, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set("RANK:" + id, rankAndQuota.getRank() + "", 450, TimeUnit.MINUTES);
                return rankAndQuota;
            } else {
                log.error(e.getMessage());
                RankAndQuota rankAndQuota = vipMapper.selectRankByUserId(id);
                redisTemplate.opsForValue().set("QUOTA:" + id, rankAndQuota.getQuota() + "", 450, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set("RANK:" + id, rankAndQuota.getRank() + "", 450, TimeUnit.MINUTES);
                return rankAndQuota;
            }
        }
    }

    public void publish(String message) {
        log.info("向RabbitMQ发送信息：" + message);
        UUID uuid = UUID.randomUUID();
        mqMessages.addMessage(uuid.toString(), message, EXCHANGE_DIRECT);
        try {
            rabbitTemplate.convertAndSend(EXCHANGE_DIRECT, ROUTING_KEY, message, persistentProcessor, new CorrelationData(uuid.toString()));
        } catch (AmqpException e) {
            publishToDeadQueue(message);
        }
    }

    public void publishToDeadQueue(String message) {
        try {
            log.info("向RabbitMQ发送死信：" + message);
            UUID uuid = UUID.randomUUID();
            mqMessages.addMessage(uuid.toString(), message, EXCHANGE_DEAD);
            rabbitTemplate.convertAndSend(EXCHANGE_DEAD, ROUTING_DEAD, message, nonPersistentProcessor, new CorrelationData(uuid.toString()));
        } catch (Exception e) {
            log.error(e.getMessage()+"向RabbitMQ发送信息失败，死信也失败了，并且是直接catch错误");
        }
    }

    public void quotaCost(String id, int cost) {
        try {
            redisTemplate.opsForValue().decrement("QUOTA:" + id, cost);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("Redis爆炸了");
        }
        JSONObject jsonObject = new JSONObject();
        String id_id = System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 23);
        //防止二次投递进行的幂等性操作
        jsonObject.put("idempotence_id", id_id);
        jsonObject.put("quota", cost);
        jsonObject.put("user_id", id);
        jsonObject.put("timestamp", System.currentTimeMillis());
        this.publish(jsonObject.toString());
    }
}
