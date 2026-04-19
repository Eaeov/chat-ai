package com.zyj.chatai.controller;

import com.zyj.chatai.pojo.Session;
import com.zyj.chatai.service.SessionService;
import com.zyj.chatai.utils.BaseContext;
import com.zyj.chatai.utils.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.controller
 * @Project：chat-ai
 * @name：SessionController
 * @Date：10 4月 2026  20:32
 * @Filename：SessionController
 */
@RequestMapping("/api/session")
@RestController
@Slf4j
@RequiredArgsConstructor
public class SessionController {
    private final SessionService sessionService;
    /**
     * 创建会话
     * 先创建会话再发送消息
     */
    @PostMapping
    public Result createSession(@RequestBody Session title) {
        log.info("创建会话：{}", title);
        Session session = sessionService.createSession(title);
        return Result.success("会话创建成功",session);
    }
    /**
     * 获得会话列表
     */
    @GetMapping
    public Result<List<Session>> getSessions() {
        log.info("获得会话列表");
        List<Session> sessions = sessionService.getSessionsByUserId(BaseContext.getCurrentId());
        return Result.success("查询成功",sessions);
    }

    /**
     * 修改会话标题
     */
    @PutMapping
    public Result updateSessionTitle(@RequestBody Session session) {
        log.info("修改会话标题：{}", session);
        boolean b = sessionService.updateSessionTitle(session.getId(), session.getTitle());
        if (!b){
            return Result.error("会话标题修改失败");
        }
        return Result.success("会话标题修改成功");
    }

    /**
     * 删除会话
     */
    @DeleteMapping
    public Result deleteSession(@PathVariable Long id) {
        log.info("删除会话：{}", id);
        boolean b = sessionService.deleteSession(id);
        if (!b){
            return Result.error("会话删除失败");
        }
        return Result.success("会话删除成功");
    }


}
