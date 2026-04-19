package com.zyj.chatai.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyj.chatai.config.RabbitConfig;
import com.zyj.chatai.manager.SseSessionManager;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.consumer
 * @Project：chat-ai
 * @name：ErrorConsumer
 * @Date：18 4月 2026  08:57
 * @Filename：ErrorConsumer
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ErrorConsumer {
    private final MessageService messageService;
    private final SseSessionManager sseManager;
    @RabbitListener(queues = RabbitConfig.ERROR_QUEUE)
    public void handleError(Message message) {
        log.error("⚠️ AI响应超时，消息ID：{}，会话ID：{}，用户ID：{}",
                message.getId(),
                message.getSessionId(),
                message.getUserId());

        try {
            LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Message::getId, message.getId())
                    .set(Message::getStatus, Message.Status.ERROR.name());
            messageService.update(updateWrapper);

            sseManager.send(message.getSessionId().toString(), "AI服务响应超时，请重试");

            log.info("超时消息处理完成，消息ID：{}", message.getId());
        } catch (Exception e) {
            log.error("处理超时消息失败，消息ID：{}", message.getId(), e);
        }
    }
}
