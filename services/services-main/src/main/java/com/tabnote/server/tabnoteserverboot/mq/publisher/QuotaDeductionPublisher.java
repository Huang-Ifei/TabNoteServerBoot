package com.tabnote.server.tabnoteserverboot.mq.publisher;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.MQMessages;
import com.tabnote.server.tabnoteserverboot.mappers.AiMapper;
import com.tabnote.server.tabnoteserverboot.mappers.VipMapper;
import com.tabnote.server.tabnoteserverboot.models.RankAndQuota;
import org.springframework.amqp.core.Correlation;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
                redisTemplate.opsForValue().set("QUOTA:" + id, rankAndQuota.getQuota() + "",450, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set("RANK:" + id, rankAndQuota.getRank() + "",450, TimeUnit.MINUTES);
                return rankAndQuota;
            }else {
                e.printStackTrace();
                RankAndQuota rankAndQuota = vipMapper.selectRankByUserId(id);
                redisTemplate.opsForValue().set("QUOTA:" + id, rankAndQuota.getQuota() + "",450, TimeUnit.MINUTES);
                redisTemplate.opsForValue().set("RANK:" + id, rankAndQuota.getRank() + "",450, TimeUnit.MINUTES);
                return rankAndQuota;
            }
        }
    }

    public void publish(String message) {
        System.out.println("向RabbitMQ发送信息：" + message);
        UUID uuid = UUID.randomUUID();
        mqMessages.addMessage(uuid.toString(), message, EXCHANGE_DIRECT);
        rabbitTemplate.convertAndSend(EXCHANGE_DIRECT, ROUTING_KEY, message, new CorrelationData(uuid.toString()));
    }

    public void quotaCost(String id, int cost) {
        redisTemplate.opsForValue().decrement("QUOTA:" + id, cost);
        JSONObject jsonObject = new JSONObject();
        //防止二次投递进行的幂等性操作
        jsonObject.put("idempotence_id", UUID.randomUUID().toString());
        jsonObject.put("quota", cost);
        jsonObject.put("user_id", id);
        jsonObject.put("timestamp", System.currentTimeMillis());
        this.publish(jsonObject.toString());
    }
}
