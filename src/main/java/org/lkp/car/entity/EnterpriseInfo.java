package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 企业资质实体类
 * 对应数据库表：enterprise_info
 */
@Data
@TableName("enterprise_info")
@ApiModel(value = "EnterpriseInfo对象", description = "企业主体资质表")
public class EnterpriseInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 企业主体ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("企业主体ID")
    private Long enterpriseId;

    /**
     * 企业名称
     */
    @ApiModelProperty("企业名称")
    private String enterpriseName;

    /**
     * 统一社会信用代码
     */
    @ApiModelProperty("统一社会信用代码")
    private String creditCode;

    /**
     * 法定代表人
     */
    @ApiModelProperty("法定代表人")
    private String legalPerson;

    /**
     * 企业联系电话
     */
    @ApiModelProperty("企业联系电话")
    private String contactPhone;

    /**
     * 营业执照OSS路径
     */
    @ApiModelProperty("营业执照OSS路径")
    private String businessLicenseUrl;

    /**
     * 资质审核状态: 0-待审, 1-通过, 2-驳回
     */
    @ApiModelProperty(value = "资质审核状态：0-待审, 1-通过, 2-驳回", hidden = true) // 标记为隐藏
    private Integer auditStatus = 0;

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
