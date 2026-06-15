package org.lkp.car.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.common.cache.CacheConstants;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.dto.EnterpriseApplyRequest;
import org.lkp.car.dto.EnterpriseAuditRequest;
import org.lkp.car.dto.MyEnterpriseStatusResponse;
import org.lkp.car.entity.ApprovalRecord;
import org.lkp.car.entity.EnterpriseInfo;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.EnterpriseInfoMapper;
import org.lkp.car.service.ApprovalRecordService;
import org.lkp.car.service.EnterpriseInfoService;
import org.lkp.car.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 企业资质 服务实现类
 */
@Service
public class EnterpriseInfoServiceImpl extends ServiceImpl<EnterpriseInfoMapper, EnterpriseInfo> implements EnterpriseInfoService {

    private static final int BUSINESS_ENTERPRISE = 2;
    private static final int ACTION_SUBMIT = 1;

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    @Lazy
    private EnterpriseInfoService self;

    @Override
    @Cacheable(
            value = CacheConstants.LOCAL_ENTERPRISE,
            key = "#id",
            cacheManager = "localCacheManager",
            unless = "#result == null"
    )
    public EnterpriseInfo getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    @CacheEvict(value = CacheConstants.LOCAL_ENTERPRISE, key = "#entity.enterpriseId", cacheManager = "localCacheManager")
    public boolean updateById(EnterpriseInfo entity) {
        return super.updateById(entity);
    }

    @Override
    public MyEnterpriseStatusResponse getMyStatus(Long userId) {
        if (userId == null) {
            return new MyEnterpriseStatusResponse(null, null);
        }

        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return new MyEnterpriseStatusResponse(null, null);
        }

        MyEnterpriseStatusResponse response = new MyEnterpriseStatusResponse();

        if (user.getAuthEnterpriseId() != null) {
            fillEnterpriseStatusFromBoundEnterprise(user, response);
        } else {
            fillEnterpriseStatusFromLatestApply(userId, response);
        }

