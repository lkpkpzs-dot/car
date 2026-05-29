package org.lkp.car.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "DashboardVehicleVO对象", description = "首页我的车辆项")
public class DashboardVehicleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("车辆ID")
    private Long vehicleId;

    @ApiModelProperty("车架号(VIN)")
    private String vin;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("车牌号")
    private String plateNumber;

    @ApiModelProperty("牌照类型：1-道路测试，2-示范应用，3-应用试点")
    private Integer plateType;

    @ApiModelProperty("发牌日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date issueDate;
}
