package com.zyj.chatai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.model.ChatModel;

@Configuration
public class AiModelConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Value("${ai.models.qwen-plus}")
    private String qwenPlusName;

    @Value("${ai.models.qwen-turbo}")
    private String qwenTurboName;


    // 1. 手动创建 DashScopeApi Bean (使用已注入的 apiKey)
    @Bean
    public DashScopeApi dashScopeApi() {
        return DashScopeApi.builder()
                .apiKey(apiKey)
                .build();
    }

    //  提取公共构建逻辑，避免重复
    private ChatModel createChatModel(DashScopeApi api, String model) {
        DashScopeChatOptions options = DashScopeChatOptions.builder()
                .model(model)
                .temperature(0.7)
                //.maxToken(2048)
                .build();
        return DashScopeChatModel.builder()
                .dashScopeApi(api)
                .defaultOptions(options)
                .build();
    }

    @Bean("qwen-plus")
    public ChatModel qwenPlusChatModel(DashScopeApi dashScopeApi) {
        return createChatModel(dashScopeApi, qwenPlusName);
    }

    @Bean("qwen-turbo")
    public ChatModel qwenTurboChatModel(DashScopeApi dashScopeApi) {
        return createChatModel(dashScopeApi, qwenTurboName);
    }

}
