package com.zyj.chatai.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.pojo
 * @Project：chat-ai
 * @name：UserVo
 * @Date：10 4月 2026  20:34
 * @Filename：UserVo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder//使用Builder模式，方便构建对象
public class UserVo implements Serializable {
    /**
     * 用户id
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * jwt
     */
    private String jwt;

}
