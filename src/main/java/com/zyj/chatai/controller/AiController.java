package com.zyj.chatai.controller;

import com.alibaba.cloud.ai.dashscope.spec.DashScopeApiSpec;
import com.zyj.chatai.manager.SseSessionManager;
import com.zyj.chatai.pojo.ChatMessage;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.service.MessageService;
import com.zyj.chatai.service.SessionService;
import com.zyj.chatai.utils.BaseContext;
import com.zyj.chatai.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.controller
 * @Project：chat-ai
 * @name：AiController
 * @Date：12 4月 2026  10:10
 * @Filename：AiController
 */
@RestController
@RequestMapping("/api/ai")
@Slf4j
@RequiredArgsConstructor
public class AiController {
    private final Map<String, ChatModel> chatModelMap;
    private final MessageService messageService;
    private final SessionService sessionService;
    private final SseSessionManager sseSessionManager;
    private static final long SSE_TIMEOUT = 3 * 60 * 1000L;
    private ChatClient getChatClient(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            modelName = "qwen-plus";
        }
        ChatModel model = chatModelMap.getOrDefault(modelName,
                chatModelMap.get("qwen-plus"));
        if (model == null) {
            model = chatModelMap.values().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("没有可用的 ChatModel"));
        }
        return ChatClient.builder(model).build();
    }



    /**
     * 前端建立SSE流式连接
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam Long sessionId) {
        // 1. 根据会话ID来建立 连接实现同用户多条聊天
        if (sessionId == null) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        // 2. 创建SSE发射器（设置3分钟超时，防止内存泄漏）
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT); //3分钟超时
        // 3. 绑定用户和连接
        sseSessionManager.connect(sessionId.toString(), emitter);
        // 4. 直接返回emitter（✅ 无任何报错）
        return emitter;
    }

}
//可以用myChatModel 来创建多个模型
//ChatModel myChatModel = ... // already autoconfigured by Spring Boot
//ChatClient chatClient = ChatClient.create(myChatModel);
//
//
//ChatClient.Builder builder = ChatClient.builder(myChatModel);
//ChatClient customChatClient = builder
//        .defaultSystemPrompt("You are a helpful assistant.")
//        .build();