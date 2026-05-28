package org.lkp.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.lkp.car.entity.LicenseApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 号牌申请 Mapper 接口
 */
@Mapper
public interface LicenseApplicationMapper extends BaseMapper<LicenseApplication> {
}
