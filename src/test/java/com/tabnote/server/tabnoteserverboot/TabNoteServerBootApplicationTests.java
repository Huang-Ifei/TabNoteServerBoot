package com.tabnote.server.tabnoteserverboot;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.OCR;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
import com.tabnote.server.tabnoteserverboot.mappers.AccountMapper;
import com.tabnote.server.tabnoteserverboot.mq.publisher.QuotaDeductionPublisher;
import com.tabnote.server.tabnoteserverboot.services.LowCodeServiceImpl;
import com.tabnote.server.tabnoteserverboot.services.XianService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.tabnote.server.tabnoteserverboot.define.AiList.*;
import static com.tabnote.server.tabnoteserverboot.define.MQName.EXCHANGE_DIRECT;
import static com.tabnote.server.tabnoteserverboot.define.MQName.ROUTING_KEY;


@SpringBootTest
class TabNoteServerBootApplicationTests {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TabNoteServerBootApplicationTests(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    private QuotaDeductionPublisher quotaDeductionPublisher;

    @Autowired
    public void setQuotaDeductionPublisher(QuotaDeductionPublisher quotaDeductionPublisher) {
        this.quotaDeductionPublisher = quotaDeductionPublisher;
    }

    @Autowired
    LowCodeServiceImpl lowCodeService;

    TabNoteInfiniteEncryption tabNoteInfiniteEncryption;

    @Autowired
    public void setTabNoteInfiniteEncryption(TabNoteInfiniteEncryption tabNoteInfiniteEncryption) {
        this.tabNoteInfiniteEncryption = tabNoteInfiniteEncryption;
    }

    @Autowired
    OCR ocr;
    @Test
    public void t() throws Exception {

    }
}
