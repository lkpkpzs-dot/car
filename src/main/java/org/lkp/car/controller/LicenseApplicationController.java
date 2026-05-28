package org.lkp.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.LicenseApplication;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.LicenseApplicationService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 号牌申请控制层
 */
@RestController
@RequestMapping("/licenseApplication")
@Api(tags = "号牌申请接口")
public class LicenseApplicationController {

    @Autowired
    private LicenseApplicationService licenseApplicationService;

    /**
     * 获取当前登录企业的申请列表 (企业端首页统计使用)
     */
    @GetMapping("/myList")
    @ApiOperation("获取当前登录企业的申请列表")
    public Result<List<LicenseApplication>> myList(HttpServletRequest request) {
        SysUser user = AuthContext.currentUser(request);
        if (user == null || user.getAuthEnterpriseId() == null) {
            return Result.success(Collections.emptyList());
        }

        List<LicenseApplication> applications = licenseApplicationService.list(
                new LambdaQueryWrapper<LicenseApplication>()
                        .eq(LicenseApplication::getEnterpriseId, user.getAuthEnterpriseId())
                        .orderByDesc(LicenseApplication::getCreateTime)
        );

        return Result.success(applications);
    }

    /**
     * 获取申请列表
     */
    @GetMapping("/list")
    @ApiOperation("获取申请列表")
    public Result<List<LicenseApplication>> list(HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无申请列表查看权限");
        }
        return Result.success(licenseApplicationService.list());
    }

    /**
     * 根据ID获取申请详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取申请详情")
    public Result<LicenseApplication> getById(@PathVariable Long id, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        LicenseApplication application = licenseApplicationService.getById(id);
        if (application == null) {
            return Result.success(null);
        }
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(application.getEnterpriseId()))) {
            return Result.error(403, "无申请详情查看权限");
        }
        return Result.success(application);
    }

    /**
     * 提交/保存申请
     */
    @PostMapping("/save")
    @ApiOperation("提交/保存申请")
    public Result<Boolean> save(@RequestBody LicenseApplication licenseApplication, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "请先完成企业资质认证和企业绑定");
        }
        licenseApplication.setEnterpriseId(currentUser.getAuthEnterpriseId());
        return Result.success(licenseApplicationService.save(licenseApplication));
    }

    /**
     * 修改申请信息
     */
    @PutMapping("/update")
    @ApiOperation("修改申请信息")
    public Result<Boolean> update(@RequestBody LicenseApplication licenseApplication, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser) && !AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, "无申请修改权限");
        }
        if (!AuthContext.isPolice(currentUser)) {
            licenseApplication.setEnterpriseId(currentUser.getAuthEnterpriseId());
        }
        return Result.success(licenseApplicationService.updateById(licenseApplication));
    }

    /**
     * 删除申请
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除申请")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无申请删除权限");
        }
        return Result.success(licenseApplicationService.removeById(id));
    }
}
