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
import org.lkp.car.service.SysUserService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

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

    @Autowired
    private SysUserService sysUserService;

    // 企业处理超时时间（小时）
//    private static final int ENTERPRISE_HANDLE_TIMEOUT_HOURS = 24;

    // 企业处理超时时间（小时），从配置文件读取
    @Value("${citizen-report.enterprise-handle-timeout-hours}")
    private int enterpriseHandleTimeoutHours;
    @Value("${citizen-report.high-risk-handle-timeout-hours:2}")
    private int highRiskHandleTimeoutHours; // 可选，默认2小时

    // 防恶意举报配置
    @Value("${citizen-report.anti-spam.max-reports-per-day:10}")
    private int maxReportsPerDay;
    @Value("${citizen-report.anti-spam.invalid-report-threshold:3}")
    private int invalidReportThreshold;
    @Value("${citizen-report.anti-spam.ban-duration-hours:24}")
    private int banDurationHours;
    @Value("${citizen-report.anti-spam.min-report-interval-minutes:1}")
    private int minReportIntervalMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitReport(CitizenReport report, Long userId) {
        report.setUserId(userId);

        // 0. 防恶意举报检查
        checkAntiSpam(userId);

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
            int timeoutHours = report.getRiskLevel() == 1
                    ? enterpriseHandleTimeoutHours
                    : highRiskHandleTimeoutHours;
            calendar.add(Calendar.HOUR, timeoutHours);
            report.setEnterpriseDeadline(calendar.getTime());
        } else {
            // 高风险：待民警核实
            report.setProcessStatus(0);
        }

        // 4. 保存举报
        this.save(report);

        // 5. 增加用户举报次数
        sysUserService.incrementTotalReportCount(userId);

        // 6. 发送通知
        sendNotificationAfterSubmit(report);

        return report.getReportId();
    }

    /**
     * 防恶意举报检查
     */
    private void checkAntiSpam(Long userId) {
        // 检查是否被封禁
        if (sysUserService.isUserBanned(userId)) {
            SysUser user = sysUserService.getById(userId);
            String banEndTime = user.getBanEndTime() != null 
                ? user.getBanEndTime().toString() 
                : "永久";
            throw new RuntimeException("您的举报功能已被封禁，封禁截止时间：" + banEndTime);
        }

        // 检查今日举报次数
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR_OF_DAY, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);

        LambdaQueryWrapper<CitizenReport> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(CitizenReport::getUserId, userId)
                   .ge(CitizenReport::getCreateTime, todayStart.getTime());
        long todayReportCount = this.count(countWrapper);

        if (todayReportCount >= maxReportsPerDay) {
            throw new RuntimeException("今日举报次数已达上限（" + maxReportsPerDay + "次），请明天再试");
        }

        // 检查最短举报间隔
        if (minReportIntervalMinutes > 0) {
            LambdaQueryWrapper<CitizenReport> lastReportWrapper = new LambdaQueryWrapper<>();
            lastReportWrapper.eq(CitizenReport::getUserId, userId)
                            .orderByDesc(CitizenReport::getCreateTime)
                            .last("LIMIT 1");
            CitizenReport lastReport = this.getOne(lastReportWrapper);
            if (lastReport != null) {
                long minutesSinceLastReport = (System.currentTimeMillis() - lastReport.getCreateTime().getTime()) / (1000 * 60);
                if (minutesSinceLastReport < minReportIntervalMinutes) {
                    throw new RuntimeException("举报过于频繁，请稍后再试（最短间隔" + minReportIntervalMinutes + "分钟）");
                }
            }
        }
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
    public boolean enterpriseHandleReport(CitizenReportEnterpriseHandleRequest request, Long enterpriseHandlerId, Long enterpriseId) {
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
        // 验证企业权限：只能处理自己企业的举报
        if (report.getEnterpriseId() == null || !report.getEnterpriseId().equals(enterpriseId)) {
            throw new RuntimeException("无权处理此举报");
        }

        // 更新处理信息
        report.setProcessStatus(request.getProcessStatus());
        report.setEnterpriseHandlerId(enterpriseHandlerId);
        report.setEnterpriseHandleTime(new Date());
        report.setEnterpriseHandleRemark(request.getRemark());

        boolean result = this.updateById(report);

        // 如果标记为无效举报，增加无效举报计数
        if (result && request.getProcessStatus() == 3 && report.getUserId() != null) {
            handleInvalidReport(report.getUserId());
        }

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

        // 如果标记为无效举报，增加无效举报计数
        if (result && request.getProcessStatus() == 3 && report.getUserId() != null) {
            handleInvalidReport(report.getUserId());
        }

        // 4. 审核完成后自动发送系统消息给举报人
        if (result) {
            sendNotification(report, request.getProcessStatus());
        }

        return result;
    }

    /**
     * 处理无效举报，增加计数并检查是否需要封禁
     */
    private void handleInvalidReport(Long userId) {
        sysUserService.incrementInvalidReportCount(userId);

        // 检查是否达到封禁阈值
        SysUser user = sysUserService.getById(userId);
        if (user != null && user.getInvalidReportCount() != null 
            && user.getInvalidReportCount() >= invalidReportThreshold) {
            sysUserService.banUserReport(userId, banDurationHours, 
                "无效举报次数达到" + invalidReportThreshold + "次，自动封禁");
        }
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
        sendNotificationToEnterpriseUsers(report.getEnterpriseId(), title, content, report.getReportId());
    }

    /**
     * 发送超时升级通知
     */
    private void sendUpgradeNotification(CitizenReport report) {
        // 通知企业
        String enterpriseContent = String.format("车牌号 %s 的举报已超时未处理，已升级为民警审核", report.getTargetPlate());
        sendNotificationToEnterpriseUsers(report.getEnterpriseId(), "举报超时升级通知", enterpriseContent, report.getReportId());

        // 通知所有民警
        String policeContent = String.format("车牌号 %s 的举报已超时未处理，请民警审核", report.getTargetPlate());
        sendNotificationToAllPolice("举报超时升级通知", policeContent, report.getReportId());
    }

    /**
     * 发送通知给企业所有用户
     */
    private void sendNotificationToEnterpriseUsers(Long enterpriseId, String title, String content, Long reportId) {
        if (enterpriseId == null) {
            return;
        }
        List<SysUser> enterpriseUsers = sysUserService.getEnterpriseUsers(enterpriseId);
        for (SysUser user : enterpriseUsers) {
            SysMessage message = new SysMessage();
            message.setReceiverId(user.getUserId());
            message.setMsgType(4);
            message.setBusinessType(1);
            message.setBusinessId(reportId);
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(0);
            sysMessageService.save(message);
        }
    }

    /**
     * 发送通知给所有民警
     */
    private void sendNotificationToAllPolice(String title, String content, Long reportId) {
        List<SysUser> policeUsers = sysUserService.getAllPoliceUsers();
        for (SysUser user : policeUsers) {
            SysMessage message = new SysMessage();
            message.setReceiverId(user.getUserId());
            message.setMsgType(4);
            message.setBusinessType(1);
            message.setBusinessId(reportId);
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(0);
            sysMessageService.save(message);
        }
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

    @Override
    public CitizenReport getReportDetailWithPermission(Long reportId, SysUser currentUser) {
        CitizenReport report = this.getById(reportId);
        if (report == null) {
            throw new RuntimeException("举报记录不存在");
        }

        // 民警可以看所有
        if (AuthContext.isPolice(currentUser)) {
            return report;
        }

        // 企业用户只能看自己企业的举报
        if (AuthContext.hasEnterprise(currentUser)) {
            if (report.getEnterpriseId() == null || 
                !report.getEnterpriseId().equals(currentUser.getAuthEnterpriseId())) {
                throw new RuntimeException("无权查看此举报");
            }
            return report;
        }

        // 市民只能看自己的举报
        if (report.getUserId() == null || !report.getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("无权查看此举报");
        }

        return report;
    }
}
