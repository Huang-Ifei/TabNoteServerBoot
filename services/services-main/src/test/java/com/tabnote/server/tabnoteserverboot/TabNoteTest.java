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

        System.out.println(System.currentTimeMillis()  + UUID.randomUUID().toString().substring(0, 23));
        Queue<Integer> queue = new LinkedList<>();
        Integer poll = queue.poll();
        queue.poll();
        queue.add(1);
        queue.peek();
        Double.valueOf("1");
        Double d = Double.valueOf("0.1");
        Stack<Integer> stack = new Stack<>();
        stack.push(1);
        stack.pop();

        List<Integer> list = new LinkedList<>();
        PriorityQueue<Integer> pq = new PriorityQueue<>(Comparator.reverseOrder());
        pq.add(1);
        pq.poll();
        Arrays.sort(new int[]{1,2,3});
        Object o = new Object();
        System.out.println(o.toString()+":"+o.hashCode());
        Thread thread = new Thread(()->{

        });
        thread.start();
        thread.interrupt();

        Class<?> aClass = Class.forName("com.tabnote.server.tabnoteserverboot.component.TabNoteInfiniteEncryption");
        Object o1 = aClass.getDeclaredConstructor().newInstance();
        Method method = o1.getClass().getMethod("getPublicKey");
        System.out.println(method.invoke(o1));

//        Thread.sleep(10000);
//        List<ServiceInstance> bertVectorCache = discoveryClient.getInstances("TabNoteServerMain");
//        String url = bertVectorCache.get(0).getHost() + ":" +  bertVectorCache.get(0).getPort();
//        System.out.println(url);
//        System.out.println(discoveryClient.getServices().get(0));
//        System.out.println(discoveryClient.getInstances(discoveryClient.getServices().get(0)).get(0).getHost());
    }
}
