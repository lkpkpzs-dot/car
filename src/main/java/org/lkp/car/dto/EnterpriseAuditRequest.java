package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 企业资质审核请求对象
 */
@Data
@ApiModel("企业资质审核请求")
public class EnterpriseAuditRequest {

    @ApiModelProperty(value = "企业ID", required = true)
    private Long enterpriseId;

    @ApiModelProperty(value = "审核状态: 1-通过, 2-驳回", required = true)
    private Integer auditStatus;

    @ApiModelProperty(value = "审核人ID (必须是民警用户的ID)", required = true)
    private Long reviewerId;

    @ApiModelProperty(value = "审核意见/备注")
    private String reason;
}
