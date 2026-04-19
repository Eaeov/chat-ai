package com.zyj.chatai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.ai.chat.model.ChatModel;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.config
 * @Project：chat-ai
 * @name：CorsConfig
 * @Date：12 4月 2026  11:19
 * @Filename：CorsConfig
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*") //  允许所有请求方法（GET/POST/OPTIONS）
                .allowCredentials(false)
                .maxAge(3600);
    }

//    @Bean
//    public ChatClient chatClient(ChatModel chatModel) {
//        return ChatClient.builder(chatModel)
//                .build();
//    }


}

