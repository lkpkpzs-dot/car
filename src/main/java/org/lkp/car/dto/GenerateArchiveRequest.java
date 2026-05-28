package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 从查验生成档案请求
 */
@Data
@ApiModel("从查验生成档案请求")
public class GenerateArchiveRequest {

    @ApiModelProperty(value = "查验记录ID", required = true)
    private Long vehicleInfoId;
}
