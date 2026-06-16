package org.lkp.car.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("管理端首页统计数据")
public class AdminDashboardVO {

    @ApiModelProperty("待审核数量")
    private Integer pendingCount;

    @ApiModelProperty("已通过数量")
    private Integer approvedCount;

    @ApiModelProperty("已驳回数量")
    private Integer rejectedCount;

    @ApiModelProperty("今日办理数量")
    private Integer todayProcess;
}
