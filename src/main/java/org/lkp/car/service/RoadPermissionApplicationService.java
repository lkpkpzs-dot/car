package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.RoadApplyRequest;
import org.lkp.car.dto.RoadAuditRequest;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.vo.RoadPermissionApplicationVO;

import java.util.List;

/**
 * 道路测试/应用申请 服务类
 */
public interface RoadPermissionApplicationService extends IService<RoadPermissionApplication> {

    /**
     * 提交道路测试/应用申请
     */
    Long apply(RoadApplyRequest applyRequest);

    /**
     * 查询我的申请列表
     */
    List<RoadPermissionApplication> listMyApplications(Long applicantId);

    /**
     * 企业查询申请列表
     */
    List<RoadPermissionApplication> listByEnterprise(Long enterpriseId);

    /**
     * 民警审核申请
     */
    boolean audit(RoadAuditRequest auditRequest);

    /**
     * 查询所有申请列表（民警审核用）
     */
    List<RoadPermissionApplicationVO> listAll(Integer status, Integer type);
}
