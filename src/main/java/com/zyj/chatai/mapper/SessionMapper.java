package com.zyj.chatai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyj.chatai.pojo.Session;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.mapper
 * @Project：chat-ai
 * @name：SessionMapper
 * @Date：10 4月 2026
 * @Filename：SessionMapper
 */
@Mapper
public interface SessionMapper extends BaseMapper<Session> {
}
