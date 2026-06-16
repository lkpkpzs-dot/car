package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 企业处理举报请求DTO
 */
@Data
@ApiModel(value = "企业处理举报请求", description = "企业处理举报请求")
public class CitizenReportEnterpriseHandleRequest {

    @NotNull(message = "举报ID不能为空")
    @ApiModelProperty(value = "举报ID", required = true)
    private Long reportId;

    @NotNull(message = "处理状态不能为空")
    @ApiModelProperty(value = "处理状态: 2-已处理 3-无效举报", required = true)
    private Integer processStatus;

    @ApiModelProperty(value = "处理备注")
    private String remark;
}
