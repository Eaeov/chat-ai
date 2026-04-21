package com.zyj.chatai.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.pojo.Session;
import com.zyj.chatai.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.consumer
 * @Project：chat-ai
 * @name：SessionConsumer
 * @Date：13 4月 2026  17:11
 * @Filename：SessionConsumer
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SessionConsumer {
    private final Map<String, ChatModel> chatModelMap;
    //private final ChatClient chatClient;
    private final SessionService sessionService;
    /**
     * 根据用户发送的消息来设置会话标题。只处理第一次问题，后续问题不再修改会话标题。
     */
    /**
     * 如果需要则生成会话标题（只在会话没有标题时执行一次）   （如果==新会话就进行替换）
     */

    private ChatClient getChatClient() {
        // 默认使用 qwen-plus 来生成标题
        ChatModel model = chatModelMap.getOrDefault("qwen-plus",
                chatModelMap.values().stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("没有可用的 ChatModel")));
        return ChatClient.builder(model).build();
    }

    public void generateSessionTitleIfNeeded(Long sessionId, String firstUserMessage) {
        // 1. 检查会话是否已经有标题
        Session session = sessionService.getById(sessionId);
        if (session == null ) {
            return; // 已经有标题了，不需要生成
        }
        String currentTitle = session.getTitle();
        // 只有当标题为空，或者是前端默认的"新会话"时，才触发 AI 生成
        // 如果用户已经手动修改过标题（不是"新会话"），则不再覆盖
        if (currentTitle != null && !currentTitle.isEmpty() && !currentTitle.equals("新会话")) {
            return;
        }


        try {
            // 2. 调用 AI 生成简短标题
            String prompt = "请为以下用户对话生成一个极简短的标题（不超过10个字），只返回标题内容，不要加引号或其他解释：\n" + firstUserMessage;

            String title = getChatClient().prompt(prompt)
                    .call()
                    .content();

            // 3. 更新数据库
            if (title != null && !title.isEmpty()) {
                LambdaUpdateWrapper<Session> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Session::getId, sessionId)
                        .set(Session::getTitle, title.length() > 20 ? title.substring(0, 20) : title);
                sessionService.update(updateWrapper);
                log.info("会话 {} 标题自动生成成功: {}", sessionId, title);
                //删除redis中的会话列表
                sessionService.clearUserSessionCache(session.getUserId());

            }
        } catch (Exception e) {
            log.error("生成会话标题失败", e);
        }
    }

}
