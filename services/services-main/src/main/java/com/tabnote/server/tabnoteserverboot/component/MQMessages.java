package com.tabnote.server.tabnoteserverboot.component;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MQMessages {

    class MQData {
        String message;
        String aimExchange;
        int tryTime;

        public MQData(String message,String aimExchange) {
            this.message = message;
            this.aimExchange = aimExchange;
            this.tryTime = 0;
        }
    }

    private ConcurrentHashMap<String, MQData> concurrentMap = new ConcurrentHashMap<>();

    public void addMessage(String key, String value,String aimExchange) {
        concurrentMap.put(key, new MQData(value,aimExchange));
    }

    public int getTryTime(String key) {
        return concurrentMap.get(key).tryTime;
    }

    public String getAimExchange(String key) {
        return concurrentMap.get(key).aimExchange;
    }

    public String getMessage(String key) {
        concurrentMap.get(key).tryTime++;
        return concurrentMap.get(key).message;
    }

    public void removeMessage(String key) {
        concurrentMap.remove(key);
    }
}
