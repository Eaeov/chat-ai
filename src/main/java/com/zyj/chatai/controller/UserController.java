package com.zyj.chatai.controller;

import com.zyj.chatai.pojo.User;
import com.zyj.chatai.pojo.UserVo;
import com.zyj.chatai.properties.JwtProperties;
import com.zyj.chatai.service.UserService;
import com.zyj.chatai.utils.JwtUtil;
import com.zyj.chatai.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.controller
 * @Project：chat-ai
 * @name：UserController
 * @Date：10 4月 2026  12:58
 * @Filename：UserController
 */
@RequestMapping("/user")
@RestController
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProperties jwtProperties;

    /**
     * 登入接口
     */
    @PostMapping("/login")
    public Result<UserVo> login(String  username, String password){
        log.info("用户登录：{}，{}", username, password);
        //查询用户信息
        User user = userService.login(username, password);
        if (user == null){
            return Result.error("用户名或密码错误");
        }
        //创建Jwt令牌


        Map<String,Object> claims = new HashMap<>();
        claims.put("userId",user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getSecret(), jwtProperties.getExpire(), claims);
        log.info("生成的令牌：{}", token);
        UserVo userVo = UserVo.builder()
                .jwt(token)
                .id(user.getId())
                .password(user.getPassword())
                .username(user.getUsername())
                .build();
        return Result.success(userVo);
    }

    /**
     * 注册接口
     */
    @GetMapping("/register")
    public Result<User> register(User user){
        log.info("注册用户：{}", user);
        userService.register(user);
        return Result.success(user);
    }

}
