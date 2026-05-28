package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.RoadApplyRequest;
import org.lkp.car.dto.RoadAuditRequest;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.utils.AuthContext;
import org.lkp.car.vo.RoadPermissionApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 道路测试/应用申请控制层
 */
@RestController
@RequestMapping("/roadApplication")
@Api(tags = "道路测试申请接口")
public class RoadPermissionApplicationController {

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @PostMapping("/apply")
    @ApiOperation("提交道路测试/应用申请")
    public Result<Long> apply(@RequestBody RoadApplyRequest applyRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        applyRequest.setApplicantId(currentUser.getUserId());
        applyRequest.setEnterpriseId(currentUser.getAuthEnterpriseId());
        return Result.success(roadPermissionApplicationService.apply(applyRequest));
    }

    @GetMapping("/myList")
    @ApiOperation("查询当前用户申请列表")
    public Result<List<RoadPermissionApplication>> myList(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        return Result.success(roadPermissionApplicationService.listMyApplications(currentUser.getUserId()));
    }

    @GetMapping("/enterpriseList")
    @ApiOperation("根据企业ID查询申请列表")
    public Result<List<RoadPermissionApplication>> enterpriseList(@RequestParam(required = false) Long enterpriseId,
                                                                  HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (AuthContext.isPolice(currentUser)) {
            if (enterpriseId == null) {
                return Result.success(roadPermissionApplicationService.list());
            }
            return Result.success(roadPermissionApplicationService.listByEnterprise(enterpriseId));
        }
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        return Result.success(roadPermissionApplicationService.listByEnterprise(currentUser.getAuthEnterpriseId()));
    }

    @PutMapping("/audit")
    @ApiOperation("民警审核道路测试申请")
    public Result<Boolean> audit(@RequestBody RoadAuditRequest auditRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无审核权限");
        }
        auditRequest.setReviewerId(currentUser.getUserId());
        return Result.success(roadPermissionApplicationService.audit(auditRequest));
    }

    @GetMapping("/list")
    @ApiOperation("民警审核列表")
    public Result<List<RoadPermissionApplicationVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            HttpServletRequest request
    ) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无审核列表查看权限");
        }
        return Result.success(roadPermissionApplicationService.listAll(status, type));
    }
}
