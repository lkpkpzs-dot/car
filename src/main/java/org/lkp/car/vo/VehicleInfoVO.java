package org.lkp.car.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.lkp.car.entity.VehicleInfo;
import org.lkp.car.common.enums.VehicleStatusEnum;
import org.springframework.beans.BeanUtils;

/**
 * 车辆查验信息返回对象
 */
@Data
@ApiModel("车辆查验信息返回对象")
public class VehicleInfoVO extends VehicleInfo {

    @ApiModelProperty("状态说明")
    private String statusDesc;

    public static VehicleInfoVO fromEntity(VehicleInfo entity) {
        if (entity == null) return null;
        VehicleInfoVO vo = new VehicleInfoVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusDesc(VehicleStatusEnum.getDescByCode(entity.getStatus()));
        return vo;
    }
}
