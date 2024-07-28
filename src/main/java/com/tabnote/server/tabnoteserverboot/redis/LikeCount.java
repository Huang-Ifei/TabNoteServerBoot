package com.tabnote.server.tabnoteserverboot.redis;

import ch.qos.logback.core.util.TimeUtil;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LikeCount {

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

    public int getTabNoteLikeCount(String tabNoteId){
        Object o = null;
        try{
            o = redisTemplate.opsForValue().get("like:" + tabNoteId);
            if(o != null){
                System.out.println(tabNoteId+"'s like count boot:"+o);
                return Integer.parseInt(o.toString());
            }else{
                Integer tabNoteLikeCount = tabNoteMapper.getTabNoteLikeCount(tabNoteId);
                redisTemplate.opsForValue().set("like:" + tabNoteId, tabNoteLikeCount,5, TimeUnit.SECONDS);
                return tabNoteLikeCount;
            }
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(o);
            return tabNoteMapper.getTabNoteLikeCount(tabNoteId);
        }
    }

}
