package org.lkp.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.JwtUtils;
import org.lkp.car.common.Result;
import org.lkp.car.entity.LicenseApplication;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.LicenseApplicationService;
import org.lkp.car.service.SysUserService;
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

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取当前登录企业的申请列表 (企业端首页统计使用)
     */
    @GetMapping("/myList")
    @ApiOperation("获取当前登录企业的申请列表")
    public Result<List<LicenseApplication>> myList(HttpServletRequest request) {
        // 1. 从 Token 中解析用户 ID
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }

        // 2. 查询用户信息，获取所属企业 ID
        SysUser user = sysUserService.getById(userId);
        if (user == null || user.getAuthEnterpriseId() == null) {
            // 如果用户未绑定企业，返回空列表
            return Result.success(Collections.emptyList());
        }

        // 3. 根据企业 ID 查询所有的号牌申请记录
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
    public Result<List<LicenseApplication>> list() {
        return Result.success(licenseApplicationService.list());
    }

    /**
     * 根据ID获取申请详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取申请详情")
    public Result<LicenseApplication> getById(@PathVariable Long id) {
        return Result.success(licenseApplicationService.getById(id));
    }

    /**
     * 提交/保存申请
     */
    @PostMapping("/save")
    @ApiOperation("提交/保存申请")
    public Result<Boolean> save(@RequestBody LicenseApplication licenseApplication) {
        return Result.success(licenseApplicationService.save(licenseApplication));
    }

    /**
     * 修改申请信息
     */
    @PutMapping("/update")
    @ApiOperation("修改申请信息")
    public Result<Boolean> update(@RequestBody LicenseApplication licenseApplication) {
        return Result.success(licenseApplicationService.updateById(licenseApplication));
    }

    /**
     * 删除申请
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除申请")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(licenseApplicationService.removeById(id));
    }
}
