package com.tabnote.server.tabnoteserverboot;


import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;
import java.util.concurrent.*;

@SpringBootTest
public class TabNoteTest {
    DiscoveryClient discoveryClient;

    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    @Test
    public void test() throws Exception {
//        Thread.sleep(10000);
//        List<ServiceInstance> bertVectorCache = discoveryClient.getInstances("TabNoteServerMain");
//        String url = bertVectorCache.get(0).getHost() + ":" +  bertVectorCache.get(0).getPort();
//        System.out.println(url);
//        System.out.println(discoveryClient.getServices().get(0));
//        System.out.println(discoveryClient.getInstances(discoveryClient.getServices().get(0)).get(0).getHost());
    }
}
