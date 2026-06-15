package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.EnterpriseDashboardService;
import org.lkp.car.utils.AuthContext;
import org.lkp.car.vo.AdminDashboardVO;
import org.lkp.car.vo.CitizenDashboardVO;
import org.lkp.car.vo.EnterpriseDashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/enterprise")
@Api(tags = "企业端首页接口")
public class EnterpriseDashboardController {

    @Autowired
    private EnterpriseDashboardService enterpriseDashboardService;

    @GetMapping("/dashboard")
    @ApiOperation("获取企业端首页数据")
    @RequireRole({RoleEnum.ENTERPRISE_CODE})
    public Result<EnterpriseDashboardVO> getDashboard(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser.getAuthEnterpriseId() == null) {
            return Result.error("请先绑定企业");
        }
        EnterpriseDashboardVO dashboard = enterpriseDashboardService.getDashboardData(currentUser.getAuthEnterpriseId());
        return Result.success(dashboard);
    }

    @GetMapping("/admin/dashboard")
    @ApiOperation("获取管理端首页统计数据")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<AdminDashboardVO> getAdminDashboard(HttpServletRequest request) {
        AdminDashboardVO dashboard = enterpriseDashboardService.getAdminDashboardData();
        return Result.success(dashboard);
    }

    @GetMapping("/citizen/dashboard")
    @ApiOperation("获取市民端首页统计数据")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<CitizenDashboardVO> getCitizenDashboard(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        CitizenDashboardVO dashboard = enterpriseDashboardService.getCitizenDashboardData(currentUser.getUserId());
        return Result.success(dashboard);
    }
}
