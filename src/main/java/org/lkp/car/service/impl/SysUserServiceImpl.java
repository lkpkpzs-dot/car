package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.SysUserMapper;
import org.lkp.car.service.SysUserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户与权限 服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public List<SysUser> getAllPoliceUsers() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRoleType, 1); // 角色1是民警
        return this.list(wrapper);
    }

    @Override
    public List<SysUser> getEnterpriseUsers(Long enterpriseId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getAuthEnterpriseId, enterpriseId);
        return this.list(wrapper);
    }
}
