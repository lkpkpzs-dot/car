package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 登录请求对象
 */
@Data
@ApiModel("小程序登录请求")
public class LoginRequest {

    @ApiModelProperty(value = "微信登录code", required = true)
    private String code;
}
