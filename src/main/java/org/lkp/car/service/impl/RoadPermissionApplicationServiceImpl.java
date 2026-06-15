package org.lkp.car.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.common.enums.VehicleStatusEnum;
import org.lkp.car.dto.RoadApplyRequest;
import org.lkp.car.dto.RoadAuditRequest;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.entity.SafetyOfficer;
import org.lkp.car.entity.SysUser;
import org.lkp.car.entity.VehicleInfo;
import org.lkp.car.mapper.RoadPermissionApplicationMapper;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.service.SafetyOfficerService;
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
 * 道路测试/通行申请服务实现类
 * <p>
 * 处理道路测试和通行申请业务，包括：
 * 1. 企业提交道路申请（需绑定安全员）
 * 2. 民警审核道路申请
 * 3. 申请状态管理和流转
 * 4. 关联车辆查验状态查询
 * </p>
 * <p>
 * 关键约束：
 * - 每个安全员最多关联3辆车（含档案车辆和待审核/已通过申请）
 * - 只有已审核通过的道路申请才能进行车辆查验
 * </p>
 */
@Service
public class RoadPermissionApplicationServiceImpl extends ServiceImpl<RoadPermissionApplicationMapper, RoadPermissionApplication> implements RoadPermissionApplicationService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    @Lazy
    private VehicleInfoService vehicleInfoService;

    @Autowired
    @Lazy
    private SafetyOfficerService safetyOfficerService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(RoadApplyRequest applyRequest) {
        validateBasicParams(applyRequest);
        
        SysUser applicant = validateApplicantPermission(applyRequest);
        
        validateSafetyOfficer(applyRequest);
        
        RoadPermissionApplication application = getOrCreateApplication(applyRequest, applicant);
        
        prepareApplicationForSave(applyRequest, application);
        
        this.saveOrUpdate(application);
        return application.getId();
    }

    /**
     * 校验基本参数
     */
    private void validateBasicParams(RoadApplyRequest applyRequest) {
        if (applyRequest.getEnterpriseId() == null || applyRequest.getApplicantId() == null) {
            throw new RuntimeException("企业ID和申请人ID不能为空");
        }
    }

    /**
     * 校验申请人权限
     * @return 申请人用户信息
     */
    private SysUser validateApplicantPermission(RoadApplyRequest applyRequest) {
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
        return applicant;
    }

    /**
     * 校验安全员信息
     */
    private void validateSafetyOfficer(RoadApplyRequest applyRequest) {
        if (applyRequest.getOfficerId() == null) {
            return;
        }

        SafetyOfficer officer = safetyOfficerService.getById(applyRequest.getOfficerId());
        if (officer == null) {
            throw new RuntimeException("安全员不存在");
        }
        
        validateOfficerEnterprise(officer, applyRequest.getEnterpriseId());
        validateOfficerStatus(officer);
        validateOfficerVehicleLimit(applyRequest);
    }

    /**
     * 校验安全员所属企业
     */
    private void validateOfficerEnterprise(SafetyOfficer officer, Long enterpriseId) {
        if (!officer.getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("安全员不属于当前企业");
        }
    }

    /**
     * 校验安全员状态
     */
    private void validateOfficerStatus(SafetyOfficer officer) {
        if (officer.getStatus() != 1) {
            throw new RuntimeException("安全员资质无效，请选择有效状态的安全员");
        }
    }

    /**
     * 校验安全员车辆数量限制
     */
    private void validateOfficerVehicleLimit(RoadApplyRequest applyRequest) {
        int archiveCount = safetyOfficerService.getOfficerVehicleCount(applyRequest.getOfficerId());
        int applyCount = countPendingApplicationsByOfficer(applyRequest.getOfficerId(), applyRequest.getId());
        
        if (archiveCount + applyCount >= 3) {
            throw new RuntimeException("该安全员已关联 " + (archiveCount + applyCount) + 
                " 辆车，最多只能关联 3 辆，请选择其他安全员");
        }
    }

    /**
     * 统计安全员待审核/已通过的申请数量
     */
    private int countPendingApplicationsByOfficer(Long officerId, Long excludeId) {
        LambdaQueryWrapper<RoadPermissionApplication> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(RoadPermissionApplication::getOfficerId, officerId)
                   .in(RoadPermissionApplication::getStatus, 1, 2);
        if (excludeId != null) {
            applyWrapper.ne(RoadPermissionApplication::getId, excludeId);
        }
        return (int) this.count(applyWrapper);
    }

    /**
     * 获取或创建申请对象
     */
    private RoadPermissionApplication getOrCreateApplication(RoadApplyRequest applyRequest, SysUser applicant) {
        if (applyRequest.getId() != null) {
            return getExistingApplication(applyRequest, applicant);
        }
        return new RoadPermissionApplication();
    }

    /**
     * 获取已有申请并校验权限
     */
    private RoadPermissionApplication getExistingApplication(RoadApplyRequest applyRequest, SysUser applicant) {
        RoadPermissionApplication application = this.getById(applyRequest.getId());
        if (application == null) {
            throw new RuntimeException("操作失败：未找到原申请记录");
        }
        if (!application.getApplicantId().equals(applicant.getUserId())) {
            throw new RuntimeException("操作失败：您无权修改他人的申请记录");
        }
        if (application.getStatus() != 3 && application.getStatus() != 0) {
            throw new RuntimeException("操作失败：当前申请状态不允许重新提交");
        }
        return application;
    }

    /**
     * 准备申请对象用于保存
     */
    private void prepareApplicationForSave(RoadApplyRequest applyRequest, RoadPermissionApplication application) {
        BeanUtils.copyProperties(applyRequest, application);
        
        application.setStatus(1);
        application.setAuditComment(null);
        application.setRejectReason(null);
        application.setAuditTime(null);
        application.setReviewerId(null);

        convertDocsToJson(applyRequest, application);
    }

    /**
     * 将文档列表转换为 JSON 存储
     */
    private void convertDocsToJson(RoadApplyRequest applyRequest, RoadPermissionApplication application) {
        application.setDocVehicleCert(JSONUtil.toJsonStr(applyRequest.getDocVehicleCert()));
        application.setDocOwnerId(JSONUtil.toJsonStr(applyRequest.getDocOwnerId()));
        application.setDocSafetyInspection(JSONUtil.toJsonStr(applyRequest.getDocSafetyInspection()));
        application.setDocInsurance(JSONUtil.toJsonStr(applyRequest.getDocInsurance()));
        application.setDocOwnerProxy(JSONUtil.toJsonStr(applyRequest.getDocOwnerProxy()));
        application.setDocAgentId(JSONUtil.toJsonStr(applyRequest.getDocAgentId()));
        application.setDocSafetyDeclaration(JSONUtil.toJsonStr(applyRequest.getDocSafetyDeclaration()));
        application.setDocApplicationDoc(JSONUtil.toJsonStr(applyRequest.getDocApplicationDoc()));
    }

    @Override
    public List<RoadPermissionApplicationVO> listMyApplications(Long applicantId) {
        List<RoadPermissionApplication> list = this.list(new LambdaQueryWrapper<RoadPermissionApplication>()
                .eq(RoadPermissionApplication::getApplicantId, applicantId)
                .orderByDesc(RoadPermissionApplication::getCreateTime));
        
        return list.stream().map(this::convertToVOAndFillInfo).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<RoadPermissionApplicationVO> listByEnterprise(Long enterpriseId) {
        List<RoadPermissionApplication> list = this.list(new LambdaQueryWrapper<RoadPermissionApplication>()
                .eq(RoadPermissionApplication::getEnterpriseId, enterpriseId)
                .orderByDesc(RoadPermissionApplication::getCreateTime));
        
        return list.stream().map(this::convertToVOAndFillInfo).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public RoadPermissionApplicationVO getDetail(Long id) {
        RoadPermissionApplication application = this.getById(id);
        if (application == null) {
            return null;
        }
        return convertToVOAndFillInfo(application);
    }

    /**
     * 将 Entity 转换为 VO 并填充额外信息
     */
    private RoadPermissionApplicationVO convertToVOAndFillInfo(RoadPermissionApplication application) {
        RoadPermissionApplicationVO vo = RoadPermissionApplicationVO.fromEntity(application);
        fillInspectionStatus(vo);
        fillOfficerInfo(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(RoadAuditRequest auditRequest) {
        // 1. 校验审核人是否为民警
        SysUser reviewer = sysUserService.getById(auditRequest.getReviewerId());
        if (reviewer == null || reviewer.getRoleType() != RoleEnum.POLICE_CODE) {
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
        
        for (RoadPermissionApplicationVO vo : voList) {
            fillInspectionStatus(vo);
            fillOfficerInfo(vo);
        }
        
        return voList;
    }

    /**
     * 填充查验状态信息到 VO
     */
    private void fillInspectionStatus(RoadPermissionApplicationVO vo) {
        LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleInfo::getApplicationId, vo.getId());
        VehicleInfo vehicleInfo = vehicleInfoService.getOne(wrapper);
        
        if (vehicleInfo != null) {
            mapVehicleStatusToVo(vehicleInfo, vo);
        } else {
            setDefaultInspectionStatus(vo);
        }
    }

    /**
     * 映射车辆状态到 VO
     */
    private void mapVehicleStatusToVo(VehicleInfo vehicleInfo, RoadPermissionApplicationVO vo) {
        if (VehicleStatusEnum.PASSED.getCode().equals(vehicleInfo.getStatus())) {
            vo.setInspectionStatus(2);
            vo.setInspectionStatusLabel("已通过");
            vo.setInspectionStatusClass("status-approved");
        } else if (VehicleStatusEnum.REJECTED.getCode().equals(vehicleInfo.getStatus())) {
            vo.setInspectionStatus(3);
            vo.setInspectionStatusLabel("已驳回");
            vo.setInspectionStatusClass("status-rejected");
        } else {
            setDefaultInspectionStatus(vo);
        }
    }

    /**
     * 设置默认查验状态（未查验）
     */
    private void setDefaultInspectionStatus(RoadPermissionApplicationVO vo) {
        vo.setInspectionStatus(1);
        vo.setInspectionStatusLabel("未查验");
        vo.setInspectionStatusClass("");
    }

    /**
     * 填充安全员信息到 VO
     */
    private void fillOfficerInfo(RoadPermissionApplicationVO vo) {
        if (vo.getOfficerId() != null) {
            SafetyOfficer officer = safetyOfficerService.getById(vo.getOfficerId());
            if (officer != null) {
                vo.setOfficerName(officer.getOfficerName());
            }
        }
    }
}
