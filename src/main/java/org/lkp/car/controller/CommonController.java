package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.ReverseGeocodeRequest;
import org.lkp.car.dto.ReverseGeocodeResponse;
import org.lkp.car.service.ReverseGeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通用接口控制层
 * <p>
 * 提供系统通用功能接口，包括：
 * 1. 逆地理编码服务（经纬度转地址）
 * </p>
 */
@RestController
@RequestMapping("/common")
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private ReverseGeocodeService reverseGeocodeService;

    @GetMapping("/reverseGeocode")
    @ApiOperation("逆地理编码（经纬度转地址-GET方式）")
    public Result<ReverseGeocodeResponse> reverseGeocodeGet(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        return Result.success(reverseGeocodeService.reverseGeocode(latitude, longitude));
    }

    @PostMapping("/reverseGeocode")
    @ApiOperation("逆地理编码（经纬度转地址-POST方式）")
    public Result<ReverseGeocodeResponse> reverseGeocodePost(
            @RequestBody ReverseGeocodeRequest request) {
        return Result.success(reverseGeocodeService.reverseGeocode(
                request.getLatitude(), request.getLongitude()));
    }
}
