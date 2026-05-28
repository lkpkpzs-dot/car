package org.lkp.car.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 安全员事故处分请求
 */
@Data
@ApiModel("安全员事故处分请求")
public class SafetyOfficerPenaltyRequest {

    @ApiModelProperty(value = "安全员ID", required = true)
    private Long officerId;

    @ApiModelProperty("事故日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date accidentDate;

    @ApiModelProperty(value = "责任等级：1-无责，2-次责，3-同等责任，4-主责，5-全责", required = true)
    private Integer liabilityLevel;

    @ApiModelProperty(value = "伤亡情况：0-无伤亡，1-受伤，2-死亡", required = true)
    private Integer casualtyType;

    @ApiModelProperty("事故及处理说明")
    private String reason;

    @ApiModelProperty("处理民警ID，由后端根据token自动填充")
    private Long handlerId;
}
