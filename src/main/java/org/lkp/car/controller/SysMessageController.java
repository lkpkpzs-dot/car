package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.SysMessageService;
import org.lkp.car.utils.AuthContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 系统消息控制层
 */
@RestController
@RequestMapping("/sysMessage")
@Api(tags = "系统消息接口")
public class SysMessageController {

    @Autowired
    private SysMessageService sysMessageService;

    /**
     * 获取消息列表
     */
    @GetMapping("/list")
    @ApiOperation("获取消息列表")
    public Result<List<SysMessage>> list() {
        return Result.success(sysMessageService.list());
    }

    /**
     * 获取当前用户的消息列表
     */
    @GetMapping("/myMessages")
    @ApiOperation("获取当前用户的消息列表")
    public Result<List<SysMessage>> getMyMessages(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        List<SysMessage> messages = sysMessageService.getMessagesByUserId(currentUser.getUserId());
        return Result.success(messages);
    }

    /**
     * 获取当前用户未读消息数量
     */
    @GetMapping("/unreadCount")
    @ApiOperation("获取当前用户未读消息数量")
    public Result<Long> getUnreadCount(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.success(0L);
        }
        Long count = sysMessageService.countUnreadMessages(currentUser.getUserId());
        return Result.success(count);
    }

    /**
     * 根据ID获取消息详情
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID获取消息详情")
    public Result<SysMessage> getById(@PathVariable Long id) {
        return Result.success(sysMessageService.getById(id));
    }

    /**
     * 发送系统消息
     */
    @PostMapping("/save")
    @ApiOperation("发送系统消息")
    public Result<Boolean> save(@RequestBody SysMessage sysMessage) {
        return Result.success(sysMessageService.save(sysMessage));
    }

    /**
     * 修改消息（如标记已读）
     */
    @PutMapping("/update")
    @ApiOperation("修改消息（如标记已读）")
    public Result<Boolean> update(@RequestBody SysMessage sysMessage) {
        return Result.success(sysMessageService.updateById(sysMessage));
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/markRead/{id}")
    @ApiOperation("标记消息为已读")
    public Result<Boolean> markAsRead(@PathVariable Long id) {
        return Result.success(sysMessageService.markAsRead(id));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除消息")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysMessageService.removeById(id));
    }
}
