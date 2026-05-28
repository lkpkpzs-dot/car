package org.lkp.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.SafetyOfficerApplyRequest;
import org.lkp.car.dto.SafetyOfficerAuditRequest;
import org.lkp.car.dto.SafetyOfficerPenaltyRequest;
import org.lkp.car.entity.SafetyOfficer;
import org.lkp.car.entity.SafetyOfficerPenalty;
import org.lkp.car.entity.SysUser;
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
}
