package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 群众举报审核请求DTO
 */
@Data
@ApiModel(value = "群众举报审核请求", description = "群众举报审核请求")
public class CitizenReportReviewRequest {

    @NotNull(message = "举报ID不能为空")
    @ApiModelProperty(value = "举报ID", required = true)
    private Long reportId;

    @NotNull(message = "处理状态不能为空")
    @ApiModelProperty(value = "处理状态: 1-已处理 2-无效举报", required = true)
    private Integer processStatus;

    @ApiModelProperty(value = "审核备注")
    private String reviewRemark;
}
