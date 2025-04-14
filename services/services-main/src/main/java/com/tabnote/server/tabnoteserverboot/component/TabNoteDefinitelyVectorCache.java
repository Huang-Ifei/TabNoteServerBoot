package com.tabnote.server.tabnoteserverboot.component;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class TabNoteDefinitelyVectorCache {
    DiscoveryClient discoveryClient;
    @Autowired
    public TabNoteDefinitelyVectorCache(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    RestTemplate restTemplate;
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void addToBQVectorCache(String bqid,String text){
        try {
            List<ServiceInstance> bertVectorCache = discoveryClient.getInstances("BERT_VECTOR_CACHE");
            String url = "http://" + bertVectorCache.get(0).getHost() + ":" + bertVectorCache.get(0).getPort();

            // 创建请求头并设置 Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建请求体 JSON 对象
            JSONObject addCacheJSON = new JSONObject();
            addCacheJSON.put("data", text);
            addCacheJSON.put("id", bqid);

            // 创建 HttpEntity 对象，将 headers 和 请求体 JSON 结合起来
            HttpEntity<String> requestEntity = new HttpEntity<>(addCacheJSON.toString(), headers);

            // 使用 HttpEntity 发送请求
            System.out.println(restTemplate.postForObject(url + "/add", requestEntity, String.class));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("缓存添加操作出错：" + e);
        }
    }

    public String getBQVectorCache(String text){
        try {
            List<ServiceInstance> bertVectorCache = discoveryClient.getInstances("BERT_VECTOR_CACHE");
            String url = "http://" + bertVectorCache.get(0).getHost() + ":" + bertVectorCache.get(0).getPort();

            // 创建请求头并设置 Content-Type
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建请求体 JSON 对象
            JSONObject addCacheJSON = new JSONObject();
            addCacheJSON.put("data", text);

            // 创建 HttpEntity 对象，将 headers 和 请求体 JSON 结合起来
            HttpEntity<String> requestEntity = new HttpEntity<>(addCacheJSON.toString(), headers);

            String backData = restTemplate.postForObject(url + "/find", requestEntity, String.class);
            // 使用 HttpEntity 发送请求
            System.out.println(backData);

            JSONObject cacheBackJSON = JSONObject.parseObject(backData);
            if (cacheBackJSON.getString("response").equals("hit")) {
                System.out.println("缓存命中：" + backData);
                return cacheBackJSON.getJSONObject("data").getJSONObject("entity").getString("id");
            } else {
                System.out.println("缓存未命中：" + backData);
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("缓存添加操作出错：" + e);
            return "";
        }
    }
}
