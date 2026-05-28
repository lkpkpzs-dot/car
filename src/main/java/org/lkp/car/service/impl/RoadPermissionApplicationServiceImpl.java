package org.lkp.car.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.common.enums.VehicleStatusEnum;
import org.lkp.car.dto.RoadApplyRequest;
import org.lkp.car.dto.RoadAuditRequest;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.entity.SysUser;
import org.lkp.car.entity.VehicleInfo;
import org.lkp.car.mapper.RoadPermissionApplicationMapper;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.service.SysUserService;
import org.lkp.car.service.VehicleInfoService;
import org.lkp.car.vo.RoadPermissionApplicationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 道路测试/应用申请 服务实现类
 */
@Service
public class RoadPermissionApplicationServiceImpl extends ServiceImpl<RoadPermissionApplicationMapper, RoadPermissionApplication> implements RoadPermissionApplicationService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    @Lazy
    private VehicleInfoService vehicleInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(RoadApplyRequest applyRequest) {
        if (applyRequest.getEnterpriseId() == null || applyRequest.getApplicantId() == null) {
            throw new RuntimeException("企业ID和申请人ID不能为空");
        }

        // 权限校验：只有绑定了企业的代办人可以申请，禁止普通市民申请
        SysUser applicant = sysUserService.getById(applyRequest.getApplicantId());
        if (applicant == null) {
            throw new RuntimeException("申请人不存在");
        }
        if (applicant.getAuthEnterpriseId() == null) {
            throw new RuntimeException("操作失败：您尚未绑定企业，无权发起道路测试申请");
        }
        if (!applicant.getAuthEnterpriseId().equals(applyRequest.getEnterpriseId())) {
            throw new RuntimeException("操作失败：您无权代表该企业发起申请");
        }

        RoadPermissionApplication application;
        if (applyRequest.getId() != null) {
            // 重新提交/修改逻辑
            application = this.getById(applyRequest.getId());
            if (application == null) {
                throw new RuntimeException("操作失败：未找到原申请记录");
            }
            // 权限校验：只能修改自己的申请
            if (!application.getApplicantId().equals(applyRequest.getApplicantId())) {
                throw new RuntimeException("操作失败：您无权修改他人的申请记录");
            }
            // 状态校验：只有“被驳回(3)”或“草稿(0)”的申请可以重新提交
            if (application.getStatus() != 3 && application.getStatus() != 0) {
                throw new RuntimeException("操作失败：当前申请状态不允许重新提交");
            }
        } else {
            // 新增申请逻辑
            application = new RoadPermissionApplication();
        }

        BeanUtils.copyProperties(applyRequest, application);

        // 设置/重置状态为待审核 (1)
        application.setStatus(1);
        // 清空之前的审核意见和驳回原因
        application.setAuditComment(null);
        application.setRejectReason(null);
        application.setAuditTime(null);
        application.setReviewerId(null);

        // 处理材料列表转 JSON 存储
        application.setDocVehicleCert(JSONUtil.toJsonStr(applyRequest.getDocVehicleCert()));
        application.setDocOwnerId(JSONUtil.toJsonStr(applyRequest.getDocOwnerId()));
        application.setDocSafetyInspection(JSONUtil.toJsonStr(applyRequest.getDocSafetyInspection()));
        application.setDocInsurance(JSONUtil.toJsonStr(applyRequest.getDocInsurance()));
        application.setDocOwnerProxy(JSONUtil.toJsonStr(applyRequest.getDocOwnerProxy()));
        application.setDocAgentId(JSONUtil.toJsonStr(applyRequest.getDocAgentId()));
        application.setDocSafetyDeclaration(JSONUtil.toJsonStr(applyRequest.getDocSafetyDeclaration()));
        application.setDocApplicationDoc(JSONUtil.toJsonStr(applyRequest.getDocApplicationDoc()));

        this.saveOrUpdate(application);
        return application.getId();
    }

    @Override
    public List<RoadPermissionApplication> listMyApplications(Long applicantId) {
        return this.list(new LambdaQueryWrapper<RoadPermissionApplication>()
                .eq(RoadPermissionApplication::getApplicantId, applicantId)
                .orderByDesc(RoadPermissionApplication::getCreateTime));
    }

    @Override
    public List<RoadPermissionApplication> listByEnterprise(Long enterpriseId) {
        return this.list(new LambdaQueryWrapper<RoadPermissionApplication>()
                .eq(RoadPermissionApplication::getEnterpriseId, enterpriseId)
                .orderByDesc(RoadPermissionApplication::getCreateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(RoadAuditRequest auditRequest) {
        // 1. 校验审核人是否为民警 (roleType = 1)
        SysUser reviewer = sysUserService.getById(auditRequest.getReviewerId());
        if (reviewer == null || reviewer.getRoleType() != 1) {
            throw new RuntimeException("操作失败：审核人必须是民警身份");
        }

        // 2. 校验申请记录是否存在
        RoadPermissionApplication application = this.getById(auditRequest.getApplicationId());
        if (application == null) {
            throw new RuntimeException("申请记录不存在");
        }

        // 3. 更新状态和审核信息
        application.setStatus(auditRequest.getStatus());
        application.setReviewerId(auditRequest.getReviewerId());
        application.setAuditComment(auditRequest.getAuditComment());
        application.setAuditTime(new Date());

        // 如果是驳回，保存驳回原因
        if (auditRequest.getStatus() == 3) {
            application.setRejectReason(auditRequest.getRejectReason());
        } else {
            application.setRejectReason(null);
        }

        return this.updateById(application);
    }

    @Override
    public List<RoadPermissionApplicationVO> listAll(Integer status, Integer type) {
        List<RoadPermissionApplicationVO> voList = this.baseMapper.listWithEnterpriseName(status, type);
        
        // 遍历每个申请，查询并填充查验状态
        for (RoadPermissionApplicationVO vo : voList) {
            LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VehicleInfo::getApplicationId, vo.getId());
            VehicleInfo vehicleInfo = vehicleInfoService.getOne(wrapper);
            
            if (vehicleInfo != null) {
                // 映射后端状态到前端状态：
                // 后端：0=待审核(PENDING)，1=通过(PASSED)，2=驳回(REJECTED)
                // 前端：1=未查验，2=已通过，3=已驳回
                if (VehicleStatusEnum.PASSED.getCode().equals(vehicleInfo.getStatus())) {
                    vo.setInspectionStatus(2); // 通过
                    vo.setInspectionStatusLabel("已通过");
                    vo.setInspectionStatusClass("status-approved");
                } else if (VehicleStatusEnum.REJECTED.getCode().equals(vehicleInfo.getStatus())) {
                    vo.setInspectionStatus(3); // 驳回
                    vo.setInspectionStatusLabel("已驳回");
                    vo.setInspectionStatusClass("status-rejected");
                } else {
                    // 0=待审核，视为未查验
                    vo.setInspectionStatus(1);
                    vo.setInspectionStatusLabel("未查验");
                    vo.setInspectionStatusClass("");
                }
            } else {
                // 没有查验记录，默认为未查验
                vo.setInspectionStatus(1);
                vo.setInspectionStatusLabel("未查验");
                vo.setInspectionStatusClass("");
            }
        }
        
        return voList;
    }
}
