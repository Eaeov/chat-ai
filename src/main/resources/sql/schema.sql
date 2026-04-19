-- AI 聊天系统数据库建表脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_chat;

-- 用户表
CREATE TABLE user
(
    id         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '用户ID' PRIMARY KEY,
    username   VARCHAR(50)                        NOT NULL COMMENT '登录名',
    password   VARCHAR(255)                       NOT NULL COMMENT '加密密码',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
    CONSTRAINT idx_username UNIQUE (username)
) COMMENT = '用户表';

-- 用户会话表
CREATE TABLE session
(
    id         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '会话ID' PRIMARY KEY,
    user_id    BIGINT UNSIGNED                    NOT NULL COMMENT '用户ID',
    title      VARCHAR(100)                       NOT NULL COMMENT '会话标题（可显示在前端）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '会话创建时间',
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) COMMENT = '用户会话表';

-- 消息/问答记录表
CREATE TABLE message
(
    id         BIGINT UNSIGNED AUTO_INCREMENT COMMENT '消息ID' PRIMARY KEY,
    session_id BIGINT UNSIGNED                       NOT NULL COMMENT '会话ID',
    user_id    BIGINT UNSIGNED                       NOT NULL COMMENT '用户ID，用于区分问答角色（用户/AI）',
    role       VARCHAR(10)                           NOT NULL COMMENT '角色：USER / AI',
    content    TEXT                                  NOT NULL COMMENT '消息内容',
    status     VARCHAR(20) DEFAULT 'DONE'            NOT NULL COMMENT '处理状态(PENDING/DONE/ERROR)',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '消息创建时间',
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES session (id) ON DELETE CASCADE,
    CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE,
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) COMMENT = '消息/问答记录表';
