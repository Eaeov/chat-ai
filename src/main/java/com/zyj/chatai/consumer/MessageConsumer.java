package com.zyj.chatai.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zyj.chatai.config.RabbitConfig;
import com.zyj.chatai.manager.SseSessionManager;
import com.zyj.chatai.mapper.MessageMapper;
import com.zyj.chatai.pojo.ChatMessage;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.listener
 * @Project：chat-ai
 * @name：MessageListener
 * @Date：13 4月 2026  09:33
 * @Filename：MessageListener
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConsumer {
    private final Map<String, ChatModel> chatModelMap;
    //private final ChatClient chatClient;
    private final SseSessionManager sseManager;
    private final MessageService messageService;
    private final MessageMapper messageMapper;
    private final SessionConsumer sessionConsumer;

    private ChatClient getChatClient(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            modelName = "qwen-plus";
        }

        ChatModel model = chatModelMap.get(modelName);

        if (model == null) {
            log.warn("未找到模型: {}，使用默认模型 qwen-plus", modelName);
            model = chatModelMap.getOrDefault("qwen-plus",
                    chatModelMap.values().stream().findFirst()
                            .orElseThrow(() -> new RuntimeException("没有可用的 ChatModel")));
        }

        return ChatClient.builder(model).build();
    }

    //监听队列并回复消息
    @RabbitListener(queues = RabbitConfig.CHAT_QUEUE)
    public void listen(ChatMessage chatMessage) {
        log.info("Received message: {}", chatMessage);

        Long userId = chatMessage.getUserId();
        String userInput = chatMessage.getContent();
        String modelName = chatMessage.getModel();

        ChatClient selectedClient = getChatClient(modelName);

        List<Message> historyList = getMessagesBySessionId(chatMessage.getSessionId());
        Prompt prompt = buildPrompt(historyList, userInput);

        try {
            StringBuilder aiFullAnswer = new StringBuilder();
            selectedClient.prompt(prompt)
                    .user(userInput)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        aiFullAnswer.append(chunk);
                        try {
                            sseManager.send(chatMessage.getSessionId().toString(), chunk);
                        } catch (Exception e) {
                            log.warn("SSE推送失败，用户{}已断开", userId);
                        }
                    })
                    .blockLast();

            Message aiMessage = new Message();
            aiMessage.setSessionId(chatMessage.getSessionId());
            aiMessage.setUserId(chatMessage.getUserId());
            aiMessage.setContent(aiFullAnswer.toString());
            aiMessage.setStatus(Message.Status.DONE.name());
            aiMessage.setRole(Message.Role.AI.name());
            aiMessage.setCreatedAt(LocalDateTime.now());
            messageService.saveAiMessage(aiMessage);

            LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Message::getId, chatMessage.getId())
                    .set(Message::getStatus, Message.Status.DONE.name());
            messageService.update(updateWrapper);

            sessionConsumer.generateSessionTitleIfNeeded(chatMessage.getSessionId(), userInput);
            log.info("AI回答保存成功，用户ID：{}，使用模型：{}", userId, modelName);
        } catch (Exception e) {
            log.error("AI处理异常", e);
            try {
                sseManager.send(chatMessage.getSessionId().toString(), "AI服务异常，请重试");
            } catch (Exception ignored) {}
        } finally {
            sseManager.complete(chatMessage.getSessionId().toString());
        }
    }


    /**
     * 根据会话查询最近5条消息
     */
    public List<Message> getMessagesBySessionId(Long sessionId) {
        log.info("根据会话ID查询消息：{}", sessionId);
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(sessionId != null , Message::getSessionId, sessionId)
                .eq(Message::getStatus, Message.Status.DONE.name())
                .orderByAsc(Message::getCreatedAt) // 按创建时间降序
                .last("LIMIT 5");
        return  messageMapper.selectList(queryWrapper);
    }

    /**
     * 构建提示词
     * @param historyList  最近5条历史消息（包含用户和AI的对话记录）
     * @param currentQuestion  当前用户问题
     * @return 提示词
     */
    private Prompt buildPrompt(List<Message> historyList, String currentQuestion) {
        String systemTemplate = """
            你是一个专业的智能聊天助手，严格遵守以下规则：
            1. 回答简洁明了、逻辑清晰、语气友好礼貌，杜绝冗余废话；
            2. 必须严格结合上下文历史对话，精准理解用户意图，不遗忘对话内容；
            3. 只回答用户当前问题相关内容，自动忽略无关信息，不主动拓展无关话题；
            4. 不清楚、不确定的问题直接告知，不编造答案、不产生幻觉；
            5. 回答格式简洁，分段清晰，不使用复杂排版；
            6. 拒绝回答违规、敏感、不道德的问题，坚守合规底线。
        """;

        // 创建系统提示消息
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemTemplate);
        org.springframework.ai.chat.messages.Message systemMsg = systemPromptTemplate.createMessage(Map.of());

        //存放AI所需的消息列表-》官方的Message对象
        List<org.springframework.ai.chat.messages.Message> msgList = new ArrayList<>();
        msgList.add(systemMsg);

        // 遍历 你自己的数据库Message → 转为 官方AI Message
        for (Message history : historyList){
            String content = history.getContent();
            if (Message.Role.USER.name().equals(history.getRole())) {
                msgList.add(new UserMessage(content)); // 用户消息
            } else if (Message.Role.AI.name().equals(history.getRole())) {
                msgList.add(new AssistantMessage(content)); // AI消息
            }
        }

        // 添加当前用户问题 → 官方 UserMessage
        msgList.add(new UserMessage(currentQuestion));

        return new Prompt(msgList);

    }


}
