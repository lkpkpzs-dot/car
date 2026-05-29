package org.lkp.car.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "DashboardApplicationVO对象", description = "首页申请列表项")
public class DashboardApplicationVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("申请ID")
    private Long id;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("车辆型号")
    private String vehicleModel;

    @ApiModelProperty("车架号(VIN)")
    private String vin;

    @ApiModelProperty("类型：1-道路测试 2-示范应用 3-应用试点")
    private Integer type;

    @ApiModelProperty("状态：0草稿 1待审 2通过 3驳回")
    private Integer status;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty("查验状态：1-未查验 2-已通过 3-已驳回")
    private Integer inspectionStatus;

    @ApiModelProperty("发牌状态：0未发牌 1已发牌")
    private Integer plateStatus;
}
