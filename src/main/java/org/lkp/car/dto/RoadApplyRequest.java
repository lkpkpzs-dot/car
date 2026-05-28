package org.lkp.car.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 道路测试/应用申请提交请求
 */
@Data
@ApiModel("道路测试申请提交请求")
public class RoadApplyRequest {

    @ApiModelProperty("申请记录ID (重新提交时传)")
    private Long id;

    @ApiModelProperty(value = "类型：1-道路测试 2-示范应用 3-应用试点", required = true)
    private Integer type;

    @ApiModelProperty(value = "企业ID", required = true)
    private Long enterpriseId;

    @ApiModelProperty(value = "申请人ID", required = true)
    private Long applicantId;

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

    @ApiModelProperty("整车合格证列表")
    private List<String> docVehicleCert;

    @ApiModelProperty("所有人身份证列表")
    private List<String> docOwnerId;

    @ApiModelProperty("安全检验报告列表")
    private List<String> docSafetyInspection;

    @ApiModelProperty("交强险列表")
    private List<String> docInsurance;

    @ApiModelProperty("委托书列表")
    private List<String> docOwnerProxy;

    @ApiModelProperty("代理人身份证列表")
    private List<String> docAgentId;

    @ApiModelProperty("安全声明列表")
    private List<String> docSafetyDeclaration;

    @ApiModelProperty("申请书列表")
    private List<String> docApplicationDoc;
}
