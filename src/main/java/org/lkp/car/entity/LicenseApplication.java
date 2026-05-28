package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 号牌申请工单实体类
 * 对应数据库表：license_application
 */
@Data
@TableName("license_application")
@ApiModel(value = "LicenseApplication对象", description = "号牌申请业务工单表")
public class LicenseApplication implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 申请单号主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("申请单号主键")
    private Long applyId;

    /**
     * 关联车辆识别代码
     */
    @ApiModelProperty("关联车辆识别代码")
    private String vin;

    /**
     * 申请企业ID
     */
    @ApiModelProperty("申请企业ID")
    private Long enterpriseId;

    /**
     * 申请号牌: 1-道路测试, 2-示范应用, 3-应用试点
     */
    @ApiModelProperty("申请号牌: 1-道路测试, 2-示范应用, 3-应用试点")
    private Integer applyPlateType;

    /**
     * 审批流: 0-待提交, 1-初审中, 2-终审中, 3-待查验, 4-已发牌, 5-驳回
     */
    @ApiModelProperty("审批流: 0-待提交, 1-初审中, 2-终审中, 3-待查验, 4-已发牌, 5-驳回")
    private Integer flowStatus = 0;

    /**
     * 审核资料JSON
     */
    @ApiModelProperty("审核资料JSON")
    private String materialsJson;

    /**
     * 路线与保障计划
     */
    @ApiModelProperty("路线与保障计划")
    private String routePlan;

    /**
     * 当前处理交警ID
     */
    @ApiModelProperty("当前处理交警ID")
    private Long reviewerId;

    /**
     * 审批意见
     */
    @ApiModelProperty("审批意见")
    private String auditComment;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Integer isDeleted;
}
