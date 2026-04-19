package com.zyj.chatai.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.properties
 * @Project：chat-ai
 * @name：JwtProperties
 * @Date：11 4月 2026  12:31
 * @Filename：JwtProperties
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    /**
     * 密钥
     */
    private String secret;
    /**
     * 过期时间
     */
    private long expire;
    /**
     * 令牌名称
     */
    private String tokenName;


}
