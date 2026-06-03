package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.vo.SysMessageDetailVO;

import java.util.List;

/**
 * 系统消息 服务类
 */
public interface SysMessageService extends IService<SysMessage> {

    /**
     * 查询当前用户的消息列表
     */
    List<SysMessage> getMessagesByUserId(Long userId);

    /**
     * 查询当前用户未读消息数量
     */
    Long countUnreadMessages(Long userId);

    /**
     * 标记消息为已读
     */
    boolean markAsRead(Long msgId);

    /**
     * 获取系统消息详情（包含关联的举报信息）
     */
    SysMessageDetailVO getMessageDetail(Long msgId);
}
