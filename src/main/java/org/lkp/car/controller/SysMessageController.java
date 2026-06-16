package org.lkp.car.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.lkp.car.common.Result;
import org.lkp.car.common.annotation.RequireRole;
import org.lkp.car.common.enums.RoleEnum;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.service.SysMessageService;
import org.lkp.car.utils.AuthContext;
import org.lkp.car.vo.SysMessageDetailVO;
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
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<List<SysMessage>> list() {
        return Result.success(sysMessageService.list());
    }

    /**
     * 获取当前用户的消息列表
     */
    @GetMapping("/myMessages")
    @ApiOperation("获取当前用户的消息列表")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
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
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
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
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<SysMessage> getById(@PathVariable Long id, HttpServletRequest request) {
        SysMessage message = sysMessageService.getById(id);
        if (message == null) {
            return Result.success(null);
        }
        SysUser currentUser = AuthContext.currentUser(request);
        if (!canAccessMessage(currentUser, message)) {
            return Result.error(403, "无消息查看权限");
        }
        return Result.success(message);
    }

    /**
     * 获取系统消息详情（包含关联的举报信息）
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取系统消息详情（包含关联的举报信息）")
    public Result<SysMessageDetailVO> getDetail(@PathVariable Long id, HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        SysMessageDetailVO detail = sysMessageService.getMessageDetail(id);
        if (detail == null) {
            return Result.error("消息不存在");
        }
        SysMessage message = sysMessageService.getById(id);
        if (!canAccessMessage(currentUser, message)) {
            return Result.error(403, "无消息查看权限");
        }
        return Result.success(detail);
    }

    /**
     * 发送系统消息
     */
    @PostMapping("/save")
    @ApiOperation("发送系统消息")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> save(@RequestBody SysMessage sysMessage) {
        return Result.success(sysMessageService.save(sysMessage));
    }

    /**
     * 修改消息（如标记已读）
     */
    @PutMapping("/update")
    @ApiOperation("修改消息（如标记已读）")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> update(@RequestBody SysMessage sysMessage) {
        return Result.success(sysMessageService.updateById(sysMessage));
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/markRead/{id}")
    @ApiOperation("标记消息为已读")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<Boolean> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        SysMessage message = sysMessageService.getById(id);
        if (message == null) {
            return Result.error("消息不存在");
        }
        if (!canAccessMessage(AuthContext.currentUser(request), message)) {
            return Result.error(403, "无消息操作权限");
        }
        return Result.success(sysMessageService.markAsRead(id));
    }

    /**
     * 标记当前用户所有消息为已读
     */
    @PutMapping("/markAllRead")
    @ApiOperation("标记当前用户所有消息为已读")
    @RequireRole({RoleEnum.CITIZEN_CODE, RoleEnum.ENTERPRISE_CODE, RoleEnum.POLICE_CODE})
    public Result<Boolean> markAllRead(HttpServletRequest request) {
        SysUser currentUser = AuthContext.currentUser(request);
        if (currentUser == null) {
            return Result.error("请先登录");
        }
        return Result.success(sysMessageService.markAllRead(currentUser.getUserId()));
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除消息")
    @RequireRole({RoleEnum.POLICE_CODE})
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysMessageService.removeById(id));
    }

    private boolean canAccessMessage(SysUser currentUser, SysMessage message) {
        if (currentUser == null || message == null) {
            return false;
        }
        return AuthContext.isPolice(currentUser)
                || currentUser.getUserId().equals(message.getReceiverId());
    }
}
