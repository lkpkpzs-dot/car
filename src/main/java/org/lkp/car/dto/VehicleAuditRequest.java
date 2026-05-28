package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 车辆查验审核请求对象
 */
@Data
@ApiModel("车辆查验审核请求")
public class VehicleAuditRequest {

    @ApiModelProperty(value = "车辆ID", required = true)
    private Long vehicleId;

    @ApiModelProperty(value = "审核人ID", required = true)
    private Long reviewerId;

    @ApiModelProperty(value = "审核状态：1通过 2驳回", required = true)
    private Integer auditStatus;

    @ApiModelProperty("审核意见/驳回原因")
    private String reason;
}
