package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.common.enums.VehicleStatusEnum;
import org.lkp.car.dto.VehicleApplyRequest;
import org.lkp.car.dto.VehicleAuditRequest;
import org.lkp.car.dto.VehicleInspectionSubmitRequest;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.entity.SysUser;
import org.lkp.car.entity.VehicleInfo;
import org.lkp.car.mapper.VehicleInfoMapper;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.service.SysUserService;
import org.lkp.car.service.VehicleInfoService;
import org.lkp.car.service.CarArchiveService;
import org.lkp.car.vo.VehicleInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 车辆查验信息 服务实现类
 */
@Service
public class VehicleInfoServiceImpl extends ServiceImpl<VehicleInfoMapper, VehicleInfo> implements VehicleInfoService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @Autowired
    @Lazy
    private CarArchiveService carArchiveService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long apply(VehicleApplyRequest applyRequest) {
        // 1. 基础校验
        if (applyRequest.getUserId() == null || applyRequest.getEnterpriseId() == null) {
            throw new RuntimeException("提交人ID和企业ID不能为空");
        }

        // 2. 校验企业身份 (authEnterpriseId != null)
        SysUser user = sysUserService.getById(applyRequest.getUserId());
        if (user == null || user.getAuthEnterpriseId() == null) {
            throw new RuntimeException("只有完成企业资质认证的用户才能提交车辆查验");
        }

        // 3. 校验 VIN 唯一性 (排除当前记录)
        LambdaQueryWrapper<VehicleInfo> vinWrapper = new LambdaQueryWrapper<VehicleInfo>()
                .eq(VehicleInfo::getVin, applyRequest.getVin());
        if (applyRequest.getVehicleId() != null) {
            vinWrapper.ne(VehicleInfo::getVehicleId, applyRequest.getVehicleId());
        }
        if (this.count(vinWrapper) > 0) {
            throw new RuntimeException("车架号(VIN)已存在，请检查后重新输入");
        }

        VehicleInfo vehicle;
        if (applyRequest.getVehicleId() != null) {
            // 重新提交
            vehicle = this.getById(applyRequest.getVehicleId());
            if (vehicle == null) {
                throw new RuntimeException("原车辆记录不存在");
            }
        } else {
            // 新增申请
            vehicle = new VehicleInfo();
        }

        BeanUtils.copyProperties(applyRequest, vehicle);
        vehicle.setStatus(VehicleStatusEnum.PENDING.getCode());
        vehicle.setRejectReason(null); // 重置驳回原因

        this.saveOrUpdate(vehicle);
        return vehicle.getVehicleId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitInspection(VehicleInspectionSubmitRequest request) {
        // 1. 校验申请ID不能为空
        if (request.getApplicationId() == null) {
            throw new RuntimeException("申请ID不能为空");
        }

        // 2. 校验道路申请是否存在
        RoadPermissionApplication application = roadPermissionApplicationService.getById(request.getApplicationId());
        if (application == null) {
            throw new RuntimeException("道路申请记录不存在");
        }

        // 3. 校验道路申请是否已审核通过 (status = 2)
        if (application.getStatus() == null || application.getStatus() != 2) {
            throw new RuntimeException("只有已审核通过的道路申请才能进行查验");
        }

        // 4. 校验查验状态不能为空
        if (request.getAuditStatus() == null) {
            throw new RuntimeException("查验状态不能为空");
        }

        // 5. 如果是驳回，驳回原因必填
        if (request.getAuditStatus().equals(VehicleStatusEnum.REJECTED.getCode()) && !StringUtils.hasText(request.getRejectReason())) {
            throw new RuntimeException("驳回请填写原因");
        }

        // 6. 查询是否已存在该申请ID的查验记录
        LambdaQueryWrapper<VehicleInfo> existWrapper = new LambdaQueryWrapper<VehicleInfo>()
                .eq(VehicleInfo::getApplicationId, request.getApplicationId());
        VehicleInfo existVehicle = this.getOne(existWrapper);
        
        // 7. 如果已有查验记录且已通过，不允许再次查验
        if (existVehicle != null && existVehicle.getStatus().equals(VehicleStatusEnum.PASSED.getCode())) {
            throw new RuntimeException("该道路申请已完成查验，不可重复提交");
        }

        VehicleInfo vehicle;
        if (existVehicle != null) {
            // 更新已存在的查验记录（驳回的可以修改后重提）
            vehicle = existVehicle;
        } else {
            // 创建新的查验记录
            vehicle = new VehicleInfo();
        }

        // 8. 从道路申请中复制核心字段（数据来源唯一）
        vehicle.setApplicationId(application.getId());
        vehicle.setEnterpriseId(application.getEnterpriseId());
        vehicle.setVin(application.getVin());
        vehicle.setVehicleBrand(application.getVehicleBrand());
        vehicle.setVehicleModel(application.getVehicleModel());
        vehicle.setUserId(application.getApplicantId()); // 从道路申请获取申请人作为查验记录的用户ID

        // 9. 复制前端提交的查验数据
        BeanUtils.copyProperties(request, vehicle);

        // 10. 设置查验状态
        vehicle.setStatus(request.getAuditStatus());
        vehicle.setRejectReason(request.getAuditStatus().equals(VehicleStatusEnum.REJECTED.getCode())
                ? request.getRejectReason() : null);

        // 11. 保存或更新
        this.saveOrUpdate(vehicle);
        
        // 12. 如果查验通过，自动生成车辆档案
        if (vehicle.getStatus().equals(VehicleStatusEnum.PASSED.getCode())) {
            carArchiveService.generateFromInspection(vehicle.getVehicleId());
        }
        
        return vehicle.getVehicleId();
    }

    @Override
    public List<VehicleInfoVO> myList(Long userId) {
        List<VehicleInfo> list = this.list(new LambdaQueryWrapper<VehicleInfo>()
                .eq(VehicleInfo::getUserId, userId)
                .orderByDesc(VehicleInfo::getCreateTime));
        return list.stream().map(VehicleInfoVO::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean audit(VehicleAuditRequest auditRequest) {
        // 1. 校验审核人权限 (roleType = 1 为民警)
        SysUser reviewer = sysUserService.getById(auditRequest.getReviewerId());
        if (reviewer == null || reviewer.getRoleType() != 1) {
            throw new RuntimeException("操作失败：审核人必须是民警身份");
        }

        // 2. 校验记录是否存在
        VehicleInfo vehicle = this.getById(auditRequest.getVehicleId());
        if (vehicle == null) {
            throw new RuntimeException("车辆记录不存在");
        }

        // 3. 更新状态
        vehicle.setStatus(auditRequest.getAuditStatus());
        if (auditRequest.getAuditStatus().equals(VehicleStatusEnum.REJECTED.getCode())) {
            if (!StringUtils.hasText(auditRequest.getReason())) {
                throw new RuntimeException("驳回请填写原因");
            }
            vehicle.setRejectReason(auditRequest.getReason());
        } else {
            vehicle.setRejectReason(null);
        }

        return this.updateById(vehicle);
    }

    @Override
    public VehicleInfoVO detail(Long id) {
        return VehicleInfoVO.fromEntity(this.getById(id));
    }
}
