package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.lkp.car.common.Result;
import org.lkp.car.dto.CitizenReportReviewRequest;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 群众举报控制层
 */
@RestController
@RequestMapping("/citizenReport")
@Api(tags = "群众举报接口")
public class CitizenReportController {

    @Autowired
    private CitizenReportService citizenReportService;

    /**
     * 举报列表（民警端）
     */
    @GetMapping("/list")
    @ApiOperation("举报列表（民警端）")
    public Result<List<CitizenReport>> list(
            @ApiParam(value = "处理状态: 0-待核实 1-已处理 2-无效举报")
            @RequestParam(required = false) Integer processStatus,
            HttpServletRequest request) {
        // 验证权限
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限访问");
        }
        List<CitizenReport> list = citizenReportService.getReportList(processStatus);
        return Result.success(list);
    }

    /**
     * 举报详情
     */
    @GetMapping("/detail")
    @ApiOperation("举报详情")
    public Result<CitizenReport> detail(
            @ApiParam(value = "举报ID", required = true)
            @RequestParam Long reportId,
            HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        CitizenReport report = citizenReportService.getReportDetail(reportId);
        if (report == null) {
            return Result.error("举报记录不存在");
        }
        return Result.success(report);
    }

    /**
     * 举报审核
     */
    @PutMapping("/review")
    @ApiOperation("举报审核")
    public Result<Boolean> review(
            @RequestBody CitizenReportReviewRequest request,
            HttpServletRequest httpRequest) {
        // 验证权限
        SysUser currentUser = AuthContext.currentUser(httpRequest);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限审核");
        }
        
        // 验证参数
        if (request.getProcessStatus() == null || 
            (request.getProcessStatus() != 1 && request.getProcessStatus() != 2)) {
            return Result.error("处理状态必须为1或2");
        }
        
        try {
            boolean result = citizenReportService.reviewReport(request, currentUser);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交举报（保留原有接口）
     */
    @PostMapping("/save")
    @ApiOperation("提交举报")
    public Result<Boolean> save(@RequestBody CitizenReport citizenReport, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser != null) {
            citizenReport.setUserId(currentUser.getUserId());
        }
        return Result.success(citizenReportService.save(citizenReport));
    }
}
