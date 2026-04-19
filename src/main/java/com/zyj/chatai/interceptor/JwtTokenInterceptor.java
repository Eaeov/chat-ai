package com.zyj.chatai.interceptor;

import com.zyj.chatai.properties.JwtProperties;
import com.zyj.chatai.utils.BaseContext;
import com.zyj.chatai.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;
/**
 * @Author：zyj
 * @Package：com.zyj.chatai.interceptor
 * @Project：chat-ai
 * @name：JwtTokenInterceptor
 * @Date：11 4月 2026  12:55
 * @Filename：JwtTokenInterceptor
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenInterceptor implements HandlerInterceptor {
    private final JwtProperties jwtProperties;

    /**
     * 校验JWT
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截得是否是controller方法
        if(!(handler instanceof HandlerMethod)){
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //校验jwt
        String token = request.getHeader(jwtProperties.getTokenName());
        if(token == null || token.isEmpty()){
            log.info("token为空");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecret(), token);

            Long userId = Long.valueOf(claims.get("userId").toString());
            // 存入
            BaseContext.setCurrentId(userId);
            log.info("当前用户ID：{}",userId);
            return true;
        }catch (Exception ex){
            //401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal，防止内存泄漏和数据混乱
        BaseContext.removeCurrentId();
    }
}
