package com.zyj.chatai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zyj.chatai.pojo.Session;

import java.util.List;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.service
 * @Project：chat-ai
 * @name：SessionService
 * @Date：10 4月 2026
 * @Filename：SessionService
 */
public interface SessionService extends IService<Session> {
    
    /**
     * 创建会话
     */
    Session createSession(Session session);
    
    /**
     * 根据用户ID查询会话列表
     */
    List<Session> getSessionsByUserId(Long userId);
    
    /**
     * 更新会话标题
     */
    boolean updateSessionTitle(Long sessionId, String title);

    /**
     * 删除会话
     */
    boolean deleteSession(Long id);

    /**
     * 清除指定用户的会话列表缓存
     */
    void clearUserSessionCache(Long userId);
}
