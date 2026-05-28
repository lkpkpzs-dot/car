package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.lkp.car.common.Result;
import org.lkp.car.dto.AuditTaskVO;
import org.lkp.car.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统一审核任务控制层
 */
@RestController
@RequestMapping("/audit")
@Api(tags = "统一审核任务接口")
public class AuditController {

    @Autowired
    private AuditService auditService;

    /**
     * 获取审核任务列表
     *
     * @param isProcessed  false-待审核, true-已处理
     * @param reviewerId   民警ID（查已办时使用，默认30002）
     * @param businessType 1-号牌, 2-企业资质, 不传-全部
     */
    @GetMapping("/list")
    @ApiOperation("待审核/已处理列表（企业资质建议传 businessType=2）")
    public Result<List<AuditTaskVO>> getAuditList(
            @RequestParam boolean isProcessed,
            @ApiParam("民警用户ID，查已办时使用，默认30002")
            @RequestParam(required = false, defaultValue = "30002") Long reviewerId,
            @ApiParam("业务类型：1-号牌, 2-企业资质, 不传返回全部")
            @RequestParam(required = false) Integer businessType) {
        return Result.success(auditService.listTasks(reviewerId, isProcessed, businessType));
    }
}
