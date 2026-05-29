package org.lkp.car.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "EnterpriseDashboardVO对象", description = "企业端首页数据")
public class EnterpriseDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("申请列表")
    private List<DashboardApplicationVO> applicationList;

    @ApiModelProperty("审核中列表")
    private List<DashboardApplicationVO> pendingList;

    @ApiModelProperty("我的车辆列表")
    private List<DashboardVehicleVO> vehicleList;

    @ApiModelProperty("统计数量")
    private DashboardCountVO count;
}
