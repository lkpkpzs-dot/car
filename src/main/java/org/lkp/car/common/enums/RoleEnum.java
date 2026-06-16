package org.lkp.car.common.enums;

/**
 * 用户角色枚举
 * 定义系统中的三种角色类型，用于权限控制和业务逻辑判断
 */
public enum RoleEnum {
    
    /**
     * 交警/车管所角色
     * 拥有最高权限，可进行审核、管理等操作
     */
    POLICE(1, "交警/车管所"),
    
    /**
     * 企业代办人角色
     * 可进行车辆上牌申请、道路通行申请等企业相关业务
     */
    ENTERPRISE(2, "企业代办人"),
    
    /**
     * 普通市民角色
     * 可进行举报、反馈等基础操作
     */
    CITIZEN(3, "普通市民");

    /**
     * 交警角色编码常量
     */
    public static final int POLICE_CODE = 1;
    
    /**
     * 企业角色编码常量
     */
    public static final int ENTERPRISE_CODE = 2;
    
    /**
     * 市民角色编码常量
     */
    public static final int CITIZEN_CODE = 3;

    /**
     * 角色编码
     */
    private final int code;
    
    /**
     * 角色描述
     */
    private final String desc;

    /**
     * 构造函数
     * @param code 角色编码
     * @param desc 角色描述
     */
    RoleEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取角色编码
     * @return 角色编码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取角色描述
     * @return 角色描述
     */
    public String getDesc() {
        return desc;
    }
}
