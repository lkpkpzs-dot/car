package org.lkp.car.common.annotation;

import java.lang.annotation.*;

/**
 * 角色权限注解
 * <p>
 * 用于标注Controller方法需要特定角色才能访问
 * 配合AuthInterceptor拦截器使用，在请求到达前进行权限校验
 * </p>
 * <p>
 * 使用示例：
 * <pre>
 *     @RequireRole({RoleEnum.POLICE_CODE})
 *     public Result<List<AuditTaskVO>> getAuditList(...) { ... }
 *     
 *     @RequireRole({RoleEnum.ENTERPRISE_CODE})
 *     public Result<VehicleInfo> submitVehicleApply(...) { ... }
 * </pre>
 * </p>
 * <p>
 * 支持的角色编码参考{@link org.lkp.car.common.enums.RoleEnum}：
 * - RoleEnum.POLICE_CODE (1): 交警/车管所
 * - RoleEnum.ENTERPRISE_CODE (2): 企业代办人
 * - RoleEnum.CITIZEN_CODE (3): 普通市民
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    
    /**
     * 允许访问的角色编码数组
     * 参考 RoleEnum 中的常量定义
     */
    int[] value();
}
