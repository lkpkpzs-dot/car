package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 安全员资质审核请求
 */
@Data
@ApiModel("安全员资质审核请求")
public class SafetyOfficerAuditRequest {

    @ApiModelProperty(value = "安全员ID", required = true)
    private Long officerId;

    @ApiModelProperty(value = "审核状态：1-通过，2-驳回", required = true)
    private Integer status;

    @ApiModelProperty("审核意见/驳回原因")
    private String comment;

    @ApiModelProperty("审核人ID，由后端根据token自动填充")
    private Long reviewerId;
}
