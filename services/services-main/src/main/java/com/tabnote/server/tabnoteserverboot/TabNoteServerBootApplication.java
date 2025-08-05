package com.tabnote.server.tabnoteserverboot;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

//注
@EnableDiscoveryClient
@SpringBootApplication
@MapperScan({"com.tabnote.server.tabnoteserverboot.mappers"})
@PropertySources({
        @PropertySource("classpath:application.properties")
})
public class TabNoteServerBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(TabNoteServerBootApplication.class, args);
    }
}
