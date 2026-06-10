package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.entity.SysUser;

import java.util.List;

/**
 * 用户与权限 服务类
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 获取所有民警用户
     * @return 民警用户列表
     */
    List<SysUser> getAllPoliceUsers();

    /**
     * 根据企业ID获取企业用户列表
     * @param enterpriseId 企业ID
     * @return 企业用户列表
     */
    List<SysUser> getEnterpriseUsers(Long enterpriseId);

    /**
     * 封禁用户举报权限
     * @param userId 用户ID
     * @param banHours 封禁时长（小时）
     * @param reason 封禁原因
     */
    void banUserReport(Long userId, int banHours, String reason);

    /**
     * 解封用户举报权限
     * @param userId 用户ID
     */
    void unbanUserReport(Long userId);

    /**
     * 检查用户是否被封禁
     * @param userId 用户ID
     * @return 是否被封禁
     */
    boolean isUserBanned(Long userId);

    /**
     * 增加用户总举报次数
     * @param userId 用户ID
     */
    void incrementTotalReportCount(Long userId);

    /**
     * 增加用户无效举报次数
     * @param userId 用户ID
     */
    void incrementInvalidReportCount(Long userId);
}
