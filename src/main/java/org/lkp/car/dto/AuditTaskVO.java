package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

/**
 * 统一审核任务视图对象
 */
@Data
@ApiModel("统一审核任务信息")
public class AuditTaskVO {

    @ApiModelProperty("业务主键ID (enterpriseId 或 applyId)")
    private Long id;

    @ApiModelProperty("业务类型: 1-号牌申请审批, 2-企业资质审批")
    private Integer businessType;

    @ApiModelProperty("任务标题 (企业名称 或 车辆VIN)")
    private String title;

    @ApiModelProperty("当前状态描述")
    private String statusDesc;

    @ApiModelProperty("当前状态码")
    private Integer status;

    @ApiModelProperty("提交时间")
    private Date createTime;
}
