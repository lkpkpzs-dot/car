package org.lkp.car.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.common.enums.VehicleStatusEnum;
import org.lkp.car.dto.GenerateArchiveRequest;
import org.lkp.car.dto.IssuePlateRequest;
import org.lkp.car.entity.*;
import org.lkp.car.mapper.CarArchiveMapper;
import org.lkp.car.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 车辆电子档案 服务实现类
 */
@Service
public class CarArchiveServiceImpl extends ServiceImpl<CarArchiveMapper, CarArchive> implements CarArchiveService {

    @Autowired
    private VehicleInfoService vehicleInfoService;

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @Autowired
    private VehiclePlateService vehiclePlateService;

    @Autowired
    private SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarArchive generateFromInspection(GenerateArchiveRequest request) {
        return generateFromInspection(request.getVehicleInfoId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CarArchive generateFromInspection(Long vehicleInfoId) {
        // 1. 校验查验记录ID
        if (vehicleInfoId == null) {
            throw new RuntimeException("查验记录ID不能为空");
        }

        // 2. 查询查验记录
        VehicleInfo vehicleInfo = vehicleInfoService.getById(vehicleInfoId);
        if (vehicleInfo == null) {
            throw new RuntimeException("查验记录不存在");
        }

        // 3. 校验查验状态必须是通过
        if (!VehicleStatusEnum.PASSED.getCode().equals(vehicleInfo.getStatus())) {
            throw new RuntimeException("只有查验通过的记录才能生成档案");
        }

        // 4. 查询道路申请记录（通过 applicationId 查询）
        if (vehicleInfo.getApplicationId() == null) {
            throw new RuntimeException("查验记录缺少关联的道路申请ID");
        }
        
        RoadPermissionApplication application = roadPermissionApplicationService.getById(vehicleInfo.getApplicationId());
        if (application == null) {
            throw new RuntimeException("关联的道路申请记录不存在，申请ID：" + vehicleInfo.getApplicationId());
        }

        // 5. 查询是否已有该申请ID的档案（防止重复生成）
        LambdaQueryWrapper<CarArchive> existByAppIdWrapper = new LambdaQueryWrapper<>();
        existByAppIdWrapper.eq(CarArchive::getApplicationId, application.getId());
        CarArchive existByAppIdArchive = this.getOne(existByAppIdWrapper);
        if (existByAppIdArchive != null) {
            throw new RuntimeException("该道路申请已生成档案，VIN：" + existByAppIdArchive.getVin());
        }

        // 6. 检查是否已存在该VIN的档案
        CarArchive existArchive = this.getById(vehicleInfo.getVin());
        if (existArchive != null) {
            throw new RuntimeException("该车辆档案已存在");
        }

        // 7. 创建档案
        CarArchive archive = new CarArchive();
        archive.setVin(vehicleInfo.getVin());
        archive.setEnterpriseId(vehicleInfo.getEnterpriseId());
        archive.setApplicationId(application.getId()); // 用实际查到的申请ID
        archive.setVehicleInfoId(vehicleInfo.getVehicleId());
        archive.setVehicleBrand(vehicleInfo.getVehicleBrand());
        archive.setVehicleModel(vehicleInfo.getVehicleModel());
        archive.setCurrentPlateType(application.getType());

        archive.setStatus(1); // 正常营运
        archive.setTotalMileage(BigDecimal.ZERO);
        archive.setViolationCount(0);

        // 8. 组装查验参数JSON
        Map<String, Object> techParams = new HashMap<>();
        techParams.put("length", vehicleInfo.getLength());
        techParams.put("width", vehicleInfo.getWidth());
        techParams.put("height", vehicleInfo.getHeight());
        techParams.put("totalMass", vehicleInfo.getTotalMass());
        techParams.put("curbWeight", vehicleInfo.getCurbWeight());
        techParams.put("ratedLoad", vehicleInfo.getRatedLoad());
        techParams.put("axleCount", vehicleInfo.getAxleCount());
        techParams.put("tireSpec", vehicleInfo.getTireSpec());
        techParams.put("motorPower", vehicleInfo.getMotorPower());
        techParams.put("motorNo", vehicleInfo.getMotorNo());
        techParams.put("maxSpeed", vehicleInfo.getMaxSpeed());
        techParams.put("batteryType", vehicleInfo.getBatteryType());
        techParams.put("batteryCapacity", vehicleInfo.getBatteryCapacity());
        archive.setTechParams(JSONUtil.toJsonStr(techParams));

        // 9. 组装照片JSON
        Map<String, Object> images = new HashMap<>();
        images.put("photoFront45", vehicleInfo.getPhotoFront_45());
        images.put("photoRear45", vehicleInfo.getPhotoRear_45());
        images.put("photoVin", vehicleInfo.getPhotoVin());
        images.put("docVehicleCertUnmanned", vehicleInfo.getDocVehicleCertUnmanned());
        archive.setImagesJson(JSONUtil.toJsonStr(images));

        // 10. 保存档案
        this.save(archive);
        return archive;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean issuePlate(IssuePlateRequest request) {
        // 1. 校验参数
        if (request.getVin() == null || request.getVin().trim().isEmpty()) {
            throw new RuntimeException("VIN码不能为空");
        }
        if (request.getPlateType() == null) {
            throw new RuntimeException("牌照类型不能为空");
        }
        if (request.getPlateNumber() == null || request.getPlateNumber().trim().isEmpty()) {
            throw new RuntimeException("车牌号不能为空");
        }
        if (request.getIssuerId() == null) {
            throw new RuntimeException("发牌民警ID不能为空");
        }

        // 2. 校验民警身份
        SysUser issuer = sysUserService.getById(request.getIssuerId());
        if (issuer == null || issuer.getRoleType() != 1) {
            throw new RuntimeException("只有民警才能发牌");
        }

        // 3. 查询档案
        CarArchive archive = this.getById(request.getVin());
        if (archive == null) {
            throw new RuntimeException("车辆档案不存在，请先生成档案");
        }

        // 4. 创建牌照记录
        VehiclePlate plate = new VehiclePlate();
        plate.setVin(request.getVin());
        plate.setEnterpriseId(archive.getEnterpriseId());
        plate.setApplicationId(archive.getApplicationId());
        plate.setVehicleInfoId(archive.getVehicleInfoId());
        plate.setPlateType(request.getPlateType());
        plate.setPlateNumber(request.getPlateNumber());

        if (request.getIssueDate() != null) {
            plate.setIssueDate(request.getIssueDate());
        } else {
            plate.setIssueDate(new Date());
        }

        plate.setExpiryDate(request.getExpiryDate());
        plate.setIssuerId(request.getIssuerId());
        plate.setIssueComment(request.getIssueComment());
        plate.setStatus(1); // 有效
        vehiclePlateService.save(plate);

        // 5. 更新档案
        archive.setCurrentPlateType(request.getPlateType());
        archive.setPlateNumber(request.getPlateNumber());
        this.updateById(archive);
        
        // 6. 更新道路申请的发牌状态为已发牌(1)
        RoadPermissionApplication application = roadPermissionApplicationService.getById(archive.getApplicationId());
        if (application != null) {
            application.setPlateStatus(1); // 1=已发牌
            roadPermissionApplicationService.updateById(application);
        }

        return true;
    }
}
