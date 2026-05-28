package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.VehiclePlate;
import org.lkp.car.service.VehiclePlateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆牌照控制层
 */
@RestController
@RequestMapping("/vehiclePlate")
@Api(tags = "车辆牌照接口")
public class VehiclePlateController {

    @Autowired
    private VehiclePlateService vehiclePlateService;

    @GetMapping("/list")
    @ApiOperation("获取牌照列表")
    public Result<List<VehiclePlate>> list() {
        return Result.success(vehiclePlateService.list());
    }

    @GetMapping("/{plateId}")
    @ApiOperation("根据ID获取牌照详情")
    public Result<VehiclePlate> getById(@PathVariable Long plateId) {
        return Result.success(vehiclePlateService.getById(plateId));
    }

    @PostMapping("/save")
    @ApiOperation("新增牌照记录")
    public Result<Boolean> save(@RequestBody VehiclePlate vehiclePlate) {
        return Result.success(vehiclePlateService.save(vehiclePlate));
    }

    @PutMapping("/update")
    @ApiOperation("修改牌照记录")
    public Result<Boolean> update(@RequestBody VehiclePlate vehiclePlate) {
        return Result.success(vehiclePlateService.updateById(vehiclePlate));
    }

    @DeleteMapping("/{plateId}")
    @ApiOperation("删除牌照记录")
    public Result<Boolean> delete(@PathVariable Long plateId) {
        return Result.success(vehiclePlateService.removeById(plateId));
    }
}
