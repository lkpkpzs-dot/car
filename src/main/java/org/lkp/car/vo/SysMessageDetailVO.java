package org.lkp.car.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 系统消息详情VO
 */
@Data
@ApiModel(value = "系统消息详情VO", description = "系统消息详情VO，包含关联的举报信息")
public class SysMessageDetailVO {

    // ========== 系统消息信息 ==========

    @ApiModelProperty("消息主键ID")
    private Long msgId;

    @ApiModelProperty("接收用户ID")
    private Long receiverId;

    @ApiModelProperty("消息类型: 1-审批通过, 2-驳回, 3-整改通知, 4-举报处理通知")
    private Integer msgType;

    @ApiModelProperty("业务类型: 1-举报")
    private Integer businessType;

    @ApiModelProperty("业务ID")
    private Long businessId;

    @ApiModelProperty("消息标题")
    private String title;

    @ApiModelProperty("消息正文")
    private String content;

    @ApiModelProperty("是否已读: 0-未读, 1-已读")
    private Integer isRead;

    @ApiModelProperty("消息创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // ========== 关联的举报信息 ==========

    @ApiModelProperty("举报ID")
    private Long reportId;

    @ApiModelProperty("举报人ID")
    private Long reportUserId;

    @ApiModelProperty("举报类型: 1-违规占道, 2-乱停乱放, 3-违规行驶, 4-意见建议")
    private Integer reportType;

    @ApiModelProperty("被举报车牌号")
    private String targetPlate;

    @ApiModelProperty("图片/视频证据JSON")
    private String evidenceJson;

    @ApiModelProperty("定位地址")
    private String locationExt;

    @ApiModelProperty("处理状态: 0-待核实, 1-已处理, 2-无效举报")
    private Integer processStatus;

    @ApiModelProperty("举报创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reportCreateTime;

    @ApiModelProperty("审核民警ID")
    private Long reviewerId;

    @ApiModelProperty("审核民警姓名")
    private String reviewerName;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewTime;

    @ApiModelProperty("审核备注")
    private String reviewRemark;
}
