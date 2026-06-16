package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.utils.AuthContext;
import org.lkp.car.vo.RoadPermissionApplicationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 兼容性接口控制层
 * 用于兼容旧接口路径
 * 处理兼容性接口
 */
@RestController
@RequestMapping("/roadAudit")
@Api(tags = "兼容性接口")
public class WuDaLangController {

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @GetMapping("/list")
    @ApiOperation("民警审核列表（兼容旧接口）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<List<RoadPermissionApplicationVO>> list(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            HttpServletRequest request
    ) {
        return Result.success(roadPermissionApplicationService.listAll(status, type));
    }
}
