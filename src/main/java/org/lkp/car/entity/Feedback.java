package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 意见建议实体类
 * 对应数据库表：feedback
 */
@Data
@TableName("feedback")
@ApiModel(value = "Feedback对象", description = "意见建议表")
public class Feedback implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 意见建议主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("意见建议主键ID")
    private Long feedbackId;

    /**
     * 提交用户ID
     */
    @ApiModelProperty("提交用户ID")
    private Long userId;

    /**
     * 意见建议类型: 1-功能建议, 2-bug反馈, 3-其他
     */
    @ApiModelProperty("意见建议类型: 1-功能建议, 2-bug反馈, 3-其他")
    private Integer feedbackType;

    /**
     * 标题
     */
    @ApiModelProperty("标题")
    private String title;

    /**
     * 详细内容
     */
    @ApiModelProperty("详细内容")
    private String content;

    /**
     * 图片附件（JSON数组格式）
     */
    @ApiModelProperty("图片附件（JSON数组格式）")
    private String images;

    /**
     * 联系方式
     */
    @ApiModelProperty("联系方式")
    private String contact;

    /**
     * 处理状态: 0-待处理, 1-处理中, 2-已处理
     */
    @ApiModelProperty("处理状态: 0-待处理, 1-处理中, 2-已处理")
    private Integer processStatus;

    /**
     * 处理备注
     */
    @ApiModelProperty("处理备注")
    private String processRemark;

    /**
     * 处理人ID
     */
    @ApiModelProperty("处理人ID")
    private Long handlerId;

    /**
     * 处理时间
     */
    @ApiModelProperty("处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date handleTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
