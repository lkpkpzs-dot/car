package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.GenerateArchiveRequest;
import org.lkp.car.dto.IssuePlateRequest;
import org.lkp.car.entity.CarArchive;

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
}
