package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.dto.UserProfileUpdateRequest;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.SysUserService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户与权限控制层
 */
@RestController
@RequestMapping("/sysUser")
@Api(tags = "用户与权限接口")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @ApiOperation("获取用户列表")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.list());
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取用户详情")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    @ApiOperation("新增用户")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> save(@RequestBody SysUser sysUser) {
        return Result.success(sysUserService.save(sysUser));
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    @ApiOperation("修改用户")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> update(@RequestBody SysUser sysUser) {
        return Result.success(sysUserService.updateById(sysUser));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }

    /**
     * 更新用户个人资料（安全接口）
     */
    @PostMapping("/updateProfile")
    @ApiOperation("更新用户个人资料")
    public Result<Boolean> updateProfile(@RequestBody UserProfileUpdateRequest request, HttpServletRequest httpRequest) {
        SysUser currentUser = AuthContext.currentUser(httpRequest);
        if (currentUser == null) {
            return Result.error("请先登录");
        }

        // 从数据库获取当前用户最新信息
        SysUser user = sysUserService.getById(currentUser.getUserId());
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 只更新允许的字段
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        return Result.success(sysUserService.updateById(user));
    }
}
