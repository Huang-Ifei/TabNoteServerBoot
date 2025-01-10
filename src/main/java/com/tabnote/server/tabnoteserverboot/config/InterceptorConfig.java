package com.tabnote.server.tabnoteserverboot.config;

import com.tabnote.server.tabnoteserverboot.interceptor.AiRequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final AiRequestInterceptor aiRequestInterceptor;

    @Autowired
    public InterceptorConfig(AiRequestInterceptor aiRequestInterceptor) {
        this.aiRequestInterceptor = aiRequestInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration interceptorRegistration = registry.addInterceptor(aiRequestInterceptor);
        interceptorRegistration.addPathPatterns("/ai/bq");
        interceptorRegistration.addPathPatterns("/ai/messages");
        interceptorRegistration.addPathPatterns("/ai/note");
        interceptorRegistration.addPathPatterns("/ai/gpt");
    }
}
