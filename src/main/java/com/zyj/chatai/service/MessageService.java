package com.zyj.chatai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.chatai.pojo.ChatMessage;
import com.zyj.chatai.pojo.Message;

import java.util.List;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.service
 * @Project：chat-ai
 * @name：MessageService
 * @Date：10 4月 2026
 * @Filename：MessageService
 */
public interface MessageService extends IService<Message> {
    
    /**
     * 发送消息
     */
    Message sendMessage(ChatMessage message);
    
    /**
     * 根据会话ID查询消息列表
     */
    List<Message> getMessagesBySessionId(Long sessionId);

    /**
     * 保存AI消息
     */
    void saveAiMessage(Message aiAnswer);
}
