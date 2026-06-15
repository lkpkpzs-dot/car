package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.dto.VehicleApplyRequest;
import org.lkp.car.dto.VehicleAuditRequest;
import org.lkp.car.dto.VehicleInspectionSubmitRequest;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.VehicleInfoService;
import org.lkp.car.utils.AuthContext;
import org.lkp.car.vo.VehicleInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 车辆查验控制层
 */
@RestController
@RequestMapping("/vehicle")
@Api(tags = "车辆查验接口")
public class VehicleInfoController {

    @Autowired
    private VehicleInfoService vehicleInfoService;

    @PostMapping("/apply")
    @ApiOperation("提交/重新提交车辆查验申请（旧接口，保留向后兼容）")
    @RequireRole({RoleEnum.ENTERPRISE_CODE})
    public Result<Long> apply(@RequestBody VehicleApplyRequest applyRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        applyRequest.setUserId(currentUser.getUserId());
        applyRequest.setEnterpriseId(currentUser.getAuthEnterpriseId());
        return Result.success(vehicleInfoService.apply(applyRequest));
    }

    @PostMapping("/inspection/submit")
    @ApiOperation("提交车辆查验（新接口）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Long> submitInspection(@RequestBody VehicleInspectionSubmitRequest submitRequest,
                                         HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无车辆查验权限");
        }
        return Result.success(vehicleInfoService.submitInspection(submitRequest));
    }

    @GetMapping("/myList")
    @ApiOperation("获取我的车辆查验列表")
    @RequireRole({RoleEnum.ENTERPRISE_CODE})
    public Result<List<VehicleInfoVO>> myList(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        return Result.success(vehicleInfoService.myList(currentUser.getUserId()));
    }

    @PostMapping("/audit")
    @ApiOperation("民警审核车辆查验（旧接口，保留向后兼容）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> audit(@RequestBody VehicleAuditRequest auditRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无车辆查验审核权限");
        }
        auditRequest.setReviewerId(currentUser.getUserId());
        return Result.success(vehicleInfoService.audit(auditRequest));
    }

    @GetMapping("/detail")
    @ApiOperation("获取车辆查验详情")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE})
    public Result<VehicleInfoVO> detail(@RequestParam Long id, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        VehicleInfoVO detail = vehicleInfoService.detail(id);
        if (detail == null) {
            return Result.success(null);
        }
        if (!AuthContext.isPolice(currentUser)
                && !currentUser.getUserId().equals(detail.getUserId())) {
            return Result.error(403, "无车辆查验详情查看权限");
        }
        return Result.success(detail);
    }
}
