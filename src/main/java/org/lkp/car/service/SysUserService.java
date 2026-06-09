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
}
