package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.ServiceEvaluation;
import org.lkp.car.service.ServiceEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 服务评价控制层
 */
@RestController
@RequestMapping("/serviceEvaluation")
@Api(tags = "服务评价接口")
public class ServiceEvaluationController {

    @Autowired
    private ServiceEvaluationService serviceEvaluationService;

    /**
     * 获取评价列表
     */
    @GetMapping("/list")
    @ApiOperation("获取评价列表")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.CITIZEN_CODE})
    public Result<List<ServiceEvaluation>> list() {
        return Result.success(serviceEvaluationService.list());
    }

    /**
     * 根据ID获取评价详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取评价详情")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.CITIZEN_CODE})
    public Result<ServiceEvaluation> getById(@PathVariable Long id) {
        return Result.success(serviceEvaluationService.getById(id));
    }

    /**
     * 提交评价
     */
    @PostMapping("/save")
    @ApiOperation("提交评价")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.CITIZEN_CODE})
    public Result<Boolean> save(@RequestBody ServiceEvaluation serviceEvaluation) {
        return Result.success(serviceEvaluationService.save(serviceEvaluation));
    }

    /**
     * 修改评价内容
     */
    @PutMapping("/update")
    @ApiOperation("修改评价内容")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> update(@RequestBody ServiceEvaluation serviceEvaluation) {
        return Result.success(serviceEvaluationService.updateById(serviceEvaluation));
    }

    /**
     * 删除评价
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除评价")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(serviceEvaluationService.removeById(id));
    }
}
