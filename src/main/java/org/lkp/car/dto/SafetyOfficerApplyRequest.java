package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.lkp.car.entity.SafetyOfficer;

/**
 * 安全员资质申请请求
 */
@Data
@ApiModel("安全员资质申请请求")
public class SafetyOfficerApplyRequest {

    @ApiModelProperty(value = "安全员信息", required = true)
    private SafetyOfficer officer;
}
