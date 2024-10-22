package com.tabnote.server.tabnoteserverboot;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.services.AccountService;
import com.tabnote.server.tabnoteserverboot.services.XianService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@SpringBootTest
class TabNoteServerBootApplicationTests {
    XianService xianService;
    @Autowired
    public void setXianService(XianService xianService) {
        this.xianService = xianService;
    }
    @Test
    public void test() throws Exception {
        xianService.scheduledTask();
    }
}
