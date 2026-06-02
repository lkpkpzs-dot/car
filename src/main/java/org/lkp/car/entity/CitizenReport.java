package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 群众举报实体类
 * 对应数据库表：citizen_report
 */
@Data
@TableName("citizen_report")
@ApiModel(value = "CitizenReport对象", description = "群众举报表")
public class CitizenReport implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 举报主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("举报主键ID")
    private Long reportId;

    /**
     * 举报人ID
     */
    @ApiModelProperty("举报人ID")
    private Long userId;

    /**
     * 举报类型: 1-违规占道, 2-乱停乱放, 3-违规行驶, 4-意见建议
     */
    @ApiModelProperty("举报类型: 1-违规占道, 2-乱停乱放, 3-违规行驶, 4-意见建议")
    private Integer reportType;

    /**
     * 被举报车牌号
     */
    @ApiModelProperty("被举报车牌号")
    private String targetPlate;

    /**
     * 图片/视频OSS路径JSON
     */
    @ApiModelProperty("图片/视频OSS路径JSON")
    private String evidenceJson;

    /**
     * LBS定位地址
     */
    @ApiModelProperty("LBS定位地址")
    private String locationExt;

    /**
     * 处理状态: 0-待核实, 1-已处理, 2-无效举报
     */
    @ApiModelProperty("处理状态: 0-待核实, 1-已处理, 2-无效举报")
    private Integer processStatus;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 审核民警ID
     */
    @ApiModelProperty("审核民警ID")
    private Long reviewerId;

    /**
     * 审核时间
     */
    @ApiModelProperty("审核时间")
    private Date reviewTime;

    /**
     * 审核备注
     */
    @ApiModelProperty("审核备注")
    private String reviewRemark;
}
