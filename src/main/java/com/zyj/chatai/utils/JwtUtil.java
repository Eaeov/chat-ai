package com.zyj.chatai.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * 创建 JWT
     *
     * @param secretKey 密钥
     * @param ttlMillis 过期时间（毫秒）
     * @param claims 私有声明
     * @return 生成的 JWT
     */

    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // secretKey 密钥 ttlMillis 过期时间 claims 负载
        // 指定签名的时候使用的签名算法，也就是header那部分 // 选择签名算法，使用 HMAC SHA-256 算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        //设置过期时间
        long expMillis = System.currentTimeMillis() + ttlMillis;
        Date exp = new Date(expMillis); // // 将毫秒转化为 Date 对象
        String jwt = Jwts.builder()
                .setClaims(claims) //设置私有声明
                .setIssuedAt(new Date()) // 设置签发时间
                .setExpiration(exp) // 设置过期时间
                .signWith(signatureAlgorithm, secretKey.getBytes(StandardCharsets.UTF_8)) // 设置签名和密钥
                .compact();
        return jwt;
    }

    /**
     * 验证 JWT
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 解析JWT并验证签名，获取JWT的Claims部分
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))  // ① 设置密钥
                    .build()                                                    // ② 构建解析器
                    .parseClaimsJws(token)                                     // ③ 解析并验证签名
                    .getBody();                                                // ④ 获取载荷数据
            /**
             * 详细过程：
             * ① 设置签名密钥
             * 使用相同的密钥（secretKey）来验证签名
             * 密钥必须与签发时使用的密钥完全一致
             * ② 构建解析器
             * 创建 JWT 解析器实例
             * 准备好验证环境
             * ③ 核心验证过程 当执行 parseClaimsJws(token) 时，系统会：
             * 解析 JWT 结构：将 token 分解为 header.payload.signature 三部分
             * 验证签名：
             * 用 header 中指定的算法（如 HS256）
             * 用你提供的密钥重新计算签名
             * 对比计算出的签名与 token 中的签名是否一致
             * 检查有效期：验证 exp（过期时间）是否已过期
             * 检查生效时间：验证 nbf（not before）是否已到达
             * ④ 获取载荷
             * 验证通过后，返回 Claims 对象
             * 包含 JWT 中存储的所有信息（用户ID、用户名等）
             */
        } catch (Exception e) {
            // 解析失败或签名验证失败的情况
            throw new RuntimeException("Invalid JWT token", e);
        }
    }


}
