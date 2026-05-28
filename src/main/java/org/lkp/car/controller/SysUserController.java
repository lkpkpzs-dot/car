package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.list());
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取用户详情")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    /**
     * 新增用户
     */
    @PostMapping("/save")
    @ApiOperation("新增用户")
    public Result<Boolean> save(@RequestBody SysUser sysUser) {
        return Result.success(sysUserService.save(sysUser));
    }

    /**
     * 修改用户
     */
    @PutMapping("/update")
    @ApiOperation("修改用户")
    public Result<Boolean> update(@RequestBody SysUser sysUser) {
        return Result.success(sysUserService.updateById(sysUser));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysUserService.removeById(id));
    }
}
