package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.SafetyOfficerApplyRequest;
import org.lkp.car.dto.SafetyOfficerAuditRequest;
import org.lkp.car.dto.SafetyOfficerPenaltyRequest;
import org.lkp.car.entity.SafetyOfficer;
import org.lkp.car.entity.SafetyOfficerPenalty;

/**
 * 安全员资质监管 服务类
 */
public interface SafetyOfficerService extends IService<SafetyOfficer> {

    Long apply(SafetyOfficerApplyRequest request, Long applicantId, Long enterpriseId);

    boolean audit(SafetyOfficerAuditRequest request);

    SafetyOfficerPenalty handleAccident(SafetyOfficerPenaltyRequest request);
}
