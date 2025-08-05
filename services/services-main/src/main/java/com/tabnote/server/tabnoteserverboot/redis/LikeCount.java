package com.tabnote.server.tabnoteserverboot.redis;

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
public class LikeCount {

    private static final Logger log = LoggerFactory.getLogger(LikeCount.class);

    private volatile long stopUseRedis;

    public LikeCount() {
        stopUseRedis = 0;
    }

    private StringRedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
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
                    log.info(tabNoteId + "'s like count boot:" + o);
                    return Integer.parseInt(o.toString());
                } else {
                    Integer tabNoteLikeCount = tabNoteMapper.getTabNoteLikeCount(tabNoteId);
                    redisTemplate.opsForValue().set("like:" + tabNoteId, tabNoteLikeCount+"", 100, TimeUnit.SECONDS);
                    return tabNoteLikeCount;
                }
            } else {
                log.error("redis use be banned");
                return tabNoteMapper.getTabNoteLikeCount(tabNoteId);
            }
        } catch (QueryTimeoutException | RedisConnectionFailureException e) {
            log.error("Redis stop use in next 100 seconds,because:connect time out,redis maybe in chaos");
            stopUseRedis = System.currentTimeMillis();
            return tabNoteMapper.getTabNoteLikeCount(tabNoteId);
        } catch (Exception e) {
            log.error(e.getMessage());
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
                redisTemplate.opsForValue().set("like:" + tabNoteId, tabNoteLikeCount+"", 100, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
