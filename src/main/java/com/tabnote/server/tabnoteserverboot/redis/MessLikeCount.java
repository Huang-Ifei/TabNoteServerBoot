package com.tabnote.server.tabnoteserverboot.redis;

import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.MessageMapper;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MessLikeCount {

    private long stopUseRedis;

    public MessLikeCount() {
        stopUseRedis = 0;
    }

    private RedisTemplate redisTemplate;
    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
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
                    System.out.println(tabMessId + "'s TM like count boot:" + o);
                    return Integer.parseInt(o.toString());
                } else {
                    Integer tabNoteLikeCount = messageMapper.getTabMessLikeCount(tabMessId);
                    redisTemplate.opsForValue().set("TMLike:" + tabMessId, tabNoteLikeCount, 100, TimeUnit.SECONDS);
                    return tabNoteLikeCount;
                }
            } else {
                System.out.println("redis use be banned");
                return messageMapper.getTabMessLikeCount(tabMessId);
            }
        } catch (QueryTimeoutException | RedisConnectionFailureException e) {
            System.out.println("Redis stop use in next 100 seconds,because:connect time out,redis maybe in chaos");
            stopUseRedis = System.currentTimeMillis();
            return messageMapper.getTabMessLikeCount(tabMessId);
        } catch (Exception e) {
            System.out.println(e);
            return messageMapper.getTabMessLikeCount(tabMessId);
        }
    }

    public void likeTabMess(String tabMessId, String usr_id) {
        try {
            messageMapper.likeTabMess(tabMessId, usr_id);
            Object o = redisTemplate.opsForValue().get("TMLike:" + tabMessId);
            if (o != null) {
                Integer tabNoteLikeCount = Integer.parseInt(o.toString()) + 1;
                System.out.println(tabMessId + " TM like++ to:" + tabNoteLikeCount);
                redisTemplate.opsForValue().set("TMLike:" + tabMessId, tabNoteLikeCount, 100, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMessMessLikeCount(String messMessId) {
        Object o = null;
        try {
            if (System.currentTimeMillis() - stopUseRedis > 100000) {
                o = redisTemplate.opsForValue().get("MMLike:" + messMessId);
                if (o != null) {
                    System.out.println(messMessId + "'s MM like count boot:" + o);
                    return Integer.parseInt(o.toString());
                } else {
                    Integer tabNoteLikeCount = messageMapper.getMessMessLikeCount(messMessId);
                    redisTemplate.opsForValue().set("MMLike:" + messMessId, tabNoteLikeCount, 100, TimeUnit.SECONDS);
                    return tabNoteLikeCount;
                }
            } else {
                System.out.println("redis use be banned");
                return messageMapper.getMessMessLikeCount(messMessId);
            }
        } catch (QueryTimeoutException | RedisConnectionFailureException e) {
            System.out.println("Redis stop use in next 100 seconds,because:connect time out,redis maybe in chaos");
            stopUseRedis = System.currentTimeMillis();
            return messageMapper.getMessMessLikeCount(messMessId);
        } catch (Exception e) {
            System.out.println(e);
            return messageMapper.getMessMessLikeCount(messMessId);
        }
    }

    public void likeMessMess(String messMessId, String usr_id) {
        try {
            messageMapper.likeMess(messMessId, usr_id);
            Object o = redisTemplate.opsForValue().get("MMLike:" + messMessId);
            if (o != null) {
                Integer tabNoteLikeCount = Integer.parseInt(o.toString()) + 1;
                System.out.println(messMessId + " MM like++ to:" + tabNoteLikeCount);
                redisTemplate.opsForValue().set("MMLike:" + messMessId, tabNoteLikeCount, 100, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
