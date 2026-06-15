package org.lkp.car.utils;

import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.SysUser;

import javax.servlet.http.HttpServletRequest;

/**
 * 登录用户上下文工具类
 * 用于从 request 中获取当前登录用户，以及做一些权限判断
 */
public class AuthContext {

    /**
     * 获取当前登录用户
     * 前提：AuthInterceptor 已经把用户信息放入 request
     * request.setAttribute("currentUser", user)
     */
    public static SysUser currentUser(HttpServletRequest request) {
        return (SysUser) request.getAttribute("currentUser");
    }

    /**
     * 判断是否为民警角色
     */
    public static boolean isPolice(SysUser user) {
        return user != null
                && user.getRoleType() != null
                && user.getRoleType() == RoleEnum.POLICE_CODE;
    }

    /**
     * 判断用户是否已绑定企业
     * 用于控制企业相关功能权限（比如提交道路申请）
     */
    public static boolean hasEnterprise(SysUser user) {
        return user != null
                && user.getAuthEnterpriseId() != null;
    }
}