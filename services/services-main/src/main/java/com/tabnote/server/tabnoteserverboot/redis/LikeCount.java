package com.tabnote.server.tabnoteserverboot.redis;

import ch.qos.logback.core.util.TimeUtil;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LikeCount {

    private long stopUseRedis;

    public LikeCount() {
        stopUseRedis = 0;
    }

    private RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private TabNoteMapper tabNoteMapper;

    @Autowired
    public void setTabNoteMapper(TabNoteMapper tabNoteMapper) {
        this.tabNoteMapper = tabNoteMapper;
    }

    public int getTabNoteLikeCount(String tabNoteId) {
        Object o = null;
        try {
            if (System.currentTimeMillis() - stopUseRedis > 100000) {
                o = redisTemplate.opsForValue().get("like:" + tabNoteId);
                if (o != null) {
                    System.out.println(tabNoteId + "'s like count boot:" + o);
                    return Integer.parseInt(o.toString());
                } else {
                    Integer tabNoteLikeCount = tabNoteMapper.getTabNoteLikeCount(tabNoteId);
                    redisTemplate.opsForValue().set("like:" + tabNoteId, tabNoteLikeCount, 100, TimeUnit.SECONDS);
                    return tabNoteLikeCount;
                }
            } else {
                System.out.println("redis use be banned");
                return tabNoteMapper.getTabNoteLikeCount(tabNoteId);
            }
        } catch (QueryTimeoutException | RedisConnectionFailureException e) {
            System.out.println("Redis stop use in next 100 seconds,because:connect time out,redis maybe in chaos");
            stopUseRedis = System.currentTimeMillis();
            return tabNoteMapper.getTabNoteLikeCount(tabNoteId);
        } catch (Exception e) {
            System.out.println(e);
            return tabNoteMapper.getTabNoteLikeCount(tabNoteId);
        }
    }

    public void likeTabNote(String tabNoteId, String usr_id) {
        try {
            tabNoteMapper.likeNote(tabNoteId, usr_id);
            if (redisTemplate.hasKey("like:" + tabNoteId)) {
                redisTemplate.opsForValue().increment("like:" + tabNoteId, 1);
                redisTemplate.expire("like:" + tabNoteId, 100, TimeUnit.SECONDS);
            }else{
                Integer tabNoteLikeCount = tabNoteMapper.getTabNoteLikeCount(tabNoteId);
                redisTemplate.opsForValue().set("like:" + tabNoteId, tabNoteLikeCount, 100, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
