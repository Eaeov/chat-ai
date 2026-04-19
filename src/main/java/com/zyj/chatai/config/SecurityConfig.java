package com.zyj.chatai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.config
 * @Project：chat-ai
 * @name：SecurityConfig
 * @Date：11 4月 2026  11:27
 * @Filename：SecurityConfig
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http   // 关闭CSRF（前后端分离项目必须关，否则POST请求会被拦截）
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}