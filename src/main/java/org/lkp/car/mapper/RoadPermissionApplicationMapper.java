package org.lkp.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.vo.RoadPermissionApplicationVO;

import java.util.List;

/**
 * 道路测试/应用申请 Mapper 接口
 */
@Mapper
public interface RoadPermissionApplicationMapper extends BaseMapper<RoadPermissionApplication> {

    List<RoadPermissionApplicationVO> listWithEnterpriseName(@Param("status") Integer status, @Param("type") Integer type);
}
