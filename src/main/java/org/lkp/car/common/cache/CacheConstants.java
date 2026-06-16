package org.lkp.car.common.cache;

/**
 * 缓存命名空间与 TTL 常量类
 * <p>
 * 定义系统中所有缓存的键名前缀和过期时间配置
 * 本地缓存使用 Caffeine，分布式缓存使用 Redis
 * </p>
 */
public final class CacheConstants {

    private CacheConstants() {
        // 私有构造函数，防止实例化
    }

    // ==================== 本地缓存（Caffeine）====================
    
    /**
     * 用户信息本地缓存
     * 用于高频访问的用户数据缓存，减少数据库查询
     */
    public static final String LOCAL_SYS_USER = "local:sysUser";
    
    /**
     * 企业信息本地缓存
     * 用于高频访问的企业数据缓存
     */
    public static final String LOCAL_ENTERPRISE = "local:enterpriseInfo";
    
    /**
     * 安全员信息本地缓存
     * 用于高频访问的安全员数据缓存
     */
    public static final String LOCAL_SAFETY_OFFICER = "local:safetyOfficer";

    // ==================== Redis 分布式缓存 ====================
    
    /**
     * 企业仪表盘数据缓存
     * 缓存企业端首页统计数据
     */
    public static final String REDIS_DASHBOARD_ENTERPRISE = "redis:dashboard:enterprise";
    
    /**
     * 管理员仪表盘数据缓存
     * 缓存管理员端首页统计数据
     */
    public static final String REDIS_DASHBOARD_ADMIN = "redis:dashboard:admin";
    
    /**
     * 市民仪表盘数据缓存
     * 缓存市民端首页统计数据
     */
    public static final String REDIS_DASHBOARD_CITIZEN = "redis:dashboard:citizen";
    
    /**
     * 未读消息缓存
     * 缓存用户未读消息数量
     */
    public static final String REDIS_MSG_UNREAD = "redis:msg:unread";
    
    /**
     * 地理编码缓存
     * 缓存逆地理编码查询结果，减少第三方API调用
     */
    public static final String REDIS_GEOCODE = "redis:geocode";

    // ==================== TTL 配置（过期时间）===================
    
    /**
     * 本地缓存默认过期时间（分钟）
     * 本地缓存用于高频访问但不要求强一致性的数据
     */
    public static final long LOCAL_TTL_MINUTES = 5;
    
    /**
     * 仪表盘数据缓存过期时间（秒）
     * 仪表盘数据实时性要求较高，设置较短过期时间
     */
    public static final long REDIS_DASHBOARD_TTL_SECONDS = 45;
    
    /**
     * 未读消息缓存过期时间（秒）
     * 消息数量需要及时更新，设置较短过期时间
     */
    public static final long REDIS_MSG_UNREAD_TTL_SECONDS = 30;
    
    /**
     * 地理编码缓存过期时间（小时）
     * 地理编码数据相对稳定，设置较长过期时间
     */
    public static final long REDIS_GEOCODE_TTL_HOURS = 24;
    
    /**
     * 短期缓存过期时间（秒）
     * 适合临时缓存场景，如表单验证、临时计算结果等
     */
    public static final long REDIS_SHORT_TTL_SECONDS = 120;
}
