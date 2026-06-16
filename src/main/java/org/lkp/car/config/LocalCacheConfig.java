package org.lkp.car.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.lkp.car.common.cache.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置类
 * <p>
 * 使用Caffeine作为本地缓存实现，用于高频访问数据的本地缓存
 * 与Redis分布式缓存配合使用，提升系统性能
 * </p>
 */
@Configuration
public class LocalCacheConfig {

    /**
     * 创建Caffeine本地缓存管理器
     * <p>
     * 配置缓存名称列表和缓存策略：
     * - 最大缓存条目数：2000
     * - 过期策略：写入后5分钟过期
     * - 记录缓存统计信息
     * </p>
     *
     * @return CaffeineCacheManager实例
     */
    @Bean("localCacheManager")
    public CacheManager localCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        // 配置缓存名称列表
        manager.setCacheNames(Arrays.asList(
                CacheConstants.LOCAL_SYS_USER,
                CacheConstants.LOCAL_ENTERPRISE,
                CacheConstants.LOCAL_SAFETY_OFFICER
        ));
        // 配置缓存策略
        manager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(2000)
                .expireAfterWrite(CacheConstants.LOCAL_TTL_MINUTES, TimeUnit.MINUTES)
                .recordStats());
        return manager;
    }
}
