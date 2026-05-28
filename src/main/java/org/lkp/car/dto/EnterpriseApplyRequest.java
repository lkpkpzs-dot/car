package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.lkp.car.entity.EnterpriseInfo;

/**
 * 企业资质申请请求
 */
@Data
@ApiModel("企业资质申请请求")
public class EnterpriseApplyRequest {

    @ApiModelProperty(value = "企业信息（新建不传 enterpriseId，重新申请传 enterpriseId）", required = true)
    private EnterpriseInfo enterprise;

    @ApiModelProperty(value = "申请人用户ID（企业代办人 sys_user.user_id）", required = true)
    private Long applicantId;

    @ApiModelProperty("申请说明")
    private String comment;
}
