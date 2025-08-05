package com.tabnote.server.tabnoteserverboot.redis;

import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.MessageMapper;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MessLikeCount {

    private static final Logger log = LoggerFactory.getLogger(MessLikeCount.class);

    private volatile long stopUseRedis;

    public MessLikeCount() {
        stopUseRedis = 0;
    }

    private StringRedisTemplate redisTemplate;
    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private MessageMapper messageMapper;
    @Autowired
    public void setMessageMapper(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    public int getTabMessLikeCount(String tabMessId) {
        Object o = null;
        try {
            if (System.currentTimeMillis() - stopUseRedis > 100000) {
                o = redisTemplate.opsForValue().get("TMLike:" + tabMessId);
                if (o != null) {
                    log.info(tabMessId + "'s TM like count boot:" + o);
                    return Integer.parseInt(o.toString());
                } else {
                    Integer tabNoteLikeCount = messageMapper.getTabMessLikeCount(tabMessId);
                    redisTemplate.opsForValue().set("TMLike:" + tabMessId, tabNoteLikeCount+"", 100, TimeUnit.SECONDS);
                    return tabNoteLikeCount;
                }
            } else {
                log.error("redis use be banned");
                return messageMapper.getTabMessLikeCount(tabMessId);
            }
        } catch (QueryTimeoutException | RedisConnectionFailureException e) {
            log.error("Redis stop use in next 100 seconds,because:connect time out,redis maybe in chaos");
            stopUseRedis = System.currentTimeMillis();
            return messageMapper.getTabMessLikeCount(tabMessId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return messageMapper.getTabMessLikeCount(tabMessId);
        }
    }

    public void likeTabMess(String tabMessId, String usr_id) {
        try {
            messageMapper.likeTabMess(tabMessId, usr_id);
            if(redisTemplate.hasKey("TMLike:" + tabMessId)){
                redisTemplate.opsForValue().increment("TMLike:" + tabMessId, 1);
                redisTemplate.expire("TMLike:" + tabMessId, 100, TimeUnit.SECONDS);
            }else{
                Integer tabNoteLikeCount = messageMapper.getTabMessLikeCount(tabMessId);
                redisTemplate.opsForValue().set("TMLike:" + tabMessId, tabNoteLikeCount+"", 100, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public int getMessMessLikeCount(String messMessId) {
        Object o = null;
        try {
            if (System.currentTimeMillis() - stopUseRedis > 100000) {
                o = redisTemplate.opsForValue().get("MMLike:" + messMessId);
                if (o != null) {
                    log.info(messMessId + "'s MM like count boot:" + o);
                    return Integer.parseInt(o.toString());
                } else {
                    Integer tabNoteLikeCount = messageMapper.getMessMessLikeCount(messMessId);
                    redisTemplate.opsForValue().set("MMLike:" + messMessId, tabNoteLikeCount+"", 100, TimeUnit.SECONDS);
                    return tabNoteLikeCount;
                }
            } else {
                log.error("redis use be banned");
                return messageMapper.getMessMessLikeCount(messMessId);
            }
        } catch (QueryTimeoutException | RedisConnectionFailureException e) {
            log.error("Redis stop use in next 100 seconds,because:connect time out,redis maybe in chaos");
            stopUseRedis = System.currentTimeMillis();
            return messageMapper.getMessMessLikeCount(messMessId);
        } catch (Exception e) {
            log.error(e.getMessage());
            return messageMapper.getMessMessLikeCount(messMessId);
        }
    }

    public void likeMessMess(String messMessId, String usr_id) {
        try {
            messageMapper.likeMess(messMessId, usr_id);
            if (redisTemplate.hasKey("MMLike:" + messMessId)) {
                redisTemplate.opsForValue().increment("MMLike:" + messMessId, 1);
                redisTemplate.expire("MMLike:" + messMessId, 100, TimeUnit.SECONDS);
            }else{
                Integer tabNoteLikeCount = messageMapper.getMessMessLikeCount(messMessId);
                redisTemplate.opsForValue().set("MMLike:" + messMessId, tabNoteLikeCount+"", 100, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
