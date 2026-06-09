package org.lkp.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.SafetyOfficerApplyRequest;
import org.lkp.car.dto.SafetyOfficerAuditRequest;
import org.lkp.car.dto.SafetyOfficerPenaltyRequest;
import org.lkp.car.entity.*;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.service.SafetyOfficerPenaltyService;
import org.lkp.car.service.SafetyOfficerService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 安全员监管控制层
 */
@RestController
@RequestMapping("/safetyOfficer")
@Api(tags = "安全员监管接口")
public class SafetyOfficerController {

    @Autowired
    private SafetyOfficerService safetyOfficerService;

    @Autowired
    private SafetyOfficerPenaltyService safetyOfficerPenaltyService;

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @PostMapping("/apply")
    @ApiOperation("提交/重新提交安全员资质申请")
    public Result<Long> apply(@RequestBody SafetyOfficerApplyRequest applyRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        return Result.success(safetyOfficerService.apply(
                applyRequest,
                currentUser.getUserId(),
                currentUser.getAuthEnterpriseId()
        ));
    }

    @GetMapping("/myList")
    @ApiOperation("获取当前企业安全员列表")
    public Result<List<SafetyOfficer>> myList(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        return Result.success(safetyOfficerService.list(new LambdaQueryWrapper<SafetyOfficer>()
                .eq(SafetyOfficer::getEnterpriseId, currentUser.getAuthEnterpriseId())
                .orderByDesc(SafetyOfficer::getCreateTime)));
    }

    @GetMapping("/list")
    @ApiOperation("民警获取安全员列表")
    public Result<List<SafetyOfficer>> list(@RequestParam(required = false) Long enterpriseId,
                                            @RequestParam(required = false) Integer status,
                                            HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无安全员监管列表查看权限");
        }
        LambdaQueryWrapper<SafetyOfficer> wrapper = new LambdaQueryWrapper<>();
        if (enterpriseId != null) {
            wrapper.eq(SafetyOfficer::getEnterpriseId, enterpriseId);
        }
        if (status != null) {
            wrapper.eq(SafetyOfficer::getStatus, status);
        }
        wrapper.orderByDesc(SafetyOfficer::getCreateTime);
        return Result.success(safetyOfficerService.list(wrapper));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取安全员详情")
    public Result<SafetyOfficer> getById(@PathVariable Long id, HttpServletRequest request) {
        SafetyOfficer officer = safetyOfficerService.getById(id);
        if (officer == null) {
            return Result.success(null);
        }
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(officer.getEnterpriseId()))) {
            return Result.error(403, "无安全员详情查看权限");
        }
        return Result.success(officer);
    }

    @PutMapping("/audit")
    @ApiOperation("民警审核安全员资质")
    public Result<Boolean> audit(@RequestBody SafetyOfficerAuditRequest auditRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无安全员审核权限");
        }
        auditRequest.setReviewerId(currentUser.getUserId());
        return Result.success(safetyOfficerService.audit(auditRequest));
    }

    @PostMapping("/accident/handle")
    @ApiOperation("民警录入事故并按规则处分安全员")
    public Result<SafetyOfficerPenalty> handleAccident(@RequestBody SafetyOfficerPenaltyRequest penaltyRequest,
                                                       HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无安全员事故处分权限");
        }
        penaltyRequest.setHandlerId(currentUser.getUserId());
        return Result.success(safetyOfficerService.handleAccident(penaltyRequest));
    }

    @GetMapping("/{id}/penalties")
    @ApiOperation("获取安全员事故处分记录")
    public Result<List<SafetyOfficerPenalty>> penaltyList(@PathVariable Long id, HttpServletRequest request) {
        SafetyOfficer officer = safetyOfficerService.getById(id);
        if (officer == null) {
            return Result.success(null);
        }
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(officer.getEnterpriseId()))) {
            return Result.error(403, "无安全员处分记录查看权限");
        }
        return Result.success(safetyOfficerPenaltyService.list(new LambdaQueryWrapper<SafetyOfficerPenalty>()
                .eq(SafetyOfficerPenalty::getOfficerId, id)
                .orderByDesc(SafetyOfficerPenalty::getCreateTime)));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除安全员记录")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        SafetyOfficer officer = safetyOfficerService.getById(id);
        if (officer == null) {
            return Result.success(true);
        }
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(officer.getEnterpriseId()))) {
            return Result.error(403, "无安全员记录删除权限");
        }
        return Result.success(safetyOfficerService.removeById(id));
    }

    @GetMapping("/enterpriseValidList")
    @ApiOperation("查询当前企业有效安全员列表（用于申请时选择）")
    public Result<List<SafetyOfficer>> enterpriseValidList(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        LambdaQueryWrapper<SafetyOfficer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SafetyOfficer::getEnterpriseId, currentUser.getAuthEnterpriseId())
               .eq(SafetyOfficer::getStatus, 1) // 只查询有效状态的安全员
               .orderByDesc(SafetyOfficer::getCreateTime);
        List<SafetyOfficer> list = safetyOfficerService.list(wrapper);
        
        // 为每个安全员补充已关联车辆数
        for (SafetyOfficer officer : list) {
            // 1. 查询已关联正式档案的车辆数
            int archiveCount = safetyOfficerService.getOfficerVehicleCount(officer.getOfficerId());
            
            // 2. 查询已选该安全员且状态为待审核/已通过的申请数
            LambdaQueryWrapper<RoadPermissionApplication> applyWrapper = new LambdaQueryWrapper<>();
            applyWrapper.eq(RoadPermissionApplication::getOfficerId, officer.getOfficerId())
                       .in(RoadPermissionApplication::getStatus, 1, 2); // 1=待审核, 2=已通过
            int applyCount = (int) roadPermissionApplicationService.count(applyWrapper);
            
            officer.setTotalVehicleCount(archiveCount + applyCount);
        }
        
        return Result.success(list);
    }

    /**
     * 获取安全员关联的车辆列表
     */
    @GetMapping("/{id}/vehicles")
    @ApiOperation("获取安全员关联的车辆列表")
    public Result<List<CarArchive>> getOfficerVehicles(@PathVariable Long id, HttpServletRequest request) {
        SafetyOfficer officer = safetyOfficerService.getById(id);
        if (officer == null) {
            return Result.success(null);
        }
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(officer.getEnterpriseId()))) {
            return Result.error(403, "无权限查看");
        }
        return Result.success(safetyOfficerService.getOfficerVehicles(id));
    }

    /**
     * 获取安全员已关联的车辆数量
     */
    @GetMapping("/{id}/vehicleCount")
    @ApiOperation("获取安全员已关联的车辆数量")
    public Result<Integer> getOfficerVehicleCount(@PathVariable Long id, HttpServletRequest request) {
        SafetyOfficer officer = safetyOfficerService.getById(id);
        if (officer == null) {
            return Result.success(0);
        }
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(officer.getEnterpriseId()))) {
            return Result.error(403, "无权限查看");
        }
        return Result.success(safetyOfficerService.getOfficerVehicleCount(id));
    }
}
