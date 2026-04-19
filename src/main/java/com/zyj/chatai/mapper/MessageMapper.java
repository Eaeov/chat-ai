package com.zyj.chatai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyj.chatai.pojo.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.mapper
 * @Project：chat-ai
 * @name：MessageMapper
 * @Date：10 4月 2026
 * @Filename：MessageMapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
