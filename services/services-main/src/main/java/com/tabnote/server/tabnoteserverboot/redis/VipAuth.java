package com.tabnote.server.tabnoteserverboot.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class VipAuth {

    private static final Logger log = LoggerFactory.getLogger(VipAuth.class);
    private StringRedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String setVipAuth(int rank) throws Exception {
        log.info("添加授权码到Redis"+rank);
        UUID uuid = UUID.randomUUID();
        redisTemplate.opsForValue().set("vip:"+uuid.toString(), rank+"",7, TimeUnit.DAYS);
        return uuid.toString();
    }

    public int checkVipAuth(String auth) {
        log.info("确认VIP等级"+auth);
        try{
            int rank = Integer.parseInt(redisTemplate.opsForValue().get("vip:"+auth));
            return rank;
        }catch (Exception e){
            return 0;
        }
    }
    public void deleteVipAuth(String auth) {
        log.info("删除授权码");
        redisTemplate.delete("vip:"+auth);
    }

    public int rankToQuota (int rank){
        //对应AFA
        if (rank==2){
            return 2850000;
        } else if (rank==4) {
            //对应AFA+
            return 11400000;
        } else if (rank==6) {
            //对应AFA++
            return 5130000;
        } else {
            return 0;
        }
    }
}
