package org.lkp.car.service;

import org.lkp.car.dto.AuditTaskVO;

import java.util.List;

/**
 * 统一审核任务服务（流程驱动）
 */
public interface AuditService {

    /**
     * 获取审核任务列表
     *
     * @param reviewerId   民警用户ID（查已办时使用）
     * @param isProcessed  false-待审核, true-已处理
     * @param businessType 业务类型：1-号牌, 2-企业资质, null-全部
     */
    List<AuditTaskVO> listTasks(Long reviewerId, boolean isProcessed, Integer businessType);
}
