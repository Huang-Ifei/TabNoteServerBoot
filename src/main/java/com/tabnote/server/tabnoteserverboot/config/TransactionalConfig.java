package com.tabnote.server.tabnoteserverboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan("com.tabnote.server.tabnoteserverboot.services")
@EnableTransactionManagement
public class TransactionalConfig {

}
