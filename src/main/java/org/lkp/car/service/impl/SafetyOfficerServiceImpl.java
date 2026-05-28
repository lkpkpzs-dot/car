package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.dto.SafetyOfficerApplyRequest;
import org.lkp.car.dto.SafetyOfficerAuditRequest;
import org.lkp.car.dto.SafetyOfficerPenaltyRequest;
import org.lkp.car.entity.SafetyOfficer;
import org.lkp.car.entity.SafetyOfficerPenalty;
import org.lkp.car.mapper.SafetyOfficerMapper;
import org.lkp.car.service.SafetyOfficerPenaltyService;
import org.lkp.car.service.SafetyOfficerService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * 安全员资质监管 服务实现类
 */
@Service
public class SafetyOfficerServiceImpl extends ServiceImpl<SafetyOfficerMapper, SafetyOfficer>
        implements SafetyOfficerService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int STATUS_SUSPENDED = 3;
    private static final int STATUS_CANCELLED = 4;

    private static final int LIABILITY_EQUAL = 3;
    private static final int CASUALTY_NONE = 0;
    private static final int CASUALTY_INJURY = 1;
    private static final int CASUALTY_DEATH = 2;

    private static final int PENALTY_SUSPEND_THREE_MONTHS = 1;
    private static final int PENALTY_SUSPEND_HALF_YEAR = 2;
    private static final int PENALTY_CANCEL = 3;

    @Autowired
    private SafetyOfficerPenaltyService safetyOfficerPenaltyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(SafetyOfficerApplyRequest request, Long applicantId, Long enterpriseId) {
        if (request == null || request.getOfficer() == null) {
            throw new RuntimeException("安全员信息不能为空");
        }
        if (applicantId == null || enterpriseId == null) {
            throw new RuntimeException("申请人和企业不能为空");
        }

        SafetyOfficer officer = request.getOfficer();
        validateOfficerBaseInfo(officer);

        if (officer.getOfficerId() != null) {
            SafetyOfficer existing = this.getById(officer.getOfficerId());
            if (existing == null) {
                throw new RuntimeException("安全员记录不存在");
            }
            if (!enterpriseId.equals(existing.getEnterpriseId())) {
                throw new RuntimeException("无权修改其他企业的安全员");
            }
        }

        officer.setApplicantId(applicantId);
        officer.setEnterpriseId(enterpriseId);
        officer.setStatus(STATUS_PENDING);
        officer.setReviewerId(null);
        officer.setReviewTime(null);
        officer.setReviewComment(null);
        officer.setSuspendStartDate(null);
        officer.setSuspendEndDate(null);
        officer.setPenaltyReason(null);

        this.saveOrUpdate(officer);
        return officer.getOfficerId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(SafetyOfficerAuditRequest request) {
        if (request == null || request.getOfficerId() == null) {
            throw new RuntimeException("安全员ID不能为空");
        }
        if (request.getStatus() == null
                || (request.getStatus() != STATUS_APPROVED && request.getStatus() != STATUS_REJECTED)) {
            throw new RuntimeException("审核状态只能为通过或驳回");
        }

        SafetyOfficer officer = this.getById(request.getOfficerId());
        if (officer == null) {
            throw new RuntimeException("安全员记录不存在");
        }

        if (request.getStatus() == STATUS_APPROVED) {
            validateOfficerEligibility(officer);
        }

        officer.setStatus(request.getStatus());
        officer.setReviewerId(request.getReviewerId());
        officer.setReviewTime(new Date());
        officer.setReviewComment(request.getComment());

        return this.updateById(officer);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SafetyOfficerPenalty handleAccident(SafetyOfficerPenaltyRequest request) {
        if (request == null || request.getOfficerId() == null) {
            throw new RuntimeException("安全员ID不能为空");
        }
        if (request.getLiabilityLevel() == null || request.getCasualtyType() == null) {
            throw new RuntimeException("责任等级和伤亡情况不能为空");
        }
        if (request.getLiabilityLevel() < LIABILITY_EQUAL) {
            throw new RuntimeException("非同等及以上责任事故无需暂停或取消安全员资格");
        }

        SafetyOfficer officer = this.getById(request.getOfficerId());
        if (officer == null) {
            throw new RuntimeException("安全员记录不存在");
        }

        SafetyOfficerPenalty penalty = new SafetyOfficerPenalty();
        BeanUtils.copyProperties(request, penalty);
        penalty.setEnterpriseId(officer.getEnterpriseId());
        penalty.setHandlerId(request.getHandlerId());
        penalty.setStartDate(new Date());

        if (CASUALTY_DEATH == request.getCasualtyType()) {
            penalty.setPenaltyType(PENALTY_CANCEL);
            officer.setStatus(STATUS_CANCELLED);
            officer.setSuspendStartDate(null);
            officer.setSuspendEndDate(null);
        } else if (CASUALTY_INJURY == request.getCasualtyType()) {
            long injuryCount = countInjuryPenalties(request.getOfficerId()) + 1;
            if (injuryCount >= 3) {
                penalty.setPenaltyType(PENALTY_CANCEL);
                officer.setStatus(STATUS_CANCELLED);
                officer.setSuspendStartDate(null);
                officer.setSuspendEndDate(null);
            } else {
                penalty.setPenaltyType(PENALTY_SUSPEND_HALF_YEAR);
                Date endDate = addMonths(penalty.getStartDate(), 6);
                penalty.setEndDate(endDate);
                officer.setStatus(STATUS_SUSPENDED);
                officer.setSuspendStartDate(penalty.getStartDate());
                officer.setSuspendEndDate(endDate);
            }
        } else if (CASUALTY_NONE == request.getCasualtyType()) {
            penalty.setPenaltyType(PENALTY_SUSPEND_THREE_MONTHS);
            Date endDate = addMonths(penalty.getStartDate(), 3);
            penalty.setEndDate(endDate);
            officer.setStatus(STATUS_SUSPENDED);
            officer.setSuspendStartDate(penalty.getStartDate());
            officer.setSuspendEndDate(endDate);
        } else {
            throw new RuntimeException("伤亡情况不正确");
        }

        if (!StringUtils.hasText(request.getReason())) {
            penalty.setReason(buildDefaultPenaltyReason(request));
        }
        officer.setPenaltyReason(penalty.getReason());

        safetyOfficerPenaltyService.save(penalty);
        this.updateById(officer);
        return penalty;
    }

    private void validateOfficerBaseInfo(SafetyOfficer officer) {
        if (!StringUtils.hasText(officer.getOfficerName())) {
            throw new RuntimeException("安全员姓名不能为空");
        }
        if (!StringUtils.hasText(officer.getIdCardNo())) {
            throw new RuntimeException("身份证号不能为空");
        }
        if (!StringUtils.hasText(officer.getLicenseType())) {
            throw new RuntimeException("驾驶证类型不能为空");
        }
        if (officer.getAge() == null || officer.getAge() < 21 || officer.getAge() > 50) {
            throw new RuntimeException("安全员年龄需在21周岁以上且不超过50周岁");
        }
        if (!isAllowedLicenseType(officer.getLicenseType())) {
            throw new RuntimeException("驾驶证类型需为C1、C2或A、B类驾驶证");
        }
        if (officer.getFirstLicenseDate() == null || monthsBetween(officer.getFirstLicenseDate(), new Date()) < 36) {
            throw new RuntimeException("安全员驾龄需不少于3年");
        }
    }

    private void validateOfficerEligibility(SafetyOfficer officer) {
        validateOfficerBaseInfo(officer);
        requireTrue(officer.getNoFullScoreRecord(), "最近连续三个记分周期存在记满12分记录");
        requireTrue(officer.getNoMajorAccidentRecord(), "存在致人死亡或重伤的交通责任事故记录");
        requireTrue(officer.getNoDuiRecord(), "存在酒后或醉酒驾驶机动车记录");
        requireTrue(officer.getNoCrimeRecord(), "存在犯罪记录");
        requireTrue(officer.getHealthy(), "存在可能危及行车安全的疾病史");
        requireTrue(officer.getNoAlcoholDrugRecord(), "存在酗酒、吸毒行为记录");
        requireText(officer.getIdCardUrl(), "请上传身份证材料");
        requireText(officer.getDriverLicenseUrl(), "请上传机动车驾驶证材料");
        requireText(officer.getHealthCertificateUrl(), "请上传机动车驾驶人身体条件证明");
        requireText(officer.getNoCrimeCertificateUrl(), "请上传无犯罪记录证明");
        requireText(officer.getNoViolationAccidentCertificateUrl(), "请上传无相应交通违法及事故证明");
        requireText(officer.getNoAlcoholDrugCertificateUrl(), "请上传无酗酒、吸毒记录证明");
    }

    private boolean isAllowedLicenseType(String licenseType) {
        String normalized = licenseType == null ? "" : licenseType.trim().toUpperCase();
        return "C1".equals(normalized)
                || "C2".equals(normalized)
                || normalized.startsWith("A")
                || normalized.startsWith("B");
    }

    private void requireTrue(Integer value, String message) {
        if (value == null || value != 1) {
            throw new RuntimeException(message);
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new RuntimeException(message);
        }
    }

    private long countInjuryPenalties(Long officerId) {
        return safetyOfficerPenaltyService.count(new LambdaQueryWrapper<SafetyOfficerPenalty>()
                .eq(SafetyOfficerPenalty::getOfficerId, officerId)
                .eq(SafetyOfficerPenalty::getCasualtyType, CASUALTY_INJURY)
                .ge(SafetyOfficerPenalty::getLiabilityLevel, LIABILITY_EQUAL));
    }

    private int monthsBetween(Date start, Date end) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        return (endCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)) * 12
                + endCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH);
    }

    private Date addMonths(Date date, int months) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    private String buildDefaultPenaltyReason(SafetyOfficerPenaltyRequest request) {
        if (request.getCasualtyType() == CASUALTY_DEATH) {
            return "发生同等及以上责任交通事故致人死亡，取消安全员资格";
        }
        if (request.getCasualtyType() == CASUALTY_INJURY) {
            return "发生同等及以上责任交通事故致人受伤，按规定处理安全员资格";
        }
        return "发生同等及以上责任交通事故，暂停安全员资格3个月";
    }
}
