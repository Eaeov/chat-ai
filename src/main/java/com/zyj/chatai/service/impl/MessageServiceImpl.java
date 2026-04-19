package com.zyj.chatai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.chatai.config.RabbitConfig;
import com.zyj.chatai.mapper.MessageMapper;
import com.zyj.chatai.pojo.ChatMessage;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.service.MessageService;
import com.zyj.chatai.utils.BaseContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.service.impl
 * @Project：chat-ai
 * @name：MessageServiceImpl
 * @Date：10 4月 2026
 * @Filename：MessageServiceImpl
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     */
    @Override
    public Message sendMessage(ChatMessage message) {
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new RuntimeException("消息内容不能为空");
        }
        if (message.getSessionId() == null) {
            throw new RuntimeException("会话ID不能为空");
        }

        message.setCreatedAt(LocalDateTime.now());
        message.setStatus(Message.Status.PENDING.name());
        message.setRole(Message.Role.USER.name());
        message.setUserId(BaseContext.getCurrentId());

        save(message);
        log.info("用户消息保存成功，ID：{}，内容：{}", message.getId(), message.getContent());

        rabbitTemplate.convertAndSend(RabbitConfig.CHAT_EXCHANGE, RabbitConfig.CHAT_ROUTING_KEY, message);

        Message result = new Message();
        BeanUtil.copyProperties(message, result);
        return result;
    }

    /**
     * 根据会话ID获取消息列表
     */
    @Override
    public List<Message> getMessagesBySessionId(Long sessionId) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(sessionId!=null,Message::getSessionId, sessionId);
        return list(queryWrapper);
    }

    @Override
    public void saveAiMessage(Message aiMessage) {
        log.info("保存 AI 消息：{}", aiMessage);
        save(aiMessage);
    }

}
