package org.lkp.car.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 市民端首页统计数据VO
 */
@Data
@ApiModel(value = "市民端首页统计数据", description = "市民端首页统计数据")
public class CitizenDashboardVO {

    @ApiModelProperty(value = "我的举报总数")
    private Integer totalReport;

    @ApiModelProperty(value = "待核实数量")
    private Integer pendingCount;

    @ApiModelProperty(value = "已处理数量")
    private Integer approvedCount;

    @ApiModelProperty(value = "无效举报数量")
    private Integer rejectedCount;
}
