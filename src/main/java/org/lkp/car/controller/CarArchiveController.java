package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.GenerateArchiveRequest;
import org.lkp.car.dto.IssuePlateRequest;
import org.lkp.car.entity.CarArchive;
import org.lkp.car.service.CarArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车辆电子档案控制层
 */
@RestController
@RequestMapping("/carArchive")
@Api(tags = "车辆电子档案接口")
public class CarArchiveController {

    @Autowired
    private CarArchiveService carArchiveService;

    /**
     * 获取车辆列表
     */
    @GetMapping("/list")
    @ApiOperation("获取车辆列表")
    public Result<List<CarArchive>> list() {
        return Result.success(carArchiveService.list());
    }

    /**
     * 根据VIN获取车辆详情
     */
    @GetMapping("/{vin}")
    @ApiOperation("根据VIN获取车辆详情")
    public Result<CarArchive> getById(@PathVariable String vin) {
        return Result.success(carArchiveService.getById(vin));
    }

    /**
     * 新增车辆档案
     */
    @PostMapping("/save")
    @ApiOperation("新增车辆档案")
    public Result<Boolean> save(@RequestBody CarArchive carArchive) {
        return Result.success(carArchiveService.save(carArchive));
    }

    /**
     * 修改车辆档案
     */
    @PutMapping("/update")
    @ApiOperation("修改车辆档案")
    public Result<Boolean> update(@RequestBody CarArchive carArchive) {
        return Result.success(carArchiveService.updateById(carArchive));
    }

    /**
     * 删除车辆档案
     */
    @DeleteMapping("/{vin}")
    @ApiOperation("删除车辆档案")
    public Result<Boolean> delete(@PathVariable String vin) {
        return Result.success(carArchiveService.removeById(vin));
    }

    /**
     * 从查验生成车辆档案
     */
    @PostMapping("/generateFromInspection")
    @ApiOperation("从查验生成车辆档案")
    public Result<CarArchive> generateFromInspection(@RequestBody GenerateArchiveRequest request) {
        return Result.success(carArchiveService.generateFromInspection(request));
    }

    /**
     * 民警发牌
     */
    @PostMapping("/issuePlate")
    @ApiOperation("民警发牌")
    public Result<Boolean> issuePlate(@RequestBody IssuePlateRequest request) {
        return Result.success(carArchiveService.issuePlate(request));
    }
}
