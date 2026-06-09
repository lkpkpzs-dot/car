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
 * 安全员资质监管表
 */
@Data
@TableName("safety_officer")
@ApiModel(value = "SafetyOfficer对象", description = "安全员资质监管表")
public class SafetyOfficer implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("安全员ID")
    private Long officerId;

    @ApiModelProperty("所属企业ID")
    private Long enterpriseId;

    @ApiModelProperty("提交人用户ID")
    private Long applicantId;

    @ApiModelProperty("安全员姓名")
    private String officerName;

    @ApiModelProperty("身份证号")
    private String idCardNo;

    @ApiModelProperty("联系电话")
    private String phone;

    @ApiModelProperty("年龄")
    private Integer age;

    @ApiModelProperty("驾驶证类型：C1、C2、A1、A2、A3、B1、B2等")
    private String licenseType;

    @ApiModelProperty("驾驶证号")
    private String driverLicenseNo;

    @ApiModelProperty("初次领证日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date firstLicenseDate;

    @ApiModelProperty("最近连续三个记分周期无记满12分记录：0-否，1-是")
    private Integer noFullScoreRecord;

    @ApiModelProperty("无致人死亡或重伤的交通责任事故记录：0-否，1-是")
    private Integer noMajorAccidentRecord;

    @ApiModelProperty("无酒后或醉酒驾驶机动车记录：0-否，1-是")
    private Integer noDuiRecord;

    @ApiModelProperty("无犯罪记录：0-否，1-是")
    private Integer noCrimeRecord;

    @ApiModelProperty("身心健康且无危及行车安全疾病史：0-否，1-是")
    private Integer healthy;

    @ApiModelProperty("无酗酒、吸毒行为记录：0-否，1-是")
    private Integer noAlcoholDrugRecord;

    @ApiModelProperty("身份证材料URL")
    private String idCardUrl;

    @ApiModelProperty("机动车驾驶证材料URL")
    private String driverLicenseUrl;

    @ApiModelProperty("机动车驾驶人身体条件证明URL（3个月内）")
    private String healthCertificateUrl;

    @ApiModelProperty("无犯罪记录证明URL")
    private String noCrimeCertificateUrl;

    @ApiModelProperty("无相应交通违法及事故证明URL")
    private String noViolationAccidentCertificateUrl;

    @ApiModelProperty("无酗酒、吸毒记录证明URL")
    private String noAlcoholDrugCertificateUrl;

    @ApiModelProperty("资质状态：0-待审核，1-有效，2-驳回，3-暂停，4-取消")
    private Integer status = 0;

    @ApiModelProperty("审核人ID")
    private Long reviewerId;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewTime;

    @ApiModelProperty("审核/驳回意见")
    private String reviewComment;

    @ApiModelProperty("暂停开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date suspendStartDate;

    @ApiModelProperty("暂停结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date suspendEndDate;

    @ApiModelProperty("处分原因")
    private String penaltyReason;

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

    @TableField(exist = false)
    @ApiModelProperty("已关联车辆总数（正式档案 + 待审核/已通过申请），用于前端展示")
    private Integer totalVehicleCount;
}
