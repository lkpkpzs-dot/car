package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("刷新 Token 请求")
public class RefreshTokenRequest {
    @ApiModelProperty("刷新令牌")
    private String refreshToken;
}
