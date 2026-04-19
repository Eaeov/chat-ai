package com.zyj.chatai.config;

import opennlp.tools.ml.maxent.io.BinaryGISModelReader;
import org.springframework.amqp.support.converter.ClassMapper;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;  // ✅ 正确
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.config
 * @Project：chat-ai
 * @name：RabbitConfig
 * @Date：12 4月 2026  13:11
 * @Filename：RabbitConfig
 */
@Configuration
public class RabbitConfig {

    public static final String CHAT_ROUTING_KEY = "chat.message";
    public static final String CHAT_QUEUE = "chat.queue";
    public static final String CHAT_EXCHANGE = "chat.exchange";
    public static final String CHAT_SESSION = "chat.session";

    // 死信队列相关配置
    public static final String ERROR_EXCHANGE = "error.exchange";
    public static final String ERROR_QUEUE = "error.queue";
    public static final String ERROR_ROUTING_KEY = "chat.error";

    /**
     * 正常聊天队列（配置死信）
     */
    @Bean
    public Queue chatQueue() {
        Map<String, Object> args = new HashMap<>();
        // 设置死信交换机
        args.put("x-dead-letter-exchange", ERROR_EXCHANGE);
        // 设置死信路由键
        args.put("x-dead-letter-routing-key", ERROR_ROUTING_KEY);
        // 设置消息TTL为30秒（AI响应超时时间）
        args.put("x-message-ttl", 60000);

        return new Queue(CHAT_QUEUE, true, false, false, args);
    }

    /**
     * 死信交换机
     */
    @Bean
    public Exchange errorExchange() {
        return new TopicExchange(ERROR_EXCHANGE, true, false);
    }
    @Bean
    public Queue errorQueue() {
        return new Queue(ERROR_QUEUE, true);
    }

    /**
     * 绑定死信队列和死信交换机
     */
    @Bean
    public Binding errorBinding() {
        return BindingBuilder.bind(errorQueue())
                .to(errorExchange())
                .with(ERROR_ROUTING_KEY)
                .noargs();
    }

    /**
     *交换机
     */
    @Bean
    public Exchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE, true, false);
    }
    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding chatBinding() {
        return BindingBuilder.bind(chatQueue()) // 绑定队列
                .to(chatExchange()) // 绑定交换机
                .with("chat.#")// 使用路由键模式 "chat.#"
                .noargs(); // 不需要参数
    }




    /**
     * 配置JSON消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
