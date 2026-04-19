package com.zyj.chatai.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息/问答记录表实体类
 * @Author：zyj
 * @Package：com.zyj.chatai.pojo
 * @Project：chat-ai
 * @name：Message
 * @Date：10 4月 2026  12:57
 * @Filename：Message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("message")
@Builder
public class Message implements Serializable {

    //status枚举
    public enum Status {
        PENDING, DONE, ERROR; // 待处理、处理完成、处理错误
    }
    //role
    public enum Role {
        USER, AI; // 用户、AI
    }
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 用户ID，用于区分问答角色（用户/AI）
     */
    private Long userId;
    
    /**
     * 角色：USER / AI
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 处理状态(PENDING/DONE/ERROR)
     */
    private String status;
    
    /**
     * 消息创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
