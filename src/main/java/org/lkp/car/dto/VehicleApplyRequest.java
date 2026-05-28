package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 车辆查验申请请求对象
 */
@Data
@ApiModel("车辆查验申请请求")
public class VehicleApplyRequest {

    @ApiModelProperty(value = "车辆ID (重新提交时传)")
    private Long vehicleId;

    @ApiModelProperty(value = "提交人ID", required = true)
    private Long userId;

    @ApiModelProperty(value = "企业ID", required = true)
    private Long enterpriseId;

    @ApiModelProperty(value = "车辆品牌", required = true)
    private String vehicleBrand;

    @ApiModelProperty(value = "车辆型号", required = true)
    private String vehicleModel;

    @ApiModelProperty(value = "车架号(VIN)", required = true)
    private String vin;

    @ApiModelProperty("车辆类型")
    private String vehicleType;

    @ApiModelProperty("长(mm)")
    private BigDecimal length;

    @ApiModelProperty("宽(mm)")
    private BigDecimal width;

    @ApiModelProperty("高(mm)")
    private BigDecimal height;

    @ApiModelProperty("总质量(kg)")
    private BigDecimal totalMass;

    @ApiModelProperty("整备质量(kg)")
    private BigDecimal curbWeight;

    @ApiModelProperty("核载质量(kg)")
    private BigDecimal ratedLoad;

    @ApiModelProperty("轴数")
    private Integer axleCount;

    @ApiModelProperty("轮胎型号")
    private String tireSpec;

    @ApiModelProperty("电机功率(kw)")
    private BigDecimal motorPower;

    @ApiModelProperty("电机号")
    private String motorNo;

    @ApiModelProperty("最高时速(km/h)")
    private BigDecimal maxSpeed;

    @ApiModelProperty("电池类型")
    private String batteryType;

    @ApiModelProperty("电池容量(kWh)")
    private BigDecimal batteryCapacity;

    @ApiModelProperty(value = "左前45度图片", required = true)
    private String photoFront_45;

    @ApiModelProperty(value = "右后45度图片", required = true)
    private String photoRear_45;

    @ApiModelProperty(value = "车架号图片", required = true)
    private String photoVin;

    @ApiModelProperty("功能无人车整车合格证图片")
    private String docVehicleCertUnmanned;
}
