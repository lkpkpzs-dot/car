package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.VehicleApplyRequest;
import org.lkp.car.dto.VehicleAuditRequest;
import org.lkp.car.dto.VehicleInspectionSubmitRequest;
import org.lkp.car.entity.VehicleInfo;
import org.lkp.car.vo.VehicleInfoVO;

import java.util.List;

/**
 * 车辆查验信息 服务类
 */
public interface VehicleInfoService extends IService<VehicleInfo> {

    /**
     * 提交/重新提交车辆查验申请（旧接口，保留向后兼容）
     */
    Long apply(VehicleApplyRequest applyRequest);

    /**
     * 提交车辆查验（新接口）
     */
    Long submitInspection(VehicleInspectionSubmitRequest request);

    /**
     * 获取我的车辆列表
     */
    List<VehicleInfoVO> myList(Long userId);

    /**
     * 民警审核车辆查验（旧接口，保留向后兼容）
     */
    boolean audit(VehicleAuditRequest auditRequest);

    /**
     * 获取车辆详情
     */
    VehicleInfoVO detail(Long id);
}
