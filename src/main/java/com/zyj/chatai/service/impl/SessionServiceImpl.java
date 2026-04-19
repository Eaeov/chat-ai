package com.zyj.chatai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zyj.chatai.mapper.SessionMapper;
import com.zyj.chatai.pojo.Message;
import com.zyj.chatai.pojo.Session;
import com.zyj.chatai.service.MessageService;
import com.zyj.chatai.service.SessionService;
import com.zyj.chatai.utils.BaseContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 会话服务实现类
 * 负责会话的创建、查询、更新和删除，并集成 Redis 缓存优化查询性能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {
    private final MessageService messageService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis 缓存 Key 前缀：session:user:{userId}
     */
    private static final String SESSION_CACHE_KEY_PREFIX = "session:user:";

    /**
     * 缓存过期时间：24小时
     */
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 创建新会话
     * 1. 从 ThreadLocal 获取当前登录用户ID
     * 2. 设置会话的用户ID和创建时间
     * 3. 保存到数据库
     * 4. 清除该用户的会话列表缓存（保证数据一致性）
     *
     * @param session 会话对象（需包含标题等信息）
     * @return 创建成功的会话对象（包含自动生成的ID）
     */
    @Override
    public Session createSession(Session session) {
        // 获取当前登录用户ID
        Long userId = BaseContext.getCurrentId();
        session.setUserId(userId);
        session.setCreatedAt(LocalDateTime.now());

        // 保存到数据库
        save(session);

        // 清除缓存，确保下次查询能获取最新数据
        clearUserSessionCache(userId);

        return session;
    }

    /**
     * 根据用户ID查询会话列表（带 Redis 缓存）
     * 缓存策略：
     * 1. 优先从 Redis 读取缓存
     * 2. 缓存未命中则查询数据库，并将结果写入 Redis
     * 3. 设置 24 小时过期时间，避免缓存雪崩
     * 4. Redis 异常时自动降级为数据库查询
     *
     * @param userId 用户ID
     * @return 会话列表（按创建时间倒序排列）
     */
    @Override
    public List<Session> getSessionsByUserId(Long userId) {
        log.info("获得会话列表ID：{}", userId);

        // 参数校验
        if (userId == null) {
            return List.of();
        }

        String key = SESSION_CACHE_KEY_PREFIX + userId;

        try {
            // 尝试从 Redis 获取缓存
            String cachedData = stringRedisTemplate.opsForValue().get(key);
            if (cachedData != null && !cachedData.isEmpty()) {
                log.info("从Redis缓存获取会话列表，用户ID：{}", userId);
                // JSON 反序列化为 List<Session>
                return objectMapper.readValue(cachedData, new TypeReference<List<Session>>() {});
            }
        } catch (Exception e) {
            // Redis 读取失败，记录警告日志，降级查数据库
            log.warn("Redis读取失败，降级查询数据库", e);
        }

        // 缓存未命中，查询数据库
        LambdaQueryWrapper<Session> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Session::getUserId, userId)
                .orderByDesc(Session::getCreatedAt); // 按创建时间倒序
        List<Session> sessions = list(queryWrapper);

        // 将查询结果写入 Redis 缓存
        if (sessions != null && !sessions.isEmpty()) {
            try {
                String jsonData = objectMapper.writeValueAsString(sessions);
                stringRedisTemplate.opsForValue().set(key, jsonData, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                log.info("会话列表已缓存到Redis，用户ID：{}，数量：{}", userId, sessions.size());
            } catch (Exception e) {
                // Redis 写入失败不影响业务，仅记录错误日志
                log.error("Redis写入失败", e);
            }
        }

        return sessions != null ? sessions : List.of();
    }

    /**
     * 更新会话标题
     * 1. 根据会话ID更新标题
     * 2. 更新成功后清除对应用户的会话列表缓存
     *
     * @param sessionId 会话ID
     * @param title 新标题
     * @return 是否更新成功
     */
    @Override
    public boolean updateSessionTitle(Long sessionId, String title) {
        // 构建更新条件
        LambdaUpdateWrapper<Session> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Session::getId, sessionId);

        // 执行更新
        boolean result = update(updateWrapper.set(Session::getTitle, title));

        // 更新成功后清除缓存
        if (result) {
            Long userId = BaseContext.getCurrentId();
            if (userId != null) {
                clearUserSessionCache(userId);
            }
        }

        return result;
    }

    /**
     * 删除会话（事务保证数据一致性）
     * 1. 先查询会话信息，获取 userId 用于清除缓存
     * 2. 删除该会话下的所有消息（级联删除）
     * 3. 删除会话本身
     * 4. 清除该用户的会话列表缓存
     *
     * @param id 会话ID
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSession(Long id) {
        // 先查询会话，获取 userId
        Session session = getById(id);
        if (session == null) {
            return false;
        }

        // 删除关联的消息
        messageService.remove(new LambdaQueryWrapper<Message>().eq(Message::getSessionId, id));

        // 删除会话
        boolean result = removeById(id);

        // 删除成功后清除缓存
        if (result) {
            clearUserSessionCache(session.getUserId());
        }

        return result;
    }

    /**
     * 清除指定用户的会话列表缓存
     *
     * @param userId 用户ID
     */
    private void clearUserSessionCache(Long userId) {
        if (userId != null) {
            String key = SESSION_CACHE_KEY_PREFIX + userId;
            stringRedisTemplate.delete(key);
            log.info("已清除用户会话缓存，用户ID：{}", userId);
        }
    }
}
