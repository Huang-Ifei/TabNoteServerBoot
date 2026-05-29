package com.tabnote.server.tabnoteserverboot.component;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CDNAI {

    private static final Logger log = LoggerFactory.getLogger(CDNAI.class);

    private static final String KEY_PREFIX = "cai+";
    private static final long KEY_TTL_SECONDS = 450;

    private StringRedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void newTACADS(String ca_id) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + ca_id, "", KEY_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("CDNAI newTACADS error: {}", e.getMessage());
        }
    }

    public void sendToTACADS(String ca_id, String s) {
        try {
            redisTemplate.opsForValue().append(KEY_PREFIX + ca_id, s + "\n");
        } catch (Exception e) {
            log.error("CDNAI sendToTACADS error: {}", e.getMessage());
        }
    }

    public String getByIndex(String cdnAiId, int index) {
        String str = redisTemplate.opsForValue().get(KEY_PREFIX + cdnAiId);
        if (str == null || str.isEmpty()) {
            return null;
        }
        if (index == 0) {
            return str;
        }
        String[] lines = str.split("\n");
        int sum = 0;
        boolean found = false;
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (!found) {
                try {
                    JSONObject json = JSONObject.parseObject(lines[i]);
                    JSONObject mess = json.getJSONObject("message");
                    int len = 0;
                    if (mess != null) {
                        String content = mess.getString("content");
                        if (content != null) {
                            len += content.codePointCount(0, content.length());
                        }
                    }
                    sum += len;
                    if (sum == index) {
                        found = true;
                    } else if (sum > index) {
                        return null;
                    }
                } catch (Exception e) {
                    return null;
                }
            } else {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(lines[i]);
            }
        }
        return found ? result.toString() : null;
    }
}
