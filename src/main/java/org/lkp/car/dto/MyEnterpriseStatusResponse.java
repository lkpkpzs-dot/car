package org.lkp.car.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 我的企业资质状态响应对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("我的企业资质状态响应")
public class MyEnterpriseStatusResponse {

    @ApiModelProperty("企业名称")
    private String enterpriseName;

    @ApiModelProperty("资质审核状态: 0-待审, 1-通过, 2-驳回")
    private Integer qualificationStatus;
}
