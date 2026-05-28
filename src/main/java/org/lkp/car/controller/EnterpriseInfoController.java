package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.dto.EnterpriseApplyRequest;
import org.lkp.car.dto.EnterpriseAuditRequest;
import org.lkp.car.dto.MyEnterpriseStatusResponse;
import org.lkp.car.entity.EnterpriseInfo;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.EnterpriseInfoService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 企业资质控制层
 */
@RestController
@RequestMapping("/enterpriseInfo")
@Api(tags = "企业资质接口")
public class EnterpriseInfoController {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    /**
     * 获取我的企业资质状态
     */
    @GetMapping("/myStatus")
    @ApiOperation("获取我的企业资质状态")
    public Result<MyEnterpriseStatusResponse> getMyStatus(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        return Result.success(enterpriseInfoService.getMyStatus(currentUser.getUserId()));
    }

    /**
     * 企业资质申请（推荐入口：写主表 + 自动写 approval_record 提交留痕）
     */
    @PostMapping("/apply")
    @ApiOperation("企业资质申请（保存企业信息并写入审批留痕）")
    public Result<Long> apply(@RequestBody EnterpriseApplyRequest applyRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        applyRequest.setApplicantId(currentUser.getUserId());
        return Result.success(enterpriseInfoService.apply(applyRequest));
    }

    /**
     * 民警审核企业资质
     */
    @PutMapping("/audit")
    @ApiOperation("民警审核企业资质")
    public Result<Boolean> audit(@RequestBody EnterpriseAuditRequest auditRequest, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无审核权限");
        }
        auditRequest.setReviewerId(currentUser.getUserId());
        return Result.success(enterpriseInfoService.audit(auditRequest));
    }

    @GetMapping("/list")
    @ApiOperation("获取企业列表")
    public Result<List<EnterpriseInfo>> list(HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无企业列表查看权限");
        }
        return Result.success(enterpriseInfoService.list());
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取企业详情")
    public Result<EnterpriseInfo> getById(@PathVariable Long id) {
        return Result.success(enterpriseInfoService.getById(id));
    }

    /**
     * 仅保存企业主表，不发起审批流程
     */
    @PostMapping("/save")
    @ApiOperation("保存企业信息（不发起资质申请，申请请用 /apply）")
    public Result<Boolean> save(@RequestBody EnterpriseInfo enterpriseInfo, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无企业信息维护权限");
        }
        return Result.success(enterpriseInfoService.save(enterpriseInfo));
    }

    @PutMapping("/update")
    @ApiOperation("修改企业信息")
    public Result<Boolean> update(@RequestBody EnterpriseInfo enterpriseInfo, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无企业信息维护权限");
        }
        return Result.success(enterpriseInfoService.updateById(enterpriseInfo));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除企业信息")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无企业信息维护权限");
        }
        return Result.success(enterpriseInfoService.removeById(id));
    }
}
