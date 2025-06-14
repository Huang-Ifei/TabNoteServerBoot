package com.tabnote.server.tabnoteserverboot.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class TabNoteMixGateway {
    DiscoveryClient discoveryClient;
    @Autowired
    public TabNoteMixGateway(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    RestTemplate restTemplate;
    @Autowired
    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    public String getGateWayHost(){
        String url = "";
        try{
            List<ServiceInstance> apiCheckMicroService = discoveryClient.getInstances("TabNote_Mix_Gateway");
            url = "http://" + apiCheckMicroService.get(0).getHost() + ":" + apiCheckMicroService.get(0).getPort();
        } catch (Exception e) {
            System.out.println("网关获取错误");
            e.printStackTrace();
        }
        return url;
    }
}
