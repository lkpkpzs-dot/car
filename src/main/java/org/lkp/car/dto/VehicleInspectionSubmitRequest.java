package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 车辆查验提交请求对象
 */
@Data
@ApiModel("车辆查验提交请求")
public class VehicleInspectionSubmitRequest {

    @ApiModelProperty(value = "关联的道路申请ID", required = true)
    private Long applicationId;

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

    @ApiModelProperty("左前45度图片")
    private String photoFront_45;

    @ApiModelProperty("右后45度图片")
    private String photoRear_45;

    @ApiModelProperty("车架号图片")
    private String photoVin;

    @ApiModelProperty("功能无人车整车合格证图片")
    private String docVehicleCertUnmanned;

    @ApiModelProperty(value = "查验状态：1通过 2驳回", required = true)
    private Integer auditStatus;

    @ApiModelProperty("驳回原因（驳回时必填）")
    private String rejectReason;
}
