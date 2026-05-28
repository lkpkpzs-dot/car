package org.lkp.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.lkp.car.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户与权限 Mapper 接口
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
