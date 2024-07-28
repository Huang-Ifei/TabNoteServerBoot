package com.tabnote.server.tabnoteserverboot;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mappers.ClassMapper;
import com.tabnote.server.tabnoteserverboot.mappers.TabNoteMapper;
import com.tabnote.server.tabnoteserverboot.models.TabNote;
import com.tabnote.server.tabnoteserverboot.models.TabNoteForList;
import com.tabnote.server.tabnoteserverboot.redis.LikeCount;
import com.tabnote.server.tabnoteserverboot.services.FileService;
import org.apache.ibatis.annotations.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;


@SpringBootTest
class TabNoteServerBootApplicationTests {
    TabNoteMapper tabNoteMapper;
    ClassMapper classMapper;
    AccountMapper accountMapper;
    FileService fileService;
    LikeCount likeCount;

    @Autowired
    public void setTabNoteMapper(TabNoteMapper tabNoteMapper) {
        this.tabNoteMapper = tabNoteMapper;
    }

    @Autowired
    public void setClassMapper(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Autowired
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    @Autowired
    public void setLikeCount(LikeCount likeCount) {
        this.likeCount = likeCount;
    }


    @Test
    public void testConnect() throws Exception {
        for(int i=0;i<5;i++){
            testGet();
            Thread.sleep(6000-i*1100);
        }
    }

    public void testGet(){
        int page = 2;

        int start = (page - 1) * 20;
        JSONObject returnJSON = new JSONObject();

        try {
            List<TabNoteForList> list = tabNoteMapper.getTabNote(start);

            long startTime2 = System.currentTimeMillis();

            for (TabNoteForList tabNoteForList : list) {
                likeCount.getTabNoteLikeCount(tabNoteForList.getTab_note_id());
            }
            System.out.println("****JSON build time(ms)：" + (System.currentTimeMillis() - startTime2));
        } catch (Exception e) {
            e.printStackTrace();
            returnJSON.put("response", "failed");
        }
    }
}

