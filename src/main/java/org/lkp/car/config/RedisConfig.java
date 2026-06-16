package org.lkp.car.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.lkp.car.common.cache.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis配置类
 * <p>
 * 配置Redis的序列化方式和缓存管理器，支持不同缓存的独立TTL配置
 * </p>
 */
@Configuration
public class RedisConfig {

    /**
     * 创建RedisTemplate实例
     * <p>
     * 配置Key使用String序列化，Value使用JSON序列化
     * 支持Java 8日期时间类型的序列化
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        Jackson2JsonRedisSerializer<Object> serializer = jsonSerializer();
        // Key使用String序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        // Value使用JSON序列化
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建Redis缓存管理器
     * <p>
     * 配置不同缓存的TTL时间，通过@Primary注解标记为默认缓存管理器
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return CacheManager实例
     */
    @Bean("redisCacheManager")
    @Primary
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<Object> serializer = jsonSerializer();
        
        // 默认缓存配置：60秒过期
        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // 各缓存的独立TTL配置
        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        perCache.put(CacheConstants.REDIS_DASHBOARD_ENTERPRISE,
                defaults.entryTtl(Duration.ofSeconds(CacheConstants.REDIS_DASHBOARD_TTL_SECONDS)));
        perCache.put(CacheConstants.REDIS_DASHBOARD_ADMIN,
                defaults.entryTtl(Duration.ofSeconds(CacheConstants.REDIS_DASHBOARD_TTL_SECONDS)));
        perCache.put(CacheConstants.REDIS_DASHBOARD_CITIZEN,
                defaults.entryTtl(Duration.ofSeconds(CacheConstants.REDIS_DASHBOARD_TTL_SECONDS)));
        perCache.put(CacheConstants.REDIS_MSG_UNREAD,
                defaults.entryTtl(Duration.ofSeconds(CacheConstants.REDIS_MSG_UNREAD_TTL_SECONDS)));
        perCache.put(CacheConstants.REDIS_GEOCODE,
                defaults.entryTtl(Duration.ofHours(CacheConstants.REDIS_GEOCODE_TTL_HOURS)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaults)
                .withInitialCacheConfigurations(perCache)
                .transactionAware()
                .build();
    }

    /**
     * 创建JSON序列化器
     * <p>
     * 配置ObjectMapper支持Java 8日期时间类型和多态类型
     * </p>
     *
     * @return Jackson2JsonRedisSerializer实例
     */
    private Jackson2JsonRedisSerializer<Object> jsonSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        mapper.registerModule(new JavaTimeModule());
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(mapper);
        return serializer;
    }
}
