package com.zyj.chatai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zyj.chatai.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.mapper
 * @Project：chat-ai
 * @name：UserMapper
 * @Date：10 4月 2026
 * @Filename：UserMapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
