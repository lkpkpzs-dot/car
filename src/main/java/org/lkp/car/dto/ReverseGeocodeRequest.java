package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("逆地理编码请求")
public class ReverseGeocodeRequest {

    @NotNull(message = "纬度不能为空")
    @ApiModelProperty(value = "纬度", required = true)
    private Double latitude;

    @NotNull(message = "经度不能为空")
    @ApiModelProperty(value = "经度", required = true)
    private Double longitude;
}
