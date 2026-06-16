package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.Feedback;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.FeedbackService;
import org.lkp.car.service.SysMessageService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 意见建议控制层
 */
@RestController
@RequestMapping("/feedback")
@Api(tags = "意见建议接口")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 提交意见建议
     */
    @PostMapping("/submit")
    @ApiOperation("提交意见建议")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<Long> submit(@RequestBody Feedback feedback, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        Long feedbackId = feedbackService.submitFeedback(feedback, currentUser.getUserId());
        return Result.success(feedbackId);
    }

    /**
     * 获取我的意见建议列表
     */
    @GetMapping("/myList")
    @ApiOperation("获取我的意见建议列表")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<List<Feedback>> myList(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        List<Feedback> list = feedbackService.getMyFeedbackList(currentUser.getUserId());
        return Result.success(list);
    }

    /**
     * 获取所有意见建议列表（管理员/民警使用）
     */
    @GetMapping("/admin/list")
    @ApiOperation("获取所有意见建议列表（管理员/民警使用）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<List<Feedback>> list(
            @ApiParam(value = "处理状态: 0-待处理, 1-处理中, 2-已处理")
            @RequestParam(required = false) Integer processStatus,
            HttpServletRequest request) {
        // 验证权限 - 只有民警可以查看
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限访问");
        }
        List<Feedback> list = feedbackService.getAllFeedbackList(processStatus);
        return Result.success(list);
    }

    /**
     * 获取意见建议详情（普通用户）
     */
    @GetMapping("/detail")
    @ApiOperation("获取意见建议详情")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<Feedback> detail(
            @ApiParam(value = "意见建议ID", required = true)
            @RequestParam Long feedbackId,
            HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        Feedback feedback = feedbackService.getById(feedbackId);
        if (feedback == null) {
            return Result.error("意见建议不存在");
        }
        // 验证权限 - 只有提交者或民警可以查看
        if (!AuthContext.isPolice(currentUser) && !feedback.getUserId().equals(currentUser.getUserId())) {
            return Result.error(403, "无权限访问");
        }
        return Result.success(feedback);
    }

    /**
     * 获取意见建议详情（管理员/民警）
     */
    @GetMapping("/admin/detail")
    @ApiOperation("获取意见建议详情（管理员/民警）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Feedback> adminDetail(
            @ApiParam(value = "意见建议ID", required = true)
            @RequestParam Long feedbackId,
            HttpServletRequest request) {
        // 直接调用普通用户的详情方法
        return detail(feedbackId, request);
    }

    /**
     * 处理意见建议（管理员/民警使用）
     */
    @PostMapping("/admin/handle")
    @ApiOperation("处理意见建议（管理员/民警使用）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> handle(
            @ApiParam(value = "意见建议ID", required = true)
            @RequestParam Long feedbackId,
            @ApiParam(value = "处理状态: 0-待处理, 1-处理中, 2-已处理", required = true)
            @RequestParam Integer processStatus,
            @ApiParam(value = "处理备注")
            @RequestParam(required = false) String processRemark,
            HttpServletRequest request) {
        // 验证权限
        SysUser currentUser = AuthContext.currentUser(request);
        if (!AuthContext.isPolice(currentUser)) {
            return Result.error(403, "无权限操作");
        }
        // 验证状态参数
        if (processStatus == null || processStatus < 0 || processStatus > 2) {
            return Result.error("处理状态不正确");
        }
        boolean result = feedbackService.handleFeedback(
                feedbackId, processStatus, processRemark, currentUser.getUserId());
        
        // 如果处理成功，发送消息通知用户
        if (result) {
            Feedback feedback = feedbackService.getById(feedbackId);
            if (feedback != null && feedback.getUserId() != null) {
                sendFeedbackNotification(feedback, processStatus, processRemark, currentUser);
            }
        }
        
        return Result.success(result);
    }

    /**
     * 发送意见建议处理通知消息
     */
    private void sendFeedbackNotification(Feedback feedback, Integer processStatus, String processRemark, SysUser police) {
        String statusText;
        switch (processStatus) {
            case 1:
                statusText = "处理中";
                break;
            case 2:
                statusText = "已处理";
                break;
            default:
                statusText = "待处理";
        }

        SysMessage message = new SysMessage();
        message.setReceiverId(feedback.getUserId());
        message.setMsgType(4); // 举报处理通知类型
        message.setBusinessType(2); // 2-意见建议（1-举报）
        message.setBusinessId(feedback.getFeedbackId());
        message.setTitle("意见建议处理通知");
        
        String content;
        if (processRemark != null && !processRemark.isEmpty()) {
            content = String.format(
                "您提交的意见建议「%s」已被民警%s处理，当前状态：%s。处理备注：%s",
                feedback.getTitle(),
                police.getRealName() != null ? police.getRealName() : "管理员",
                statusText,
                processRemark
            );
        } else {
            content = String.format(
                "您提交的意见建议「%s」已被民警%s处理，当前状态：%s",
                feedback.getTitle(),
                police.getRealName() != null ? police.getRealName() : "管理员",
                statusText
            );
        }
        message.setContent(content);
        message.setIsRead(0);
        sysMessageService.save(message);
    }
}
