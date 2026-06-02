package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.mapper.CitizenReportMapper;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.service.SysMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class CitizenReportServiceImpl extends ServiceImpl<CitizenReportMapper, CitizenReport> implements CitizenReportService {

    @Autowired
    private SysMessageService sysMessageService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(CitizenReport entity) {
        // 先查询旧记录
        CitizenReport oldReport = this.getById(entity.getReportId());
        if (oldReport == null) {
            return false;
        }

        // 如果是更新处理状态，自动设置审核时间
        if (entity.getProcessStatus() != null && (entity.getProcessStatus() == 1 || entity.getProcessStatus() == 2)) {
            entity.setReviewTime(new Date());
        }

        // 更新举报记录
        boolean result = super.updateById(entity);

        // 判断是否需要发送消息
        if (result && entity.getProcessStatus() != null && (entity.getProcessStatus() == 1 || entity.getProcessStatus() == 2)) {
            sendNotification(oldReport, entity.getProcessStatus());
        }

        return result;
    }

    /**
     * 发送举报处理通知
     */
    private void sendNotification(CitizenReport report, Integer processStatus) {
        SysMessage message = new SysMessage();
        message.setReceiverId(report.getUserId());
        message.setMsgType(4); // 举报处理通知
        message.setBusinessType(1); // 业务类型：1-举报
        message.setBusinessId(report.getReportId());
        message.setTitle("举报处理通知");

        if (processStatus == 1) {
            message.setContent("您举报的【" + report.getTargetPlate() + "】已处理");
        } else if (processStatus == 2) {
            message.setContent("您举报的【" + report.getTargetPlate() + "】已判定为无效举报");
        }

        message.setIsRead(0); // 未读
        sysMessageService.save(message);
    }
}
