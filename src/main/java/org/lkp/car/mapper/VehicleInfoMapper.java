package org.lkp.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.lkp.car.entity.VehicleInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆查验信息 Mapper 接口
 */
@Mapper
public interface VehicleInfoMapper extends BaseMapper<VehicleInfo> {
}
