package com.zyj.chatai.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 * @Author：zyj
 * @Package：com.zyj.chatai.pojo
 * @Project：chat-ai
 * @name：User
 * @Date：10 4月 2026  12:57
 * @Filename：User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {
    
    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 登录名
     */
    private String username;
    
    /**
     * 加密密码
     */
    private String password;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
