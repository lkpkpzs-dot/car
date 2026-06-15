package org.lkp.car.common;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import org.lkp.car.config.JwtConfig;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String PAYLOAD_USER_ID = "userId";
    private static final String PAYLOAD_TYPE = "type";
    private static final String PAYLOAD_TIMESTAMP = "ts";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Autowired
    private JwtConfig jwtConfig;

    /**
     * 生成 Access Token (2小时过期)
     * @param userId 用户ID
     * @return token
     */
    public String createAccessToken(Long userId) {
        Map<String, Object> payload = buildTokenPayload(userId, TOKEN_TYPE_ACCESS);
        payload.put(JWT.EXPIRES_AT, new Date(System.currentTimeMillis() + jwtConfig.getAccessTokenExpireTime()));
        return JWTUtil.createToken(payload, jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Refresh Token (7天过期)
     * @param userId 用户ID
     * @return token
     */
    public String createRefreshToken(Long userId) {
        Map<String, Object> payload = buildTokenPayload(userId, TOKEN_TYPE_REFRESH);
        payload.put(JWT.EXPIRES_AT, new Date(System.currentTimeMillis() + jwtConfig.getRefreshTokenExpireTime()));
        return JWTUtil.createToken(payload, jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 构建 Token Payload
     * @param userId 用户ID
     * @param tokenType token类型
     * @return payload map
     */
    private Map<String, Object> buildTokenPayload(Long userId, String tokenType) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(PAYLOAD_USER_ID, userId);
        payload.put(PAYLOAD_TYPE, tokenType);
        payload.put(PAYLOAD_TIMESTAMP, System.currentTimeMillis());
        return payload;
    }

    /**
     * 解析并验证 Token
     * @param token token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            if (!JWTUtil.verify(token, jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8))) {
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
        return Long.parseLong(jwt.getPayload(PAYLOAD_USER_ID).toString());
    }

    /**
     * 从 Token 中获取类型
     * @param token token
     * @return token 类型
     */
    public String getTokenType(String token) {
        try {
            JWT jwt = JWTUtil.parseToken(token);
            Object type = jwt.getPayload(PAYLOAD_TYPE);
            return type != null ? type.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查是否为 Refresh Token
     * @param token token
     * @return 是否为 Refresh Token
     */
    public boolean isRefreshToken(String token) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(token));
    }
}
