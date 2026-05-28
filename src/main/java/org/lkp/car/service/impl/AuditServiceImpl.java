package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.lkp.car.dto.AuditTaskVO;
import org.lkp.car.entity.ApprovalRecord;
import org.lkp.car.entity.EnterpriseInfo;
import org.lkp.car.entity.LicenseApplication;
import org.lkp.car.service.ApprovalRecordService;
import org.lkp.car.service.AuditService;
import org.lkp.car.service.EnterpriseInfoService;
import org.lkp.car.service.LicenseApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一审核任务服务
 *
 * <p>企业资质（businessType=2）：
 * <ul>
 *   <li>待审核：主表 audit_status=0，或最新留痕为「提交」；驳回后重新提交亦可见</li>
 *   <li>已处理：当前民警留痕中存在通过/驳回记录</li>
 * </ul>
 */
@Service
public class AuditServiceImpl implements AuditService {

    private static final int BUSINESS_LICENSE = 1;
    private static final int BUSINESS_ENTERPRISE = 2;

    private static final int ACTION_SUBMIT = 1;
    private static final int ACTION_PASS = 2;
    private static final int ACTION_REJECT = 3;

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    @Autowired
    private LicenseApplicationService licenseApplicationService;

    @Override
    public List<AuditTaskVO> listTasks(Long reviewerId, boolean isProcessed, Integer businessType) {
        List<AuditTaskVO> tasks;
        if (isProcessed) {
            tasks = listProcessed(reviewerId, businessType);
        } else {
            tasks = listPending(businessType);
        }
        tasks.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return tasks;
    }

    // ======================== 待审核 ========================

    private List<AuditTaskVO> listPending(Integer businessType) {
        List<AuditTaskVO> tasks = new ArrayList<>();
        if (businessType == null || businessType == BUSINESS_ENTERPRISE) {
            tasks.addAll(listEnterprisePending());
        }
        if (businessType == null || businessType == BUSINESS_LICENSE) {
            tasks.addAll(listLicensePending());
        }
        return tasks;
    }

    /**
     * 企业资质待审核
     */
    private List<AuditTaskVO> listEnterprisePending() {
        Map<String, List<ApprovalRecord>> enterpriseRecords = groupRecordsByType(BUSINESS_ENTERPRISE);
        Map<Long, AuditTaskVO> taskMap = new LinkedHashMap<>();

        // 1. 留痕驱动：最新一条为「提交」的企业
        for (Map.Entry<String, List<ApprovalRecord>> entry : enterpriseRecords.entrySet()) {
            ApprovalRecord latest = latestRecord(entry.getValue());
            if (latest == null || latest.getActionType() != ACTION_SUBMIT) {
                continue;
            }
            EnterpriseInfo ent = enterpriseInfoService.getById(latest.getApplyId());
            if (ent == null) {
                continue;
            }
            taskMap.put(ent.getEnterpriseId(), toEnterprisePendingVo(ent, latest.getCreateTime()));
        }

        // 2. 主表补充：audit_status=0 且尚未出现在列表中的企业（已注册、待审）
        List<EnterpriseInfo> pendingOnTable = enterpriseInfoService.list(
                new LambdaQueryWrapper<EnterpriseInfo>()
                        .and(w -> w.eq(EnterpriseInfo::getAuditStatus, 0)
                                .or()
                                .isNull(EnterpriseInfo::getAuditStatus))
        );
        for (EnterpriseInfo ent : pendingOnTable) {
            if (taskMap.containsKey(ent.getEnterpriseId())) {
                continue;
            }
            String key = businessKey(BUSINESS_ENTERPRISE, ent.getEnterpriseId());
            List<ApprovalRecord> records = enterpriseRecords.get(key);
            if (records == null || records.isEmpty()) {
                taskMap.put(ent.getEnterpriseId(), toEnterprisePendingVo(ent, ent.getCreateTime()));
                continue;
            }
            ApprovalRecord latest = latestRecord(records);
            if (latest != null && latest.getActionType() == ACTION_SUBMIT) {
                taskMap.put(ent.getEnterpriseId(), toEnterprisePendingVo(ent, latest.getCreateTime()));
            }
        }

        return new ArrayList<>(taskMap.values());
    }

    /**
     * 号牌待审核：最新留痕为「提交」
     */
    private List<AuditTaskVO> listLicensePending() {
        Map<String, List<ApprovalRecord>> licenseRecords = groupRecordsByType(BUSINESS_LICENSE);
        List<AuditTaskVO> tasks = new ArrayList<>();

        for (Map.Entry<String, List<ApprovalRecord>> entry : licenseRecords.entrySet()) {
            ApprovalRecord latest = latestRecord(entry.getValue());
            if (latest == null || latest.getActionType() != ACTION_SUBMIT) {
                continue;
            }
            LicenseApplication app = licenseApplicationService.getById(latest.getApplyId());
            if (app != null) {
                tasks.add(toLicenseVo(app, latest.getCreateTime(), true));
            }
        }
        return tasks;
    }

    // ======================== 已处理 ========================

    private List<AuditTaskVO> listProcessed(Long reviewerId, Integer businessType) {
        List<AuditTaskVO> tasks = new ArrayList<>();
        if (businessType == null || businessType == BUSINESS_ENTERPRISE) {
            tasks.addAll(listEnterpriseProcessed(reviewerId));
        }
        if (businessType == null || businessType == BUSINESS_LICENSE) {
            tasks.addAll(listLicenseProcessed(reviewerId));
        }
        return tasks;
    }

