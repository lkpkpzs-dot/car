package org.lkp.car.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置类
 * <p>
 * 通过@ConfigurationProperties注解从application.yml中读取jwt前缀的配置项
 * 用于配置JWT Token的生成和验证参数
 * </p>
 * <p>
 * 配置示例（application.yml）：
 * <pre>
 * jwt:
 *   secret: your-256-bit-secret-key-here
 *   access-token-expire-time: 7200000    # 2小时（毫秒）
 *   refresh-token-expire-time: 604800000 # 7天（毫秒）
 * </pre>
 * </p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    
    /**
     * JWT签名密钥
     * 建议使用至少256位的随机字符串，用于Token的签名和验证
     */
    private String secret;
    
    /**
     * Access Token过期时间（毫秒）
     * 建议设置为2小时（7200000毫秒）
     */
    private long accessTokenExpireTime;
    
    /**
     * Refresh Token过期时间（毫秒）
     * 建议设置为7天（604800000毫秒）
     */
    private long refreshTokenExpireTime;
}
