package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分配安全员请求
 */
@Data
@ApiModel("分配安全员请求")
public class AssignSafetyOfficerRequest {

    @ApiModelProperty(value = "车辆VIN码", required = true)
    private String vin;

    @ApiModelProperty(value = "安全员ID", required = true)
    private Long officerId;
}
