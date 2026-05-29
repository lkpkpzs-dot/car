package org.lkp.car.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "DashboardCountVO对象", description = "首页统计数量")
public class DashboardCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("总申请数")
    private Integer totalApplication;

    @ApiModelProperty("待审核数")
    private Integer pending;

    @ApiModelProperty("车辆数")
    private Integer vehicle;
}
