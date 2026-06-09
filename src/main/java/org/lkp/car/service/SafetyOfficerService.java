package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.SafetyOfficerApplyRequest;
import org.lkp.car.dto.SafetyOfficerAuditRequest;
import org.lkp.car.dto.SafetyOfficerPenaltyRequest;
import org.lkp.car.entity.CarArchive;
import org.lkp.car.entity.SafetyOfficer;
import org.lkp.car.entity.SafetyOfficerPenalty;

import java.util.List;

/**
 * 安全员资质监管 服务类
 */
public interface SafetyOfficerService extends IService<SafetyOfficer> {

    Long apply(SafetyOfficerApplyRequest request, Long applicantId, Long enterpriseId);

    boolean audit(SafetyOfficerAuditRequest request);

    SafetyOfficerPenalty handleAccident(SafetyOfficerPenaltyRequest request);

    /**
     * 获取安全员关联的车辆列表
     */
    List<CarArchive> getOfficerVehicles(Long officerId);

    /**
     * 获取安全员已关联的车辆数量
     */
    int getOfficerVehicleCount(Long officerId);
}
