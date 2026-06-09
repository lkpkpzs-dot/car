package org.lkp.car.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
            // 已绑定企业
            EnterpriseInfo enterprise = this.getById(user.getAuthEnterpriseId());
            if (enterprise != null) {
                response.setEnterpriseName(enterprise.getEnterpriseName());
                response.setQualificationStatus(enterprise.getAuditStatus());
            }
        } else {
            // 尚未绑定：查询最近一次申请记录
            ApprovalRecord latestApply = approvalRecordService.getOne(
                    new LambdaQueryWrapper<ApprovalRecord>()
                            .eq(ApprovalRecord::getApplicantId, userId)
                            .eq(ApprovalRecord::getBusinessType, BUSINESS_ENTERPRISE)
                            .eq(ApprovalRecord::getActionType, ACTION_SUBMIT)
                            .orderByDesc(ApprovalRecord::getCreateTime)
                            .last("LIMIT 1")
            );

            if (latestApply != null) {
                EnterpriseInfo enterprise = this.getById(latestApply.getApplyId());
                if (enterprise != null) {
                    response.setEnterpriseName(enterprise.getEnterpriseName());
                    response.setQualificationStatus(enterprise.getAuditStatus());
                }
            }
        }
        return response;
    }

    @Override
    public boolean save(EnterpriseInfo entity) {
        // 仅保存主表，不发起审批；发起申请请调用 apply()
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(EnterpriseApplyRequest applyRequest) {
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

        enterprise.setAuditStatus(0);

        if (enterprise.getEnterpriseId() == null) {
            // 新建申请：检查信用代码是否已存在
            LambdaQueryWrapper<EnterpriseInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EnterpriseInfo::getCreditCode, enterprise.getCreditCode());
            EnterpriseInfo existingByCreditCode = this.getOne(wrapper);
            if (existingByCreditCode != null) {
                throw new RuntimeException("该统一社会信用代码已提交过申请，请联系管理员或使用其他代码");
            }
            super.save(enterprise);
        } else {
            // 重新申请：检查信用代码是否被其他企业占用
            EnterpriseInfo existing = this.getById(enterprise.getEnterpriseId());
            if (existing == null) {
                throw new RuntimeException("企业信息不存在，无法重新申请");
            }
            // 检查信用代码是否被其他企业占用
            if (!existing.getCreditCode().equals(enterprise.getCreditCode())) {
                LambdaQueryWrapper<EnterpriseInfo> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(EnterpriseInfo::getCreditCode, enterprise.getCreditCode());
                EnterpriseInfo existingByCreditCode = this.getOne(wrapper);
                if (existingByCreditCode != null && !existingByCreditCode.getEnterpriseId().equals(enterprise.getEnterpriseId())) {
                    throw new RuntimeException("该统一社会信用代码已被其他企业使用");
                }
            }
            this.updateById(enterprise);
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setApplyId(enterprise.getEnterpriseId());
        record.setApplicantId(applyRequest.getApplicantId());
        record.setBusinessType(BUSINESS_ENTERPRISE);
        record.setNodeName("提交申请");
        record.setReviewerId(applyRequest.getApplicantId()); // 初始提交操作人也是申请人
        record.setActionType(ACTION_SUBMIT);
        record.setComment(StringUtils.hasText(applyRequest.getComment())
                ? applyRequest.getComment()
                : "企业提交资质认证申请");
        record.setSnapshotJson(buildSnapshotJson(enterprise));
        approvalRecordService.save(record);

        return enterprise.getEnterpriseId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(EnterpriseAuditRequest auditRequest) {
        SysUser reviewer = sysUserService.getById(auditRequest.getReviewerId());
        if (reviewer == null || reviewer.getRoleType() != 1) {
            throw new RuntimeException("操作失败：审核人必须是民警身份");
        }

        EnterpriseInfo enterprise = this.getById(auditRequest.getEnterpriseId());
        if (enterprise == null) {
            throw new RuntimeException("企业信息不存在");
        }

        enterprise.setAuditStatus(auditRequest.getAuditStatus());
        this.updateById(enterprise);

        // 如果审核通过，则需要更新申请人的 auth_enterprise_id
        if (auditRequest.getAuditStatus() == 1) {
            // 联表查询：从审批留痕中找到该企业的提交人（使用 applicant_id 字段）
            ApprovalRecord submitRecord = approvalRecordService.getOne(
                    new LambdaQueryWrapper<ApprovalRecord>()
                            .eq(ApprovalRecord::getApplyId, auditRequest.getEnterpriseId())
                            .eq(ApprovalRecord::getBusinessType, BUSINESS_ENTERPRISE)
                            .eq(ApprovalRecord::getActionType, ACTION_SUBMIT)
                            .orderByDesc(ApprovalRecord::getCreateTime)
                            .last("LIMIT 1")
            );

            if (submitRecord != null && submitRecord.getApplicantId() != null) {
                SysUser applicant = sysUserService.getById(submitRecord.getApplicantId());
                if (applicant != null) {
                    applicant.setAuthEnterpriseId(auditRequest.getEnterpriseId());
                    sysUserService.updateById(applicant);
                }
            }
        }

        ApprovalRecord record = new ApprovalRecord();
        record.setApplyId(auditRequest.getEnterpriseId());
        // 审核记录不需要 applicant_id，但为了保持逻辑清晰，此处不设置 applicant_id
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
