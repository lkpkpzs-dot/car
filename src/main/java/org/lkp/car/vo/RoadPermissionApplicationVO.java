package org.lkp.car.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.lkp.car.entity.RoadPermissionApplication;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "RoadPermissionApplicationVO对象", description = "道路测试/应用申请返回对象")
public class RoadPermissionApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

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

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty("逻辑删除")
    private Integer isDeleted;

    @ApiModelProperty("企业名称")
    private String enterpriseName;

    @ApiModelProperty("查验状态：1-未查验 2-已通过 3-已驳回")
    private Integer inspectionStatus;

    @ApiModelProperty("查验状态描述")
    private String inspectionStatusLabel;

    @ApiModelProperty("查验状态样式类")
    private String inspectionStatusClass;

    public static RoadPermissionApplicationVO fromEntity(RoadPermissionApplication entity) {
        if (entity == null) return null;
        RoadPermissionApplicationVO vo = new RoadPermissionApplicationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
