package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.lkp.car.common.Result;
import org.lkp.car.dto.CitizenReportEnterpriseHandleRequest;
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
     * 提交举报（新流程）
     */
    @PostMapping("/submit")
    @ApiOperation("提交举报（新流程）")
    public Result<Long> submit(@RequestBody CitizenReport citizenReport, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        Long reportId = citizenReportService.submitReport(citizenReport, currentUser.getUserId());
        return Result.success(reportId);
    }

    /**
     * 举报列表（民警端）
     */
    @GetMapping("/list")
    @ApiOperation("举报列表（民警端）")
    public Result<List<CitizenReport>> list(
            @ApiParam(value = "处理状态: 0-待核实 1-企业处理中 2-已处理 3-无效举报 4-待民警审核（超时升级）")
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
     * 举报列表（企业端）
     */
    @GetMapping("/enterpriseList")
    @ApiOperation("举报列表（企业端）")
    public Result<List<CitizenReport>> enterpriseList(
            @ApiParam(value = "处理状态")
            @RequestParam(required = false) Integer processStatus,
            HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        List<CitizenReport> list = citizenReportService.getEnterpriseReportList(
                currentUser.getAuthEnterpriseId(), processStatus);
        return Result.success(list);
    }

    /**
     * 举报列表（市民端）
     */
    @GetMapping("/citizenList")
    @ApiOperation("举报列表（市民端）")
    public Result<List<CitizenReport>> citizenList(
            @ApiParam(value = "处理状态: 0-待核实 1-企业处理中 2-已处理 3-无效举报 4-待民警审核（超时升级）")
            @RequestParam(required = false) Integer processStatus,
            HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        List<CitizenReport> list = citizenReportService.getCitizenReportList(
                currentUser.getUserId(), processStatus);
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
        try {
            CitizenReport report = citizenReportService.getReportDetailWithPermission(reportId, currentUser);
            return Result.success(report);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 企业处理举报
     */
    @PostMapping("/enterpriseHandle")
    @ApiOperation("企业处理举报")
    public Result<Boolean> enterpriseHandle(
            @RequestBody CitizenReportEnterpriseHandleRequest request,
            HttpServletRequest httpRequest) {
        SysUser currentUser = AuthContext.currentUser(httpRequest);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        
        // 验证参数
        if (request.getProcessStatus() == null || 
            (request.getProcessStatus() != 2 && request.getProcessStatus() != 3)) {
            return Result.error("处理状态必须为2(已处理)或3(无效举报)");
        }
        
        try {
            boolean result = citizenReportService.enterpriseHandleReport(
                    request, currentUser.getUserId(), currentUser.getAuthEnterpriseId());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
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
            (request.getProcessStatus() != 2 && request.getProcessStatus() != 3)) {
            return Result.error("处理状态必须为2(已处理)或3(无效举报)");
        }
        
        try {
            boolean result = citizenReportService.reviewReport(request, currentUser);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 提交举报（保留原有接口，兼容旧版）
     */
    @PostMapping("/save")
    @ApiOperation("提交举报（兼容旧版）")
    public Result<Boolean> save(@RequestBody CitizenReport citizenReport, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser != null) {
            citizenReport.setUserId(currentUser.getUserId());
        }
        return Result.success(citizenReportService.save(citizenReport));
    }
}
