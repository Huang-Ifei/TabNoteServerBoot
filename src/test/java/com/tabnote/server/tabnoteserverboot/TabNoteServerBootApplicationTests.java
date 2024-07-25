package com.tabnote.server.tabnoteserverboot;

import com.alibaba.fastjson2.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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


@SpringBootTest
class TabNoteServerBootApplicationTests {

    @Test
    public void testConnect() throws Exception {
        Jedis jedis = new Jedis("172.24.145.1", 6379);
        jedis.set("clientName", "Jedis");
        System.out.println(jedis.get("clientName"));
    }
}
