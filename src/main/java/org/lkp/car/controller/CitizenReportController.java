package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 群众举报控制层
 */
@RestController
@RequestMapping("/citizenReport")
@Api(tags = "群众举报接口")
public class CitizenReportController {

    @Autowired
    private CitizenReportService citizenReportService;

    /**
     * 获取举报列表
     */
    @GetMapping("/list")
    @ApiOperation("获取举报列表")
    public Result<List<CitizenReport>> list() {
        return Result.success(citizenReportService.list());
    }

    /**
     * 根据ID获取举报详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取举报详情")
    public Result<CitizenReport> getById(@PathVariable Long id) {
        return Result.success(citizenReportService.getById(id));
    }

    /**
     * 提交举报
     */
    @PostMapping("/save")
    @ApiOperation("提交举报")
    public Result<Boolean> save(@RequestBody CitizenReport citizenReport, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser != null) {
            citizenReport.setUserId(currentUser.getUserId());
        }
        return Result.success(citizenReportService.save(citizenReport));
    }

    /**
     * 更新举报处理状态
     */
    @PutMapping("/update")
    @ApiOperation("更新举报处理状态")
    public Result<Boolean> update(@RequestBody CitizenReport citizenReport, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        // 如果是更新处理状态，自动设置审核人
        if (citizenReport.getProcessStatus() != null && (citizenReport.getProcessStatus() == 1 || citizenReport.getProcessStatus() == 2)) {
            if (AuthContext.isPolice(currentUser)) {
                citizenReport.setReviewerId(currentUser.getUserId());
            }
        }
        return Result.success(citizenReportService.updateById(citizenReport));
    }

    /**
     * 删除举报记录
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除举报记录")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(citizenReportService.removeById(id));
    }
}
