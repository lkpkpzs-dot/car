package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 车辆查验信息实体类
 */
@Data
@TableName("vehicle_info")
@ApiModel(value = "VehicleInfo对象", description = "车辆查验信息表")
public class VehicleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("车辆ID")
    private Long vehicleId;

    @ApiModelProperty("关联的道路申请ID")
    private Long applicationId;

    @ApiModelProperty("企业ID")
    private Long enterpriseId;

    @ApiModelProperty("提交人ID")
    private Long userId;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("车辆型号")
    private String vehicleModel;

    @ApiModelProperty("车架号(VIN)")
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

    @ApiModelProperty("左前45度图片")
    private String photoFront_45;

    @ApiModelProperty("右后45度图片")
    private String photoRear_45;

    @ApiModelProperty("车架号图片")
    private String photoVin;

    @ApiModelProperty("功能无人车整车合格证图片")
    private String docVehicleCertUnmanned;

    @ApiModelProperty("状态：0待审核 1通过 2驳回")
    private Integer status;

    @ApiModelProperty("驳回原因")
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("创建时间")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty("更新时间")
    private Date updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Integer isDeleted;
}
