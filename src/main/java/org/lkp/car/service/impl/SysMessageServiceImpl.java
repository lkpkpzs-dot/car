package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.mapper.SysMessageMapper;
import org.lkp.car.service.SysMessageService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Override
    public List<SysMessage> getMessagesByUserId(Long userId) {
        return this.list(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getReceiverId, userId)
                        .orderByDesc(SysMessage::getCreateTime)
        );
    }

    @Override
    public Long countUnreadMessages(Long userId) {
        return this.count(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getReceiverId, userId)
                        .eq(SysMessage::getIsRead, 0)
        );
    }

    @Override
    public boolean markAsRead(Long msgId) {
        SysMessage message = new SysMessage();
        message.setMsgId(msgId);
        message.setIsRead(1);
        return this.updateById(message);
    }
}
