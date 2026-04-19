package com.zyj.chatai;


import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;

@SpringBootApplication
@EnableRabbit // 启用RabbitMQ
//@MapperScan("com.zyj.chatai.mapper")
public class ChatAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatAiApplication.class, args);
    }

}
