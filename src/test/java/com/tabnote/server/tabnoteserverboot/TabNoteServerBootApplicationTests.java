package com.tabnote.server.tabnoteserverboot;

import com.alibaba.fastjson2.JSONObject;
import com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption;
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

import javax.crypto.Cipher;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;


@SpringBootTest
class TabNoteServerBootApplicationTests {

}
