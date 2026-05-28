package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 安全员事故处分记录表
 */
@Data
@TableName("safety_officer_penalty")
@ApiModel(value = "SafetyOfficerPenalty对象", description = "安全员事故处分记录表")
public class SafetyOfficerPenalty implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("处分记录ID")
    private Long penaltyId;

    @ApiModelProperty("安全员ID")
    private Long officerId;

    @ApiModelProperty("所属企业ID")
    private Long enterpriseId;

    @ApiModelProperty("事故日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date accidentDate;

    @ApiModelProperty("责任等级：1-无责，2-次责，3-同等责任，4-主责，5-全责")
    private Integer liabilityLevel;

    @ApiModelProperty("伤亡情况：0-无伤亡，1-受伤，2-死亡")
    private Integer casualtyType;

    @ApiModelProperty("处分类型：1-暂停3个月，2-暂停半年，3-取消资格")
    private Integer penaltyType;

    @ApiModelProperty("处分开始日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @ApiModelProperty("处分结束日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    @ApiModelProperty("处理民警ID")
    private Long handlerId;

    @ApiModelProperty("事故及处理说明")
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @TableLogic
    @ApiModelProperty("逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
