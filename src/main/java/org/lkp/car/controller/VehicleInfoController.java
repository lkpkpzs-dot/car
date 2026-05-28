package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.VehicleApplyRequest;
import org.lkp.car.dto.VehicleAuditRequest;
import org.lkp.car.dto.VehicleInspectionSubmitRequest;
import org.lkp.car.service.VehicleInfoService;
import org.lkp.car.vo.VehicleInfoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<Long> apply(@RequestBody VehicleApplyRequest applyRequest) {
        return Result.success(vehicleInfoService.apply(applyRequest));
    }

    @PostMapping("/inspection/submit")
    @ApiOperation("提交车辆查验（新接口）")
    public Result<Long> submitInspection(@RequestBody VehicleInspectionSubmitRequest request) {
        return Result.success(vehicleInfoService.submitInspection(request));
    }

    @GetMapping("/myList")
    @ApiOperation("获取我的车辆查验列表")
    public Result<List<VehicleInfoVO>> myList(@RequestParam Long userId) {
        return Result.success(vehicleInfoService.myList(userId));
    }

    @PostMapping("/audit")
    @ApiOperation("民警审核车辆查验（旧接口，保留向后兼容）")
    public Result<Boolean> audit(@RequestBody VehicleAuditRequest auditRequest) {
        return Result.success(vehicleInfoService.audit(auditRequest));
    }

    @GetMapping("/detail")
    @ApiOperation("获取车辆查验详情")
    public Result<VehicleInfoVO> detail(@RequestParam Long id) {
        return Result.success(vehicleInfoService.detail(id));
    }
}
