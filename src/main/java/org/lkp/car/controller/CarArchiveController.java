package org.lkp.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.dto.AssignSafetyOfficerRequest;
import org.lkp.car.dto.GenerateArchiveRequest;
import org.lkp.car.dto.IssuePlateRequest;
import org.lkp.car.entity.CarArchive;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.CarArchiveService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 车辆电子档案控制层
 */
@RestController
@RequestMapping("/carArchive")
@Api(tags = "车辆电子档案接口")
public class CarArchiveController {

    private static final String PLEASE_COMPLETE_ENTERPRISE_AUTH_AND_BINDING = "请先完成企业资质认证和企业绑定";
    private static final String NO_VEHICLE_ARCHIVE_VIEW_PERMISSION = "无车辆档案查看权限";
    private static final String NO_VEHICLE_ARCHIVE_MAINTENANCE_PERMISSION = "无车辆档案维护权限";
    private static final String NO_VEHICLE_ARCHIVE_GENERATE_PERMISSION = "无车辆档案生成权限";
    private static final String NO_PLATE_ISSUANCE_PERMISSION = "无发牌权限";
    private static final String NO_SAFETY_OFFICER_ASSIGNMENT_PERMISSION = "无分配安全员权限";
    private static final String NO_QUERY_PERMISSION = "无权限查询";

    @Autowired
    private CarArchiveService carArchiveService;

    /**
     * 获取车辆列表
     */
    @GetMapping("/list")
    @ApiOperation("获取车辆列表")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE})
    public Result<List<CarArchive>> list(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (AuthContext.isPolice(currentUser)) {
            return Result.success(carArchiveService.list());
        }
        if (!AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, PLEASE_COMPLETE_ENTERPRISE_AUTH_AND_BINDING);
        }
        return Result.success(carArchiveService.list(new LambdaQueryWrapper<CarArchive>()
                .eq(CarArchive::getEnterpriseId, currentUser.getAuthEnterpriseId())));
    }

    /**
     * 根据VIN获取车辆详情
     */
    @GetMapping("/{vin}")
    @ApiOperation("根据VIN获取车辆详情")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE})
    public Result<CarArchive> getById(@PathVariable String vin, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        CarArchive archive = carArchiveService.getById(vin);
        if (archive == null) {
            return Result.success(null);
        }
        if (!AuthContext.isPolice(currentUser)
                && (!AuthContext.hasEnterprise(currentUser)
                || !currentUser.getAuthEnterpriseId().equals(archive.getEnterpriseId()))) {
            return Result.error(403, NO_VEHICLE_ARCHIVE_VIEW_PERMISSION);
        }
        return Result.success(archive);
    }

    /**
     * 新增车辆档案
     */
    @PostMapping("/save")
    @ApiOperation("新增车辆档案")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> save(@RequestBody CarArchive carArchive, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, NO_VEHICLE_ARCHIVE_MAINTENANCE_PERMISSION);
        }
        return Result.success(carArchiveService.save(carArchive));
    }

    /**
     * 修改车辆档案
     */
    @PutMapping("/update")
    @ApiOperation("修改车辆档案")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> update(@RequestBody CarArchive carArchive, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, NO_VEHICLE_ARCHIVE_MAINTENANCE_PERMISSION);
        }
        return Result.success(carArchiveService.updateById(carArchive));
    }

    /**
     * 删除车辆档案
     */
    @DeleteMapping("/{vin}")
    @ApiOperation("删除车辆档案")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> delete(@PathVariable String vin, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, NO_VEHICLE_ARCHIVE_MAINTENANCE_PERMISSION);
        }
        return Result.success(carArchiveService.removeById(vin));
    }

    /**
     * 从查验生成车辆档案
     */
    @PostMapping("/generateFromInspection")
    @ApiOperation("从查验生成车辆档案")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<CarArchive> generateFromInspection(@RequestBody GenerateArchiveRequest archiveRequest,
                                                     HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, NO_VEHICLE_ARCHIVE_GENERATE_PERMISSION);
        }
        return Result.success(carArchiveService.generateFromInspection(archiveRequest));
    }

    /**
     * 民警发牌
     */
    @PostMapping("/issuePlate")
    @ApiOperation("民警发牌")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> issuePlate(@RequestBody IssuePlateRequest issueRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, NO_PLATE_ISSUANCE_PERMISSION);
        }
        issueRequest.setIssuerId(currentUser.getUserId());
        return Result.success(carArchiveService.issuePlate(issueRequest));
    }

    /**
     * 分配安全员
     */
    @PostMapping("/assignSafetyOfficer")
    @ApiOperation("分配安全员")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE})
    public Result<Boolean> assignSafetyOfficer(@RequestBody AssignSafetyOfficerRequest request, HttpServletRequest httpRequest) {
        SysUser currentUser = AuthContext.currentUser(httpRequest);
        if (!AuthContext.isPolice(currentUser) && !AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, NO_SAFETY_OFFICER_ASSIGNMENT_PERMISSION);
        }
        return Result.success(carArchiveService.assignSafetyOfficer(request));
    }

    /**
     * 获取安全员关联的车辆列表
     */
    @GetMapping("/byOfficer/{officerId}")
    @ApiOperation("获取安全员关联的车辆列表")
    @RequireRole({RoleEnum.POLICE_CODE, RoleEnum.ENTERPRISE_CODE})
    public Result<List<CarArchive>> getVehiclesByOfficerId(@PathVariable Long officerId, HttpServletRequest httpRequest) {
        SysUser currentUser = AuthContext.currentUser(httpRequest);
        if (!AuthContext.isPolice(currentUser) && !AuthContext.hasEnterprise(currentUser)) {
            return Result.error(403, NO_QUERY_PERMISSION);
        }
        return Result.success(carArchiveService.getVehiclesByOfficerId(officerId));
    }
}
