package org.lkp.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.ApprovalRecord;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.ApprovalRecordService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 审批流转记录控制层
 *
 * <p>提交类业务（企业资质、号牌申请等）应先调用 {@link #save} 写入 actionType=1 的留痕，
 * 再由对应审核接口（如 /enterpriseInfo/audit）写入通过/驳回留痕；
 * 待办/已办由 /audit/list 读取本表聚合展示。
 */
@RestController
@RequestMapping("/approvalRecord")
@Api(tags = "审批流转记录接口")
public class ApprovalRecordController {

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @GetMapping("/history")
    @ApiOperation("获取业务审批历史记录")
    public Result<List<ApprovalRecord>> getHistory(
            @RequestParam Integer businessType,
            @RequestParam Long applyId,
            HttpServletRequest request) {

        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getBusinessType, businessType)
               .eq(ApprovalRecord::getApplyId, applyId)
               .orderByDesc(ApprovalRecord::getCreateTime);

        List<ApprovalRecord> records = approvalRecordService.list(wrapper);
        SysUser currentUser = AuthContext.currentUser(request);
        if (AuthContext.isPolice(currentUser)) {
            return Result.success(records);
        }

        boolean belongsToCurrentUser = records.stream()
                .anyMatch(record -> currentUser.getUserId().equals(record.getApplicantId()));
        if (!belongsToCurrentUser) {
            return Result.error(403, "无审批历史查看权限");
        }
        return Result.success(records);
    }

    @GetMapping("/list")
    @ApiOperation("获取审批记录列表")
    public Result<List<ApprovalRecord>> list(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (AuthContext.isPolice(currentUser)) {
            return Result.success(approvalRecordService.list());
        }
        return Result.success(approvalRecordService.list(new LambdaQueryWrapper<ApprovalRecord>()
                .eq(ApprovalRecord::getApplicantId, currentUser.getUserId())));
    }

    @GetMapping("/{id}")
    @ApiOperation("根据ID获取审批记录详情")
    public Result<ApprovalRecord> getById(@PathVariable Long id, HttpServletRequest request) {
        ApprovalRecord record = approvalRecordService.getById(id);
        SysUser currentUser = AuthContext.currentUser(request);
        if (record == null) {
            return Result.success(null);
        }
        if (!AuthContext.isPolice(currentUser) && !currentUser.getUserId().equals(record.getApplicantId())) {
            return Result.error(403, "无审批记录查看权限");
        }
        return Result.success(record);
    }

    /**
     * 新增审批留痕（底层接口）
     * 企业资质申请请使用 POST /enterpriseInfo/apply
     */
    @PostMapping("/save")
    @ApiOperation("新增审批留痕（底层；企业资质请用 /enterpriseInfo/apply）")
    public Result<Boolean> save(@RequestBody ApprovalRecord approvalRecord, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无审批记录维护权限");
        }
        return Result.success(approvalRecordService.save(approvalRecord));
    }

    @PutMapping("/update")
    @ApiOperation("修改审批记录")
    public Result<Boolean> update(@RequestBody ApprovalRecord approvalRecord, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无审批记录维护权限");
        }
        return Result.success(approvalRecordService.updateById(approvalRecord));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除审批记录")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!AuthContext.isPolice(AuthContext.currentUser(request))) {
            return Result.error(403, "无审批记录维护权限");
        }
        return Result.success(approvalRecordService.removeById(id));
    }
}
