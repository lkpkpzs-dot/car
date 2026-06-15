package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.CitizenReportEnterpriseHandleRequest;
import org.lkp.car.dto.CitizenReportReviewRequest;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysUser;

import java.util.List;

/**
 * 市民举报服务接口
 * <p>
 * 处理市民举报业务，包括：
 * 1. 举报提交（含防恶意举报检查）
 * 2. 企业处理举报
 * 3. 民警审核举报
 * 4. 超时举报自动升级
 * 5. 举报查询（按角色权限）
 * </p>
 */
public interface CitizenReportService extends IService<CitizenReport> {

    /**
     * 提交举报（新流程：自动分级）
     */
    Long submitReport(CitizenReport report, Long userId);

    /**
     * 获取举报列表（民警端）
     * @param processStatus 处理状态，可为null
     * @return 举报列表
     */
    List<CitizenReport> getReportList(Integer processStatus);

    /**
     * 获取企业端举报列表
     */
    List<CitizenReport> getEnterpriseReportList(Long enterpriseId, Integer processStatus);

    /**
     * 获取举报详情
     * @param reportId 举报ID
     * @return 举报详情
     */
    CitizenReport getReportDetail(Long reportId);

    /**
     * 获取举报详情（带权限验证）
     * @param reportId 举报ID
     * @param currentUser 当前用户
     * @return 举报详情
     */
    CitizenReport getReportDetailWithPermission(Long reportId, SysUser currentUser);

    /**
     * 企业处理举报
     */
    boolean enterpriseHandleReport(CitizenReportEnterpriseHandleRequest request, Long enterpriseHandlerId, Long enterpriseId);

    /**
     * 审核举报
     * @param request 审核请求
     * @param currentUser 当前登录民警
     * @return 是否成功
     */
    boolean reviewReport(CitizenReportReviewRequest request, SysUser currentUser);

    /**
     * 超时升级处理（定时任务调用）
     */
    void processTimeoutReports();

    /**
     * 获取市民端举报列表
     * @param userId 市民用户ID
     * @param processStatus 处理状态，可为null
     * @return 举报列表
     */
    List<CitizenReport> getCitizenReportList(Long userId, Integer processStatus);
}
