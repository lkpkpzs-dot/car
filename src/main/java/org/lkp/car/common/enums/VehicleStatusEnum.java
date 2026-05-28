package org.lkp.car.common.enums;

import lombok.Getter;

/**
 * 车辆查验状态枚举
 */
@Getter
public enum VehicleStatusEnum {
    PENDING(0, "待审核"),
    PASSED(1, "通过"),
    REJECTED(2, "驳回");

    private final Integer code;
    private final String desc;

    VehicleStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDescByCode(Integer code) {
        for (VehicleStatusEnum status : VehicleStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知";
    }
}
