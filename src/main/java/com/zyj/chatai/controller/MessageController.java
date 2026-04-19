package com.zyj.chatai.controller;

import com.zyj.chatai.pojo.ChatMessage;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.service.MessageService;
import com.zyj.chatai.utils.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.controller
 * @Project：chat-ai
 * @name：MessageController
 * @Date：10 4月 2026  12:58
 * @Filename：MessageController
 */
@RequestMapping("/api/message")
@RestController
@Slf4j
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;
    /**
     * 用户向指定会话发送问题，后台异步调用 AI
     */
    @PostMapping
    public Result sendMessage(@RequestBody ChatMessage message) {
        log.info("发送问题");
        return Result.success("提问成功，AI 正在生成回答",messageService.sendMessage(message));
    }
    /**
     *  获取会话历史消息
     */
    @GetMapping("/session/{sessionId}")
    public Result<List<Message>> getMessages(@PathVariable Long sessionId) {
        log.info("获取会话历史消息");
        return Result.success(messageService.getMessagesBySessionId(sessionId));
    }

    /**
     * 根据ID查询消息
     */
    @GetMapping("/{id}")
    public Result getMessage(@PathVariable Long id) {
        log.info("根据ID查询消息");
        return Result.success(messageService.getById(id));
    }
}
