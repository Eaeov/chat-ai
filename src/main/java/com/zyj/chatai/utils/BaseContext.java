package com.zyj.chatai.utils;

/**
 * @Author：zyj
 * @Package：com.zyj.chatai.utils
 * @Project：chat-ai
 * @name：BaseContext
 * @Date：11 4月 2026  12:28
 * @Filename：BaseContext
 */
public class BaseContext {
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId(){return threadLocal.get();}

    public static void removeCurrentId() {
        threadLocal.remove();
    }
}
