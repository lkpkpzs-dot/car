package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "UserProfileUpdateRequest对象", description = "用户资料更新请求")
public class UserProfileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("真实姓名")
    private String realName;

    @ApiModelProperty("联系手机号")
    private String phone;
}
