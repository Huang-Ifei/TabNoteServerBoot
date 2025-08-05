package com.tabnote.server.tabnoteserverboot.config;

import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MPPConfig {
    @Bean("persistentMessagePostProcessor")
    public MessagePostProcessor persistentMessagePostProcessor() {
        return message -> {
            message.getMessageProperties().setContentEncoding("UTF-8");
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        };
    }

    @Bean("nonPersistentMessagePostProcessor")
    public MessagePostProcessor nonPersistentMessagePostProcessor() {
        return message -> {
            message.getMessageProperties().setContentEncoding("UTF-8");
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);
            return message;
        };
    }
}
