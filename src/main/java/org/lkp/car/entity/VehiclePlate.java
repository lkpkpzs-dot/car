package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 车辆牌照实体类
 * 对应数据库表：vehicle_plate
 */
@Data
@TableName("vehicle_plate")
@ApiModel(value = "VehiclePlate对象", description = "车辆牌照表")
public class VehiclePlate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("牌照记录ID")
    private Long plateId;

    @ApiModelProperty("车辆识别代码（车架号）")
    private String vin;

    @ApiModelProperty("所属企业ID")
    private Long enterpriseId;

    @ApiModelProperty("关联的道路申请ID")
    private Long applicationId;

    @ApiModelProperty("关联的查验记录ID")
    private Long vehicleInfoId;

    @ApiModelProperty("牌照类型：1-道路测试，2-示范应用，3-应用试点")
    private Integer plateType;

    @ApiModelProperty("车牌号")
    private String plateNumber;

    @ApiModelProperty("发牌日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date issueDate;

    @ApiModelProperty("到期日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expiryDate;

    @ApiModelProperty("发牌民警ID")
    private Long issuerId;

    @ApiModelProperty("发牌备注")
    private String issueComment;

    @ApiModelProperty("状态：1-有效，2-已过期，3-已注销")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Integer isDeleted;
}
