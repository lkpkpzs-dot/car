package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.dto.CitizenReportEnterpriseHandleRequest;
import org.lkp.car.dto.CitizenReportReviewRequest;
import org.lkp.car.entity.CarArchive;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.CitizenReportMapper;
import org.lkp.car.service.CarArchiveService;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.service.SysMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class CitizenReportServiceImpl extends ServiceImpl<CitizenReportMapper, CitizenReport> implements CitizenReportService {

    @Autowired
    private SysMessageService sysMessageService;

    @Autowired
    @Lazy
    private CarArchiveService carArchiveService;

    // 企业处理超时时间（小时）
    private static final int ENTERPRISE_HANDLE_TIMEOUT_HOURS = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReport(CitizenReport report, Long userId) {
        report.setUserId(userId);

        // 1. 如果前端没传风险等级，给默认值
        if (report.getRiskLevel() == null) {
            report.setRiskLevel(1); // 默认低风险
        }

        // 2. 根据车牌号关联企业
        if (report.getTargetPlate() != null) {
            LambdaQueryWrapper<CarArchive> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CarArchive::getPlateNumber, report.getTargetPlate());
            CarArchive archive = carArchiveService.getOne(wrapper);
            if (archive != null) {
                report.setEnterpriseId(archive.getEnterpriseId());
            }
        }

        // 3. 根据风险等级设置初始状态
        if (report.getRiskLevel() == 1) {
            // 低风险：企业处理中
            report.setProcessStatus(1);
            // 设置企业处理截止时间
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, ENTERPRISE_HANDLE_TIMEOUT_HOURS);
            report.setEnterpriseDeadline(calendar.getTime());
        } else {
            // 高风险：待民警核实
            report.setProcessStatus(0);
        }

        // 4. 保存举报
        this.save(report);

        // 5. 发送通知
        sendNotificationAfterSubmit(report);

        return report.getReportId();
    }

    /**
     * 提交举报后发送通知
     */
    private void sendNotificationAfterSubmit(CitizenReport report) {
        // 通知企业（无论低风险还是高风险都通知）
        if (report.getEnterpriseId() != null) {
            sendEnterpriseNotification(report);
        }
    }

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
    public List<CitizenReport> getEnterpriseReportList(Long enterpriseId, Integer processStatus) {
        LambdaQueryWrapper<CitizenReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CitizenReport::getEnterpriseId, enterpriseId);
        if (processStatus != null) {
            wrapper.eq(CitizenReport::getProcessStatus, processStatus);
        }
        wrapper.orderByDesc(CitizenReport::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public CitizenReport getReportDetail(Long reportId) {
        return this.getById(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enterpriseHandleReport(CitizenReportEnterpriseHandleRequest request, Long enterpriseHandlerId) {
        CitizenReport report = this.getById(request.getReportId());
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }
        if (report.getProcessStatus() != 1) {
            throw new RuntimeException("当前状态不允许企业处理");
        }
        if (request.getProcessStatus() != 2 && request.getProcessStatus() != 3) {
            throw new RuntimeException("处理状态必须为2(已处理)或3(无效举报)");
        }

        // 更新处理信息
        report.setProcessStatus(request.getProcessStatus());
        report.setEnterpriseHandlerId(enterpriseHandlerId);
        report.setEnterpriseHandleTime(new Date());
        report.setEnterpriseHandleRemark(request.getRemark());

        boolean result = this.updateById(report);

        // 通知市民
        if (result) {
            String notificationContent;
            if (request.getProcessStatus() == 2) {
                notificationContent = "企业已处理您的举报：" + request.getRemark();
            } else {
                notificationContent = "企业认为此举报无效：" + request.getRemark();
            }
            sendCitizenNotification(report, notificationContent);
        }

        return result;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processTimeoutReports() {
        // 查找超时未处理的举报（企业处理中且超过截止时间）
        LambdaQueryWrapper<CitizenReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CitizenReport::getProcessStatus, 1)
               .lt(CitizenReport::getEnterpriseDeadline, new Date());
        List<CitizenReport> timeoutReports = this.list(wrapper);

        for (CitizenReport report : timeoutReports) {
            // 升级为待民警审核
            report.setProcessStatus(4);
            this.updateById(report);

            // 发送通知给民警和企业
            sendUpgradeNotification(report);
        }
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

        if (processStatus == 1 || processStatus == 2) {
            message.setContent("您的举报已处理，感谢您的反馈");
        } else if (processStatus == 3) {
            message.setContent("经核实为无效举报");
        }

        sysMessageService.save(message);
    }

    /**
     * 发送通知给市民
     */
    private void sendCitizenNotification(CitizenReport report, String content) {
        SysMessage message = new SysMessage();
        message.setReceiverId(report.getUserId());
        message.setMsgType(4);
        message.setBusinessType(1);
        message.setBusinessId(report.getReportId());
        message.setTitle("举报处理通知");
        message.setContent(content);
        message.setIsRead(0);
        sysMessageService.save(message);
    }

    /**
     * 发送通知给企业
     */
    private void sendEnterpriseNotification(CitizenReport report) {
        String title = report.getRiskLevel() == 1 ? "收到新的举报，请处理" : "收到高风险举报，请关注";
        String content = String.format("车牌号 %s 被举报，请及时处理", report.getTargetPlate());

        SysMessage message = new SysMessage();
        message.setReceiverId(report.getEnterpriseId()); // 这里需要根据实际情况设置企业接收人ID
        message.setMsgType(4);
        message.setBusinessType(1);
        message.setBusinessId(report.getReportId());
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        sysMessageService.save(message);
    }

    /**
     * 发送超时升级通知
     */
    private void sendUpgradeNotification(CitizenReport report) {
        // 通知企业
        String enterpriseContent = String.format("车牌号 %s 的举报已超时未处理，已升级为民警审核", report.getTargetPlate());
        sendEnterpriseNotificationContent(report.getEnterpriseId(), "举报超时升级通知", enterpriseContent);

        // 这里还可以添加通知民警的逻辑
    }

    private void sendEnterpriseNotificationContent(Long enterpriseId, String title, String content) {
        SysMessage message = new SysMessage();
        message.setReceiverId(enterpriseId); // 需要根据实际业务调整
        message.setMsgType(4);
        message.setBusinessType(1);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        sysMessageService.save(message);
    }

    @Override
    public List<CitizenReport> getCitizenReportList(Long userId, Integer processStatus) {
        LambdaQueryWrapper<CitizenReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CitizenReport::getUserId, userId);
        if (processStatus != null) {
            wrapper.eq(CitizenReport::getProcessStatus, processStatus);
        }
        wrapper.orderByDesc(CitizenReport::getCreateTime);
        return this.list(wrapper);
    }
}
