package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 审批流转记录实体类
 * 对应数据库表：approval_record
 */
@Data
@TableName("approval_record")
@ApiModel(value = "ApprovalRecord对象", description = "审批流转全程留痕表")
public class ApprovalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 记录主键
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("记录主键")
    private Long recordId;

    /**
     * 关联的业务主键ID (如 apply_id 或 enterprise_id)
     */
    @ApiModelProperty("关联的业务主键ID")
    private Long applyId;

    /**
     * 申请人用户ID (sys_user.user_id)
     */
    @ApiModelProperty("申请人用户ID")
    private Long applicantId;

    /**
     * 业务类型: 1-号牌申请审批, 2-企业资质审批
     */
    @ApiModelProperty("业务类型: 1-号牌申请审批, 2-企业资质审批")
    private Integer businessType;

    /**
     * 审批节点: 提交申请/初审/终审/查验发牌
     */
    @ApiModelProperty("审批节点: 提交申请/初审/终审/查验发牌")
    private String nodeName;

    /**
     * 操作人ID (经办人或交警ID)
     */
    @ApiModelProperty("操作人ID (经办人或交警ID)")
    private Long reviewerId;

    /**
     * 动作类型: 1-提交, 2-通过, 3-驳回, 4-转交
     */
    @ApiModelProperty("动作类型: 1-提交, 2-通过, 3-驳回, 4-转交")
    private Integer actionType;

    /**
     * 审批意见/驳回原因
     */
    @ApiModelProperty("审批意见/驳回原因")
    private String comment;

    /**
     * 当前节点数据快照(JSON格式)
     */
    @ApiModelProperty("当前节点数据快照(JSON格式)")
    private String snapshotJson;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("操作时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
