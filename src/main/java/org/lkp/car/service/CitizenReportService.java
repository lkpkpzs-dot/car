package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.CitizenReportReviewRequest;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysUser;

import java.util.List;

/**
 * 群众举报 服务类
 */
public interface CitizenReportService extends IService<CitizenReport> {

    /**
     * 获取举报列表（民警端）
     * @param processStatus 处理状态，可为null
     * @return 举报列表
     */
    List<CitizenReport> getReportList(Integer processStatus);

    /**
     * 获取举报详情
     * @param reportId 举报ID
     * @return 举报详情
     */
    CitizenReport getReportDetail(Long reportId);

    /**
     * 审核举报
     * @param request 审核请求
     * @param currentUser 当前登录民警
     * @return 是否成功
     */
    boolean reviewReport(CitizenReportReviewRequest request, SysUser currentUser);
}
