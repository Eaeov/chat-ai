package com.zyj.chatai.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.pojo
 * @Project：chat-ai
 * @name：ChatMessage
 * @Date：19 4月 2026  09:12
 * @Filename：ChatMessage
 */

@Data
@EqualsAndHashCode(callSuper = true) // 继承父类的equals和hashCode方法
public class ChatMessage extends Message {
    private String model; // 模型名称，不存入数据库
}
