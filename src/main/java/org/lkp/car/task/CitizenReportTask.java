package org.lkp.car.task;

import org.lkp.car.service.CitizenReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 市民举报定时任务
 */
@Component
public class CitizenReportTask {

    @Autowired
    private CitizenReportService citizenReportService;

    /**
     * 超时举报升级处理
     * 每30分钟执行一次
     */
    @Scheduled(cron = "0 0/30 * * * ?")
    public void processTimeoutReports() {
        citizenReportService.processTimeoutReports();
    }
}
