package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统消息实体类
 * 对应数据库表：sys_message
 */
@Data
@TableName("sys_message")
@ApiModel(value = "SysMessage对象", description = "系统消息表")
public class SysMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 消息主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("消息主键ID")
    private Long msgId;

    /**
     * 接收用户ID
     */
    @ApiModelProperty("接收用户ID")
    private Long receiverId;

    /**
     * 消息类型: 1-审批通过, 2-驳回, 3-整改通知, 4-举报处理通知
     */
    @ApiModelProperty("消息类型: 1-审批通过, 2-驳回, 3-整改通知, 4-举报处理通知")
    private Integer msgType;

    /**
     * 业务类型: 1-举报
     */
    @ApiModelProperty("业务类型: 1-举报")
    private Integer businessType;

    /**
     * 业务ID
     */
    @ApiModelProperty("业务ID")
    private Long businessId;

    /**
     * 消息标题
     */
    @ApiModelProperty("消息标题")
    private String title;

    /**
     * 消息正文
     */
    @ApiModelProperty("消息正文")
    private String content;

    /**
     * 是否已读: 0-未读, 1-已读
     */
    @ApiModelProperty("是否已读: 0-未读, 1-已读")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
