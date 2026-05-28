package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 车辆电子档案实体类
 * 对应数据库表：car_archive
 */
@Data
@TableName("car_archive")
@ApiModel(value = "CarArchive对象", description = "车辆电子档案中心")
public class CarArchive implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 车辆识别代码（车架号/主键）
     */
    @TableId(type = IdType.INPUT)
    @ApiModelProperty("车辆识别代码（车架号/主键）")
    private String vin;

    @ApiModelProperty("关联的道路申请ID")
    private Long applicationId;

    @ApiModelProperty("关联的查验记录ID")
    private Long vehicleInfoId;

    /**
     * 所属企业ID
     */
    @ApiModelProperty("所属企业ID")
    private Long enterpriseId;

    @ApiModelProperty("车辆品牌")
    private String vehicleBrand;

    @ApiModelProperty("车辆型号")
    private String vehicleModel;

    /**
     * 当前号牌: 1-道路测试, 2-示范应用, 3-应用试点
     */
    @ApiModelProperty("当前号牌: 1-道路测试, 2-示范应用, 3-应用试点")
    private Integer currentPlateType;

    /**
     * 当前车牌号(未发牌为空)
     */
    @ApiModelProperty("当前车牌号(未发牌为空)")
    private String plateNumber;

    /**
     * 状态: 1-正常营运, 2-异常整改, 3-已吊销
     */
    @ApiModelProperty("状态: 1-正常营运, 2-异常整改, 3-已吊销")
    private Integer status;

    /**
     * 累计合规测试里程
     */
    @ApiModelProperty("累计合规测试里程")
    private BigDecimal totalMileage;

    /**
     * 违章/事故记录次数
     */
    @ApiModelProperty("违章/事故记录次数")
    private Integer violationCount;

    /**
     * 查验参数JSON
     */
    @ApiModelProperty("查验参数JSON")
    private String techParams;

    /**
     * 标准照片JSON
     */
    @ApiModelProperty("标准照片JSON")
    private String imagesJson;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @ApiModelProperty("逻辑删除")
    private Integer isDeleted;
}
