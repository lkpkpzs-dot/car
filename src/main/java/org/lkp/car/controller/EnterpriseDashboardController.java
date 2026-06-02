package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.EnterpriseDashboardService;
import org.lkp.car.utils.AuthContext;
import org.lkp.car.vo.AdminDashboardVO;
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
    public Result<EnterpriseDashboardVO> getDashboard(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null || currentUser.getAuthEnterpriseId() == null) {
            return Result.error("请先登录并绑定企业");
        }
        EnterpriseDashboardVO dashboard = enterpriseDashboardService.getDashboardData(currentUser.getAuthEnterpriseId());
        return Result.success(dashboard);
    }

    @GetMapping("/admin/dashboard")
    @ApiOperation("获取管理端首页统计数据")
    public Result<AdminDashboardVO> getAdminDashboard(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限访问管理端数据");
        }
        AdminDashboardVO dashboard = enterpriseDashboardService.getAdminDashboardData();
        return Result.success(dashboard);
    }
}
