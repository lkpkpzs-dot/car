package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.SysUserMapper;
import org.lkp.car.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户与权限 服务实现类
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
