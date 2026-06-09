package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.AssignSafetyOfficerRequest;
import org.lkp.car.dto.GenerateArchiveRequest;
import org.lkp.car.dto.IssuePlateRequest;
import org.lkp.car.entity.CarArchive;

import java.util.List;

/**
 * 车辆电子档案 服务类
 */
public interface CarArchiveService extends IService<CarArchive> {

    /**
     * 从查验记录生成车辆档案
     */
    CarArchive generateFromInspection(GenerateArchiveRequest request);

    /**
     * 从查验记录生成车辆档案（内部调用，直接传ID）
     */
    CarArchive generateFromInspection(Long vehicleInfoId);

    /**
     * 民警发牌
     */
    boolean issuePlate(IssuePlateRequest request);

    /**
     * 分配安全员
     */
    boolean assignSafetyOfficer(AssignSafetyOfficerRequest request);

    /**
     * 获取安全员关联的车辆列表
     */
    List<CarArchive> getVehiclesByOfficerId(Long officerId);

    /**
     * 查询安全员已关联的车辆数量
     */
    int countVehiclesByOfficerId(Long officerId);
}
