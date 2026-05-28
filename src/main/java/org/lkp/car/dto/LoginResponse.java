package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.lkp.car.entity.SysUser;

/**
 * 登录响应对象
 */
@Data
@ApiModel("小程序登录响应")
public class LoginResponse {

    @ApiModelProperty("身份令牌")
    private String token;

    @ApiModelProperty("用户信息")
    private SysUser user;

    // 👇 新增
    @ApiModelProperty("企业名称")
    private String enterpriseName;

    @ApiModelProperty("资质状态")
    private Integer qualificationStatus;
}
