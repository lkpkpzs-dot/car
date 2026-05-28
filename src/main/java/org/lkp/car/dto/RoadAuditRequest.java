package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 道路测试/应用申请审核请求
 */
@Data
@ApiModel("道路测试申请审核请求")
public class RoadAuditRequest {

    @ApiModelProperty(value = "申请ID", required = true)
    private Long applicationId;

    @ApiModelProperty(value = "状态：2通过 3驳回", required = true)
    private Integer status;

    @ApiModelProperty("审批意见")
    private String auditComment;

    @ApiModelProperty("驳回原因")
    private String rejectReason;

    @ApiModelProperty(value = "审核人ID", required = true)
    private Long reviewerId;
}
