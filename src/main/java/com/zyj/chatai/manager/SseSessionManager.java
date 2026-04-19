package com.zyj.chatai.manager;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.manager
 * @Project：chat-ai
 * @name：SseSessionManager
 * @Date：12 4月 2026  15:41
 * @Filename：SseSessionManager
 */
@Component
//建立会话管理类，管理用户的SSE连接会话，提供添加、移除和获取会话的方法，以便在需要时向特定用户发送消息。
public class SseSessionManager {
    // Key: 用户ID (Long), Value: SSE连接
    private final Map<String, SseEmitter> sseMap = new ConcurrentHashMap<>();

    public void connect(String sessionId, SseEmitter emitter) {
        // 🔥 设置30秒超时，防止内存泄漏
        sseMap.put(sessionId, emitter);
        // 设置完成、错误和超时处理
        emitter.onCompletion(() -> sseMap.remove(sessionId));
        emitter.onError(e -> sseMap.remove(sessionId));
        emitter.onTimeout(() -> sseMap.remove(sessionId));
    }

    // 发送消息
    public void send(String sessionId, String content) {
        SseEmitter emitter = sseMap.get(sessionId);
        if (emitter != null) {
            try {
                emitter.send(content);
            } catch (Exception e) {
                sseMap.remove(sessionId); // 发送失败，移除连接
            }
        }
    }

    // 关闭连接
    public void complete(String sessionId) {
        SseEmitter emitter = sseMap.get(sessionId);
        if (emitter != null) {
            emitter.complete();
            sseMap.remove(sessionId);
        }
    }

    // 等待连接就绪
    public void waitForConnectionReady(String sessionId, long timeoutMillis) throws InterruptedException {
        long start = System.currentTimeMillis();
        while (!sseMap.containsKey(sessionId)) {
            if (System.currentTimeMillis() - start > timeoutMillis) {
                throw new RuntimeException("SSE 连接建立超时");
            }
            Thread.sleep(50);
        }
    }
}
