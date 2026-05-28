package org.lkp.car.common;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类 (基于 Hutool)
 */
@Component
public class JwtUtils {

    // 密钥 (请在生产环境中妥善保管)
    private static final byte[] KEY = "car-management-system-secret-key-2024".getBytes(StandardCharsets.UTF_8);
    
    // 过期时间: 7天
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    /**
     * 生成 Token
     * @param userId 用户ID
     * @return token
     */
    public String createToken(Long userId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("userId", userId);
        payload.put("ts", System.currentTimeMillis());
        // 过期时间
        payload.put(JWT.EXPIRES_AT, new Date(System.currentTimeMillis() + EXPIRE_TIME));
        
        return JWTUtil.createToken(payload, KEY);
    }

    /**
     * 解析并验证 Token
     * @param token token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            if (!JWTUtil.verify(token, KEY)) {
                return false;
            }
            JWT jwt = JWTUtil.parseToken(token);
            Object expiresAt = jwt.getPayload(JWT.EXPIRES_AT);
            if (expiresAt instanceof Date) {
                return ((Date) expiresAt).after(new Date());
            }
            return expiresAt != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从 Token 中获取用户ID
     * @param token token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        JWT jwt = JWTUtil.parseToken(token);
        return Long.parseLong(jwt.getPayload("userId").toString());
    }
}
