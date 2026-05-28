package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 民警发牌请求
 */
@Data
@ApiModel("民警发牌请求")
public class IssuePlateRequest {

    @ApiModelProperty(value = "档案VIN码", required = true)
    private String vin;

    @ApiModelProperty(value = "申请类型：1-道路测试，2-示范应用，3-应用试点", required = true)
    private Integer plateType;

    @ApiModelProperty(value = "车牌号", required = true)
    private String plateNumber;

    @ApiModelProperty(value = "发牌日期")
    private Date issueDate;

    @ApiModelProperty(value = "到期日期")
    private Date expiryDate;

    @ApiModelProperty(value = "发牌民警ID", required = true)
    private Long issuerId;

    @ApiModelProperty(value = "发牌备注")
    private String issueComment;
}
