package org.lkp.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 用户与权限实体类
 * 对应数据库表：sys_user
 */
@Data
@TableName("sys_user")
@ApiModel(value = "SysUser对象", description = "用户与权限表")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户主键ID
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty("用户主键ID")
    private Long userId;

    /**
     * 微信小程序唯一标识
     */
    @ApiModelProperty("微信小程序唯一标识")
    private String openid;

    /**
     * 角色: 1-交警/车管所, 2-企业代办人, 3-普通市民
     */
    @ApiModelProperty("角色: 1-交警/车管所, 2-企业代办人, 3-普通市民")
    private Integer roleType;

    /**
     * 真实姓名
     */
    @ApiModelProperty("真实姓名")
    private String realName;

    /**
     * 联系手机号
     */
    @ApiModelProperty("联系手机号")
    private String phone;

    /**
     * 关联企业ID(仅企业角色有值)
     */
    @ApiModelProperty("关联企业ID(仅企业角色有值)")
    private Long authEnterpriseId;

    /**
     * 创建时间（由 MyBatis-Plus 自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间（由 MyBatis-Plus 自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty("更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    /**
     * 逻辑删除: 0-未删除, 1-已删除
     */
    @TableLogic
    @ApiModelProperty("逻辑删除: 0-未删除, 1-已删除")
    private Integer isDeleted = 0;
}
