package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 服务评价实体类
 * 对应数据库表：service_evaluation
 */
@Data
@TableName("service_evaluation")
@ApiModel(value = "ServiceEvaluation对象", description = "服务评价表")
public class ServiceEvaluation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 评价主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("评价主键ID")
    private Long evalId;

    /**
     * 关联的审批工单ID
     */
    @ApiModelProperty("关联的审批工单ID")
    private Long applyId;

    /**
     * 评价企业ID
     */
    @ApiModelProperty("评价企业ID")
    private Long enterpriseId;

    /**
     * 星级评分(1-5)
     */
    @ApiModelProperty("星级评分(1-5)")
    private Integer starRating;

    /**
     * 文字评价内容
     */
    @ApiModelProperty("文字评价内容")
    private String content;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
