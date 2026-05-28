package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 道路测试/应用申请实体类
 * 对应表：road_permission_application
 */
@Data
@TableName("road_permission_application")
@ApiModel(value = "RoadPermissionApplication对象", description = "道路测试/应用申请表")
public class RoadPermissionApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("申请ID")
    private Long id;

    @ApiModelProperty("企业ID")
    private Long enterpriseId;

    @ApiModelProperty("申请人ID")
    private Long applicantId;

    @ApiModelProperty("类型：1-道路测试 2-示范应用 3-应用试点")
    private Integer type;

    @ApiModelProperty("状态：0草稿 1待审 2通过 3驳回")
    private Integer status;

    @ApiModelProperty("发牌状态：0未发牌 1已发牌")
    private Integer plateStatus;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("车辆型号")
    private String vehicleModel;

    @ApiModelProperty("车架号(VIN)")
    private String vin;

    @ApiModelProperty("测试区域")
    private String testArea;

    @ApiModelProperty("开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @ApiModelProperty("结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    @ApiModelProperty("测试项目")
    private String testProjects;

    @ApiModelProperty("保障计划")
    private String supportPlan;

    @ApiModelProperty("整车合格证(JSON)")
    private String docVehicleCert;

    @ApiModelProperty("所有人身份证(JSON)")
    private String docOwnerId;

    @ApiModelProperty("安全检验报告(JSON)")
    private String docSafetyInspection;

    @ApiModelProperty("交强险(JSON)")
    private String docInsurance;

    @ApiModelProperty("委托书(JSON)")
    private String docOwnerProxy;

    @ApiModelProperty("代理人身份证(JSON)")
    private String docAgentId;

    @ApiModelProperty("安全声明(JSON)")
    private String docSafetyDeclaration;

    @ApiModelProperty("申请书(JSON)")
    private String docApplicationDoc;

    @ApiModelProperty("审核人ID")
    private Long reviewerId;

    @ApiModelProperty("审批意见")
    private String auditComment;

    @ApiModelProperty("驳回原因")
    private String rejectReason;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date auditTime;

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
