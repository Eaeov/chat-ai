package com.zyj.chatai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.chatai.pojo.User;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.service
 * @Project：chat-ai
 * @name：UserService
 * @Date：10 4月 2026
 * @Filename：UserService
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     */
    void register(User user);
    
    /**
     * 用户登录
     */
    User login(String username, String password);
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);


}