        return response;
    }

    /**
     * 从已绑定企业填充状态信息
     */
    private void fillEnterpriseStatusFromBoundEnterprise(SysUser user, MyEnterpriseStatusResponse response) {
        EnterpriseInfo enterprise = self.getById(user.getAuthEnterpriseId());
        if (enterprise != null) {
            response.setEnterpriseName(enterprise.getEnterpriseName());
            response.setQualificationStatus(enterprise.getAuditStatus());
        }
    }

    /**
     * 从最近一次申请记录填充状态信息
     */
    private void fillEnterpriseStatusFromLatestApply(Long userId, MyEnterpriseStatusResponse response) {
        ApprovalRecord latestApply = findLatestEnterpriseApply(userId);
        if (latestApply == null) {
            return;
        }

        EnterpriseInfo enterprise = self.getById(latestApply.getApplyId());
        if (enterprise != null) {
            response.setEnterpriseName(enterprise.getEnterpriseName());
            response.setQualificationStatus(enterprise.getAuditStatus());
        }
    }

    /**
     * 查询用户最近一次企业申请记录
     */
    private ApprovalRecord findLatestEnterpriseApply(Long userId) {
        return approvalRecordService.getOne(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getApplicantId, userId)
                        .eq(ApprovalRecord::getBusinessType, BUSINESS_ENTERPRISE)
                        .eq(ApprovalRecord::getActionType, ACTION_SUBMIT)
                        .orderByDesc(ApprovalRecord::getCreateTime)
                        .last("LIMIT 1")
        );
    }

    @Override
    public boolean save(EnterpriseInfo entity) {
        // 仅保存主表，不发起审批；发起申请请调用 apply()
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(EnterpriseApplyRequest applyRequest) {
        validateApplyRequest(applyRequest);

        EnterpriseInfo enterprise = applyRequest.getEnterprise();
        enterprise.setAuditStatus(0);

        saveOrUpdateEnterprise(enterprise);

        createApprovalRecord(applyRequest, enterprise);

        return enterprise.getEnterpriseId();
    }

    /**
     * 校验申请请求参数
     */
    private void validateApplyRequest(EnterpriseApplyRequest applyRequest) {
        if (applyRequest == null || applyRequest.getEnterprise() == null) {
            throw new RuntimeException("企业信息不能为空");
        }
        if (applyRequest.getApplicantId() == null) {
            throw new RuntimeException("申请人ID不能为空");
        }

        EnterpriseInfo enterprise = applyRequest.getEnterprise();
        if (!StringUtils.hasText(enterprise.getEnterpriseName())) {
            throw new RuntimeException("企业名称不能为空");
        }
        if (!StringUtils.hasText(enterprise.getCreditCode())) {
            throw new RuntimeException("统一社会信用代码不能为空");
        }

        SysUser applicant = sysUserService.getById(applyRequest.getApplicantId());
        if (applicant == null) {
            throw new RuntimeException("申请人不存在");
        }
    }

    /**
     * 保存或更新企业信息
     */
    private void saveOrUpdateEnterprise(EnterpriseInfo enterprise) {
        if (enterprise.getEnterpriseId() == null) {
            saveNewEnterprise(enterprise);
        } else {
            updateExistingEnterprise(enterprise);
        }
    }

    /**
     * 保存新企业（新建申请）
     */
    private void saveNewEnterprise(EnterpriseInfo enterprise) {
        checkCreditCodeUnique(enterprise.getCreditCode(), null);
        super.save(enterprise);
    }

    /**
     * 更新现有企业（重新申请）
     */
    private void updateExistingEnterprise(EnterpriseInfo enterprise) {
        EnterpriseInfo existing = self.getById(enterprise.getEnterpriseId());
        if (existing == null) {
            throw new RuntimeException("企业信息不存在，无法重新申请");
        }
        // 检查信用代码是否被其他企业占用
        if (!existing.getCreditCode().equals(enterprise.getCreditCode())) {
            checkCreditCodeUnique(enterprise.getCreditCode(), enterprise.getEnterpriseId());
        }
        this.updateById(enterprise);
    }

    /**
     * 检查信用代码是否唯一
     * @param creditCode 信用代码
     * @param excludeId 排除的企业ID（更新时使用）
     */
    private void checkCreditCodeUnique(String creditCode, Long excludeId) {
        LambdaQueryWrapper<EnterpriseInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseInfo::getCreditCode, creditCode);
        EnterpriseInfo existingByCreditCode = this.getOne(wrapper);
        
        if (existingByCreditCode != null) {
            if (excludeId == null) {
                throw new RuntimeException("该统一社会信用代码已提交过申请，请联系管理员或使用其他代码");
            }
            if (!existingByCreditCode.getEnterpriseId().equals(excludeId)) {
                throw new RuntimeException("该统一社会信用代码已被其他企业使用");
            }
        }
    }

    /**
     * 创建审批记录
     */
    private void createApprovalRecord(EnterpriseApplyRequest applyRequest, EnterpriseInfo enterprise) {
        ApprovalRecord record = new ApprovalRecord();
        record.setApplyId(enterprise.getEnterpriseId());
        record.setApplicantId(applyRequest.getApplicantId());
        record.setBusinessType(BUSINESS_ENTERPRISE);
        record.setNodeName("提交申请");
        record.setReviewerId(applyRequest.getApplicantId());
        record.setActionType(ACTION_SUBMIT);
        record.setComment(StringUtils.hasText(applyRequest.getComment())
                ? applyRequest.getComment()
                : "企业提交资质认证申请");
        record.setSnapshotJson(buildSnapshotJson(enterprise));
        approvalRecordService.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(EnterpriseAuditRequest auditRequest) {
        validateAuditRequest(auditRequest);

        EnterpriseInfo enterprise = self.getById(auditRequest.getEnterpriseId());
        if (enterprise == null) {
            throw new RuntimeException("企业信息不存在");
        }

        updateEnterpriseAuditStatus(enterprise, auditRequest.getAuditStatus());

        if (auditRequest.getAuditStatus() == 1) {
            bindEnterpriseToApplicant(auditRequest.getEnterpriseId());
        }

        return createAuditRecord(auditRequest, enterprise);
    }

    /**
     * 校验审核请求参数
     */
    private void validateAuditRequest(EnterpriseAuditRequest auditRequest) {
        SysUser reviewer = sysUserService.getById(auditRequest.getReviewerId());
        if (reviewer == null || reviewer.getRoleType() != RoleEnum.POLICE_CODE) {
            throw new RuntimeException("操作失败：审核人必须是民警身份");
        }
    }

    /**
     * 更新企业审核状态
     */
    private void updateEnterpriseAuditStatus(EnterpriseInfo enterprise, Integer auditStatus) {
        enterprise.setAuditStatus(auditStatus);
        this.updateById(enterprise);
    }

    /**
     * 将企业绑定到申请人
     */
    private void bindEnterpriseToApplicant(Long enterpriseId) {
        ApprovalRecord submitRecord = findLatestSubmitRecord(enterpriseId);
        if (submitRecord == null || submitRecord.getApplicantId() == null) {
            return;
        }

        SysUser applicant = sysUserService.getById(submitRecord.getApplicantId());
        if (applicant != null) {
            applicant.setAuthEnterpriseId(enterpriseId);
            sysUserService.updateById(applicant);
        }
    }

    /**
     * 查询企业最近一次提交记录
     */
    private ApprovalRecord findLatestSubmitRecord(Long enterpriseId) {
        return approvalRecordService.getOne(
                new LambdaQueryWrapper<ApprovalRecord>()
                        .eq(ApprovalRecord::getApplyId, enterpriseId)
                        .eq(ApprovalRecord::getBusinessType, BUSINESS_ENTERPRISE)
                        .eq(ApprovalRecord::getActionType, ACTION_SUBMIT)
                        .orderByDesc(ApprovalRecord::getCreateTime)
                        .last("LIMIT 1")
        );
    }

    /**
     * 创建审核记录
     */
    private boolean createAuditRecord(EnterpriseAuditRequest auditRequest, EnterpriseInfo enterprise) {
        ApprovalRecord record = new ApprovalRecord();
        record.setApplyId(auditRequest.getEnterpriseId());
        record.setBusinessType(BUSINESS_ENTERPRISE);
        record.setNodeName("民警审核");
        record.setReviewerId(auditRequest.getReviewerId());
        record.setActionType(auditRequest.getAuditStatus() == 1 ? 2 : 3);
        record.setComment(auditRequest.getReason());
        record.setSnapshotJson(buildSnapshotJson(enterprise));

        return approvalRecordService.save(record);
    }

    private String buildSnapshotJson(EnterpriseInfo enterprise) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("enterpriseId", enterprise.getEnterpriseId());
        snapshot.put("enterpriseName", enterprise.getEnterpriseName());
        snapshot.put("creditCode", enterprise.getCreditCode());
        snapshot.put("legalPerson", enterprise.getLegalPerson());
        snapshot.put("contactPhone", enterprise.getContactPhone());
        return JSONUtil.toJsonStr(snapshot);
    }
}
