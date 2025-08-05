package com.tabnote.server.tabnoteserverboot.component;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

//CDN不准确，应该是AI对话缓存/分发微服务
@Component
public class CDNAI {

    private static final Logger log = LoggerFactory.getLogger(CDNAI.class);

    DiscoveryClient discoveryClient;
    @Autowired
    public CDNAI(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    RestTemplate restTemplate;
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getTACADSHost(){
        String url = "";
        try{
            List<ServiceInstance> apiCheckMicroService = discoveryClient.getInstances("TabNote_AI_Cache_And_Delivery_Service");
            url = "http://" + apiCheckMicroService.get(0).getHost() + ":" + apiCheckMicroService.get(0).getPort();
        } catch (Exception e) {
            System.out.println("TabNote_AI_Cache_And_Delivery_Service,TabNote精华爆炸啦！！我的缓存递送微服务爆炸啦！！");
            log.error(e.getMessage());
        }
        return url;
    }

    public void sendToTACADS(String ca_id,String s){

        JSONObject param = new JSONObject();
        param.put("cdn_ai_id", ca_id);
        param.put("JSONList", s);
        //添加token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=UTF-8");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try{
            //封装请求头
            HttpEntity<String> formEntity = new HttpEntity<String>(param.toString(),headers);
            HttpEntity<String> he = restTemplate.postForEntity(getTACADSHost()+"/push", formEntity, String.class);
            System.out.println(he.getBody());
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }
    public void newTACADS(String ca_id){

        JSONObject param = new JSONObject();
        param.put("cdn_ai_id", ca_id);
        param.put("JSONList", "");
        //添加token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type","application/json;charset=UTF-8");
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        try {
            //封装请求头
            HttpEntity<String> formEntity = new HttpEntity<String>(param.toString(),headers);
            HttpEntity<String> he = restTemplate.postForEntity(getTACADSHost()+"/new", formEntity, String.class);
            System.out.println(he.getBody());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
