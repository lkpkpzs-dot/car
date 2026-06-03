package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.dto.CitizenReportReviewRequest;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.CitizenReportMapper;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.service.SysMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CitizenReportServiceImpl extends ServiceImpl<CitizenReportMapper, CitizenReport> implements CitizenReportService {

    @Autowired
    private SysMessageService sysMessageService;

    @Override
    public List<CitizenReport> getReportList(Integer processStatus) {
        LambdaQueryWrapper<CitizenReport> wrapper = new LambdaQueryWrapper<>();
        // 按处理状态筛选
        if (processStatus != null) {
            wrapper.eq(CitizenReport::getProcessStatus, processStatus);
        }
        // 默认按创建时间倒序
        wrapper.orderByDesc(CitizenReport::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public CitizenReport getReportDetail(Long reportId) {
        return this.getById(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewReport(CitizenReportReviewRequest request, SysUser currentUser) {
        // 1. 根据reportId查询举报记录
        CitizenReport report = this.getById(request.getReportId());
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        // 2. 更新字段
        report.setProcessStatus(request.getProcessStatus());
        report.setReviewRemark(request.getReviewRemark());
        report.setReviewTime(new Date());
        report.setReviewerId(currentUser.getUserId());

        // 3. 保存更新
        boolean result = this.updateById(report);

        // 4. 审核完成后自动发送系统消息给举报人
        if (result) {
            sendNotification(report, request.getProcessStatus());
        }

        return result;
    }

    /**
     * 发送举报处理结果通知
     */
    private void sendNotification(CitizenReport report, Integer processStatus) {
        SysMessage message = new SysMessage();
        message.setReceiverId(report.getUserId());
        message.setMsgType(4); // 举报处理通知
        message.setBusinessType(1); // 业务类型：1-举报
        message.setBusinessId(report.getReportId());
        message.setTitle("举报处理结果通知");
        message.setIsRead(0); // 未读

        if (processStatus == 1) {
            message.setContent("您的举报已处理，感谢您的反馈");
        } else if (processStatus == 2) {
            message.setContent("经核实为无效举报");
        }

        sysMessageService.save(message);
    }
}
