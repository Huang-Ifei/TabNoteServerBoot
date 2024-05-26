package com.tabnote.server.tabnoteserverboot.services;

import com.tabnote.server.tabnoteserverboot.define.AiList;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Component
public class AiRequestCounter {
    final List<Long> proSynchronizedList = Collections.synchronizedList(new ArrayList<>());
    final List<Long> flashSynchronizedList = Collections.synchronizedList(new ArrayList<>());
    public boolean proAiRequestCheck() {
        System.out.println(proSynchronizedList.size());
        if(proSynchronizedList.size()<= AiList.GEMINI_PRO_MAX_DAILY_REQUEST){
            return true;
        }else{
            synchronized (proSynchronizedList) {
                Iterator<Long> iterator = proSynchronizedList.iterator();
                while (iterator.hasNext()){
                    if (System.currentTimeMillis()-iterator.next().longValue()>(24*60*60*1000)){
                        iterator.remove();
                    }else{
                        break;
                    }
                }
            }
            if(proSynchronizedList.size()<=AiList.GEMINI_PRO_MAX_DAILY_REQUEST){
                return true;
            }else {
                return false;
            }
        }
    }
    public boolean flashAiRequestCheck() {
        System.out.println(flashSynchronizedList.size());
        if(flashSynchronizedList.size()<=AiList.GEMINI_FLASH_MAX_DAILY_REQUEST){
            return true;
        }else{
            synchronized (flashSynchronizedList) {
                Iterator<Long> iterator = flashSynchronizedList.iterator();
                while (iterator.hasNext()){
                    if (System.currentTimeMillis()-iterator.next().longValue()>(24*60*60*1000)){
                        iterator.remove();
                    }else{
                        break;
                    }
                }
            }
            if(flashSynchronizedList.size()<=AiList.GEMINI_FLASH_MAX_DAILY_REQUEST){
                return true;
            }else {
                return false;
            }
        }
    }
    public void addFlashAiRequest(){
        Long time = System.currentTimeMillis();
        synchronized (flashSynchronizedList) {
            flashSynchronizedList.add(time);
        }
    }
    public void addProAiRequest(){
        Long time = System.currentTimeMillis();
        synchronized (proSynchronizedList) {
            proSynchronizedList.add(time);
        }
    }
}
