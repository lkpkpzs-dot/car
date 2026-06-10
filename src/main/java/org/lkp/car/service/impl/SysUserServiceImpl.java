package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.SysUserMapper;
import org.lkp.car.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUserReport(Long userId, int banHours, String reason) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIsReportBanned(1);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, banHours);
        user.setBanEndTime(calendar.getTime());
        this.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUserReport(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setIsReportBanned(0);
        user.setBanEndTime(null);
        this.updateById(user);
    }

    @Override
    public boolean isUserBanned(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            return false;
        }
        // 检查是否被封禁且未过封禁时间
        if (user.getIsReportBanned() == null || user.getIsReportBanned() != 1) {
            return false;
        }
        if (user.getBanEndTime() == null) {
            return true; // 永久封禁（当前没设置永久，暂时按有结束时间处理）
        }
        return user.getBanEndTime().after(new Date());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementTotalReportCount(Long userId) {
        SysUser user = this.getById(userId);
        if (user != null) {
            user.setTotalReportCount((user.getTotalReportCount() == null ? 0 : user.getTotalReportCount()) + 1);
            this.updateById(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementInvalidReportCount(Long userId) {
        SysUser user = this.getById(userId);
        if (user != null) {
            user.setInvalidReportCount((user.getInvalidReportCount() == null ? 0 : user.getInvalidReportCount()) + 1);
            this.updateById(user);
        }
    }
}