    /**
     * 企业资质已处理：当前民警做过通过/驳回
     */
    private List<AuditTaskVO> listEnterpriseProcessed(Long reviewerId) {
        List<ApprovalRecord> records = approvalRecordService.list(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getReviewerId, reviewerId)
                        .eq(ApprovalRecord::getBusinessType, BUSINESS_ENTERPRISE)
                        .in(ApprovalRecord::getActionType, ACTION_PASS, ACTION_REJECT)
                        .orderByDesc(ApprovalRecord::getCreateTime)
        );

        Map<Long, ApprovalRecord> latestByEnterprise = new LinkedHashMap<>();
        for (ApprovalRecord record : records) {
            latestByEnterprise.putIfAbsent(record.getApplyId(), record);
        }

        List<AuditTaskVO> tasks = new ArrayList<>();
        for (ApprovalRecord record : latestByEnterprise.values()) {
            EnterpriseInfo ent = enterpriseInfoService.getById(record.getApplyId());
            if (ent != null) {
                tasks.add(toEnterpriseProcessedVo(ent, record));
            }
        }
        return tasks;
    }

    /**
     * 号牌已处理：当前民警做过通过/驳回
     */
    private List<AuditTaskVO> listLicenseProcessed(Long reviewerId) {
        List<ApprovalRecord> records = approvalRecordService.list(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getReviewerId, reviewerId)
                        .eq(ApprovalRecord::getBusinessType, BUSINESS_LICENSE)
                        .in(ApprovalRecord::getActionType, ACTION_PASS, ACTION_REJECT)
                        .orderByDesc(ApprovalRecord::getCreateTime)
        );

        Map<Long, ApprovalRecord> latestByApply = new LinkedHashMap<>();
        for (ApprovalRecord record : records) {
            latestByApply.putIfAbsent(record.getApplyId(), record);
        }

        List<AuditTaskVO> tasks = new ArrayList<>();
        for (ApprovalRecord record : latestByApply.values()) {
            LicenseApplication app = licenseApplicationService.getById(record.getApplyId());
            if (app != null) {
                tasks.add(toLicenseProcessedVo(app, record));
            }
        }
        return tasks;
    }

    // ======================== 工具方法 ========================

    private Map<String, List<ApprovalRecord>> groupRecordsByType(int businessType) {
        List<ApprovalRecord> allRecords = approvalRecordService.list(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getBusinessType, businessType)
                        .orderByAsc(ApprovalRecord::getCreateTime)
        );
        Map<String, List<ApprovalRecord>> grouped = new LinkedHashMap<>();
        for (ApprovalRecord record : allRecords) {
            String key = businessKey(record.getBusinessType(), record.getApplyId());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(record);
        }
        return grouped;
    }

    private ApprovalRecord latestRecord(List<ApprovalRecord> records) {
        return records.stream()
                .max(Comparator.comparing(ApprovalRecord::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private String businessKey(Integer businessType, Long applyId) {
        return businessType + ":" + applyId;
    }

    private AuditTaskVO toEnterprisePendingVo(EnterpriseInfo ent, Date sortTime) {
        AuditTaskVO vo = new AuditTaskVO();
        vo.setId(ent.getEnterpriseId());
        vo.setBusinessType(BUSINESS_ENTERPRISE);
        vo.setTitle(ent.getEnterpriseName());
        vo.setStatus(0);
        vo.setStatusDesc("待审核");
        vo.setCreateTime(sortTime);
        return vo;
    }

    private AuditTaskVO toEnterpriseProcessedVo(EnterpriseInfo ent, ApprovalRecord record) {
        AuditTaskVO vo = new AuditTaskVO();
        vo.setId(ent.getEnterpriseId());
        vo.setBusinessType(BUSINESS_ENTERPRISE);
        vo.setTitle(ent.getEnterpriseName());
        vo.setCreateTime(record.getCreateTime());
        if (record.getActionType() == ACTION_PASS) {
            vo.setStatus(1);
            vo.setStatusDesc("通过");
        } else {
            vo.setStatus(2);
            vo.setStatusDesc("驳回");
        }
        return vo;
    }

    private AuditTaskVO toLicenseVo(LicenseApplication app, Date sortTime, boolean pending) {
        AuditTaskVO vo = new AuditTaskVO();
        vo.setId(app.getApplyId());
        vo.setBusinessType(BUSINESS_LICENSE);
        vo.setTitle("上牌申请: " + app.getVin());
        vo.setCreateTime(sortTime);
        if (pending) {
            vo.setStatus(app.getFlowStatus());
            vo.setStatusDesc(getLicenseStatusDesc(app.getFlowStatus()));
        }
        return vo;
    }

    private AuditTaskVO toLicenseProcessedVo(LicenseApplication app, ApprovalRecord record) {
        AuditTaskVO vo = new AuditTaskVO();
        vo.setId(app.getApplyId());
        vo.setBusinessType(BUSINESS_LICENSE);
        vo.setTitle("上牌申请: " + app.getVin());
        vo.setCreateTime(record.getCreateTime());
        if (record.getActionType() == ACTION_PASS) {
            vo.setStatus(1);
            vo.setStatusDesc("通过");
        } else {
            vo.setStatus(2);
            vo.setStatusDesc("驳回");
        }
        return vo;
    }

    private String getLicenseStatusDesc(Integer status) {
        if (status == null) {
            return "待提交";
        }
        switch (status) {
            case 1: return "初审中";
            case 2: return "终审中";
            case 3: return "待查验";
            case 4: return "已发牌";
            case 5: return "已驳回";
            default: return "待提交";
        }
    }
}
