package com.tabnote.server.tabnoteserverboot.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class XianService {
    @Scheduled(cron = "* * * * * *")
    public void scheduledTask() {
        System.out.println("scheduledTask");
    }
}
