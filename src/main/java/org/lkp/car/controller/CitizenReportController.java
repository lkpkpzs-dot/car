package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.dto.CitizenReportEnterpriseHandleRequest;
import org.lkp.car.dto.CitizenReportReviewRequest;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.service.SysMessageService;
import org.lkp.car.service.SysUserService;
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

    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 提交举报（新流程）
     */
    @PostMapping("/submit")
    @ApiOperation("提交举报（新流程）")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
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
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<List<CitizenReport>> list(
            @ApiParam(value = "处理状态: 0-待核实 1-企业处理中 2-已处理 3-无效举报 4-待民警审核（超时升级）")
            @RequestParam(required=false) Integer processStatus,
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
    @RequireRole({RoleEnum.ENTERPRISE_CODE})
    public Result<List<CitizenReport>> enterpriseList(
            @ApiParam(value = "处理状态")
            @RequestParam(required= false) Integer processStatus,
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
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<List<CitizenReport>> citizenList(
            @ApiParam(value = "处理状态: 0-待核实 1-企业处理中 2-已处理 3-无效举报 4-待民警审核（超时升级）")
            @RequestParam(required= false) Integer processStatus,
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
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
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
    @RequireRole({RoleEnum.ENTERPRISE_CODE})
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
    @RequireRole({RoleEnum.POLICE_CODE})
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
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<Boolean> save(@RequestBody CitizenReport citizenReport, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser != null) {
            citizenReport.setUserId(currentUser.getUserId());
        }
        return Result.success(citizenReportService.save(citizenReport));
    }

    /**
     * 封禁用户举报权限（民警专用）
     */
    @PostMapping("/admin/ban-user")
    @ApiOperation("封禁用户举报权限（民警专用）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Void> banUserReport(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "24") int banHours,
            @RequestParam String reason,
            HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限操作");
        }
        try {
            sysUserService.banUserReport(userId, banHours, reason);
            
            // 发送封禁通知消息给用户
            sendBanNotification(userId, banHours, reason, currentUser);
            
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 解封用户举报权限（民警专用）
     */
    @PostMapping("/admin/unban-user")
    @ApiOperation("解封用户举报权限（民警专用）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Void> unbanUserReport(
            @RequestParam Long userId,
            HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限操作");
        }
        try {
            sysUserService.unbanUserReport(userId);
            
            // 发送解封通知消息给用户
            sendUnbanNotification(userId, currentUser);
            
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 发送封禁通知消息
     */
    private void sendBanNotification(Long userId, int banHours, String reason, SysUser police) {
        SysMessage message = new SysMessage();
        message.setReceiverId(userId);
        message.setMsgType(4); // 举报处理通知类型
        message.setBusinessType(null); // 不关联具体业务
        message.setBusinessId(null);
        message.setTitle("举报功能封禁通知");
        message.setContent(String.format(
            "您的举报功能已被封禁%d小时，原因：%s。封禁由民警%s执行。",
            banHours, reason, police.getRealName() != null ? police.getRealName() : "管理员"
        ));
        message.setIsRead(0);
        sysMessageService.save(message);
    }
    
    /**
     * 发送解封通知消息
     */
    private void sendUnbanNotification(Long userId, SysUser police) {
        SysMessage message = new SysMessage();
        message.setReceiverId(userId);
        message.setMsgType(4); // 举报处理通知类型
        message.setBusinessType(null); // 不关联具体业务
        message.setBusinessId(null);
        message.setTitle("举报功能解封通知");
        message.setContent(String.format(
            "您的举报功能已解封，现在可以正常使用了。解封由民警%s执行。",
            police.getRealName() != null ? police.getRealName() : "管理员"
        ));
        message.setIsRead(0);
        sysMessageService.save(message);
    }

    /**
     * 获取用户列表（民警专用，用于查看举报统计）
     */
    @GetMapping("/admin/users")
    @ApiOperation("获取用户列表（民警专用）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<List<SysUser>> getUserList(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限访问");
        }
        List<SysUser> users = sysUserService.list();
        return Result.success(users);
    }
}
