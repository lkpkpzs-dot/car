package org.lkp.car.common.interceptor;

import org.lkp.car.common.JwtUtils;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import cn.hutool.json.JSONUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 认证拦截器
 * <p>
 * 负责在请求到达Controller之前进行身份验证和权限检查：
 * 1. 验证JWT Token的有效性
 * 2. 检查Token类型（拒绝使用refresh token访问接口）
 * 3. 根据@RequireRole注解进行角色权限校验
 * 4. 将当前用户信息存入request，供后续业务使用
 * </p>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private SysUserService sysUserService;

    /**
     * 请求前置处理方法
     * <p>
     * 执行流程：
     * 1. 放行OPTIONS预检请求
     * 2. 从请求头获取Authorization Token
     * 3. 验证Token格式和有效性
     * 4. 检查Token类型（必须是access token）
     * 5. 根据Token获取用户信息
     * 6. 检查@RequireRole注解的角色权限
     * 7. 将用户信息存入request供后续使用
     * </p>
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  处理器对象（可能是HandlerMethod或ResourceHttpRequestHandler）
     * @return true-继续执行，false-拦截并返回错误
     * @throws Exception 处理过程中的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行OPTIONS预检请求
        if (isOptionsRequest(request)) {
            return true;
        }

        // 从请求头获取Authorization Token
        String token = extractToken(request);
        if (token == null) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        // 验证Token有效性
        if (!jwtUtils.validateToken(token)) {
            writeUnauthorized(response, "登录已过期，请重新登录");
            return false;
        }

        // 检查Token类型，拒绝使用refresh token访问接口
        if (jwtUtils.isRefreshToken(token)) {
            writeForbidden(response, "刷新令牌不能用于访问接口");
            return false;
        }

        // 根据Token获取用户信息
        SysUser user = getUserByToken(token);
        if (user == null) {
            writeUnauthorized(response, "用户不存在，请重新登录");
            return false;
        }

        // 将用户信息存入request，供后续业务使用
        request.setAttribute("currentUser", user);

        // 如果是HandlerMethod，检查是否有@RequireRole注解
        return checkRolePermission(handler, user, response);
    }

    /**
     * 判断是否为OPTIONS预检请求
     */
    private boolean isOptionsRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    /**
     * 从请求头提取Token
     * @return Token字符串，如果格式不正确返回null
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            return null;
        }
        return token.substring(7);
    }

    /**
     * 根据Token获取用户信息
     */
    private SysUser getUserByToken(String token) {
        Long userId = jwtUtils.getUserId(token);
        return sysUserService.getById(userId);
    }

    /**
     * 检查角色权限
     * @return true-有权限，false-无权限
     */
    private boolean checkRolePermission(Object handler, SysUser user, HttpServletResponse response) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        if (!method.isAnnotationPresent(RequireRole.class)) {
            return true;
        }

        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        return hasRequiredRole(user, requireRole, response);
    }

    /**
     * 判断用户是否具有所需角色权限
     * @return true-有权限，false-无权限
     */
    private boolean hasRequiredRole(SysUser user, RequireRole requireRole, HttpServletResponse response) throws Exception {
        int[] allowedRoles = requireRole.value();
        Integer userRole = user.getRoleType();
        
        // 首先检查角色类型是否直接匹配
        if (userRole != null && Arrays.stream(allowedRoles).anyMatch(r -> r == userRole)) {
            return true;
        }
        
        // 特殊处理：如果需要企业角色，且用户有authEnterpriseId，也视为有企业权限
        if (Arrays.stream(allowedRoles).anyMatch(r -> r == RoleEnum.ENTERPRISE_CODE) 
            && user.getAuthEnterpriseId() != null) {
            return true;
        }
        
        writeForbidden(response, "无权限访问该接口");
        return false;
    }

    /**
     * 写入未授权响应
     *
     * @param response HTTP响应对象
     * @param message  错误消息
     * @throws Exception IO异常
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(401, message)));
    }

    /**
     * 写入禁止访问响应
     *
     * @param response HTTP响应对象
     * @param message  错误消息
     * @throws Exception IO异常
     */
    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(403, message)));
    }
}
