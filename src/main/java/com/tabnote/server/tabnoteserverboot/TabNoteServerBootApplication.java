package com.tabnote.server.tabnoteserverboot;

import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tabnote.server.tabnoteserverboot.mappers")
public class TabNoteServerBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(TabNoteServerBootApplication.class, args);
	}
}
