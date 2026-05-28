package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.JwtUtils;
import org.lkp.car.common.Result;
import org.lkp.car.dto.RoadApplyRequest;
import org.lkp.car.dto.RoadAuditRequest;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.vo.RoadPermissionApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 道路测试/应用申请控制层
 */
@RestController
@RequestMapping("/roadApplication")
@Api(tags = "道路测试申请接口")
public class RoadPermissionApplicationController {

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/apply")
    @ApiOperation("提交道路测试/应用申请")
    public Result<Long> apply(@RequestBody RoadApplyRequest applyRequest) {
        return Result.success(roadPermissionApplicationService.apply(applyRequest));
    }

    @GetMapping("/myList")
    @ApiOperation("查询当前用户申请列表")
    public Result<List<RoadPermissionApplication>> myList(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            return Result.error("未登录或登录已过期");
        }
        return Result.success(roadPermissionApplicationService.listMyApplications(userId));
    }

    @GetMapping("/enterpriseList")
    @ApiOperation("根据企业ID查询申请列表")
    public Result<List<RoadPermissionApplication>> enterpriseList(@RequestParam Long enterpriseId) {
        return Result.success(roadPermissionApplicationService.listByEnterprise(enterpriseId));
    }

    @PutMapping("/audit")
    @ApiOperation("民警审核道路测试申请")
    public Result<Boolean> audit(@RequestBody RoadAuditRequest auditRequest) {
        return Result.success(roadPermissionApplicationService.audit(auditRequest));
    }

    @GetMapping("/list")
    @ApiOperation("民警审核列表")
    public Result<List<RoadPermissionApplicationVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type
    ) {
        return Result.success(roadPermissionApplicationService.listAll(status, type));
    }
}
