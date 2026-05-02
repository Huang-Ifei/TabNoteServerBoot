package com.tabnote.server.tabnoteserverboot;


import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

@SpringBootTest
public class TabNoteTest {
    DiscoveryClient discoveryClient;

    Object ox = new Object();
    @Autowired
    public void setDiscoveryClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }
    @Test
    public void test() throws Exception {
    }
}
