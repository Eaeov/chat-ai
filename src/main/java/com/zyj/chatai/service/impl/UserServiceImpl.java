package com.zyj.chatai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyj.chatai.mapper.UserMapper;
import com.zyj.chatai.pojo.User;
import com.zyj.chatai.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.service.impl
 * @Project：chat-ai
 * @name：UserServiceImpl
 * @Date：10 4月 2026
 * @Filename：UserServiceImpl
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    /**
     * 注册用户
     *
     * @param user
     */
    @Override
    public void register(User user) {
        log.info("注册用户：{}", user);
        //检查用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User existUser = getOne(queryWrapper);
        if(existUser != null){
            throw new RuntimeException("用户名已存在");
        }
        //加密密码
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setCreatedAt(LocalDateTime.now());
        save(user);

    }

    /**
     * 登录用户
     */
    @Override
    public User login(String username, String password) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(username != null, User::getUsername, username);

        //解密
        User user = getOne(queryWrapper);
        if (user == null){
            throw new RuntimeException("用户名或密码错误");
        }
        if(!BCrypt.checkpw(password,user.getPassword())){
            throw new RuntimeException("用户名或密码错误");
        }
        //隐藏密码
        user.setPassword("*******");
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        return null;
    }
}
