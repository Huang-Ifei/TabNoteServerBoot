package com.tabnote.server.tabnoteserverboot.controller;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@CrossOrigin
@Controller
public class XianController {

    @GetMapping("xian")
    public ResponseEntity<String> publicKey(@RequestParam String account,@RequestParam String authCode) throws Exception {
        System.out.println("xian");
        JSONObject jsonObject = new JSONObject();
        UUID VIP1Key = UUID.randomUUID();

        //构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://101.42.31.139:8888/user/auth?authCode="+authCode+"&account="+account))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject getJson = JSONObject.parseObject(response.body());
        int code = getJson.getInteger("code");
        if (code==-1){
            jsonObject.put("code", -1);
        }else if (code==0){
            jsonObject.put("code", 0);
            jsonObject.put("VIP1Key", VIP1Key);
        }

        return sendMes(jsonObject);
    }

    private ResponseEntity<String> sendErr() {
        return ResponseEntity.badRequest().body("err");
    }

    private ResponseEntity<String> sendMes(JSONObject sendJSON) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(sendJSON.toString());
    }
}
