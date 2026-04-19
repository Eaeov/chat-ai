package com.zyj.chatai.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户会话表实体类
 * @Author：zyj
 * @Package：com.zyj.chatai.pojo
 * @Project：chat-ai
 * @name：Session
 * @Date：10 4月 2026  12:57
 * @Filename：Session
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("session")
public class Session {
    
    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 会话标题（可显示在前端）
     */
    private String title;
    
    /**
     * 会话创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
