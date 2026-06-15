package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.common.cache.CacheConstants;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.entity.SysUser;
import org.lkp.car.mapper.SysMessageMapper;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.service.SysMessageService;
import org.lkp.car.service.SysUserService;
import org.lkp.car.vo.SysMessageDetailVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统消息服务实现类
 * <p>
 * 处理系统消息相关业务，包括：
 * 1. 消息发送与保存
 * 2. 用户消息列表查询
 * 3. 未读消息计数（带缓存优化）
 * 4. 消息阅读状态更新
 * 5. 消息详情查询（含关联业务数据）
 * </p>
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Autowired
    @Lazy
    private CitizenReportService citizenReportService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    @Qualifier("redisCacheManager")
    private CacheManager redisCacheManager;

    @Override
    public boolean save(SysMessage entity) {
        boolean saved = super.save(entity);
        if (saved) {
            evictUnreadCache(entity.getReceiverId());
        }
        return saved;
    }

    @Override
    public List<SysMessage> getMessagesByUserId(Long userId) {
        return this.list(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getReceiverId, userId)
                        .orderByDesc(SysMessage::getCreateTime)
        );
    }

    @Override
    @Cacheable(
            value = CacheConstants.REDIS_MSG_UNREAD,
            key = "#userId",
            cacheManager = "redisCacheManager"
    )
    public Long countUnreadMessages(Long userId) {
        return this.count(
                new LambdaQueryWrapper<SysMessage>()
                        .eq(SysMessage::getReceiverId, userId)
                        .eq(SysMessage::getIsRead, 0)
        );
    }

    @Override
    public boolean markAsRead(Long msgId) {
        SysMessage existing = super.getById(msgId);
        if (existing == null) {
            return false;
        }
        SysMessage message = new SysMessage();
        message.setMsgId(msgId);
        message.setIsRead(1);
        boolean updated = this.updateById(message);
        if (updated) {
            evictUnreadCache(existing.getReceiverId());
        }
        return updated;
    }

    @Override
    public boolean markAllRead(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        // 批量更新用户所有未读消息为已读
        LambdaQueryWrapper<SysMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMessage::getReceiverId, userId)
               .eq(SysMessage::getIsRead, 0);
        
        // 创建更新对象
        SysMessage updateMsg = new SysMessage();
        updateMsg.setIsRead(1);
        
        // 执行批量更新
        boolean updated = this.update(updateMsg, wrapper);
        
        // 清除缓存
        if (updated) {
            evictUnreadCache(userId);
        }
        return updated;
    }

    private void evictUnreadCache(Long userId) {
        if (userId == null || redisCacheManager == null) {
            return;
        }
        if (redisCacheManager.getCache(CacheConstants.REDIS_MSG_UNREAD) != null) {
            redisCacheManager.getCache(CacheConstants.REDIS_MSG_UNREAD).evict(userId);
        }
    }

    @Override
    public SysMessageDetailVO getMessageDetail(Long msgId) {
        // 1. 获取系统消息
        SysMessage message = this.getById(msgId);
        if (message == null) {
            return null;
        }

        // 2. 拷贝系统消息基本信息
        SysMessageDetailVO vo = new SysMessageDetailVO();
        BeanUtils.copyProperties(message, vo);

        // 3. 如果是举报类型的消息（businessType = 1），查询关联的举报信息
        if (message.getBusinessType() != null && message.getBusinessType() == 1 && message.getBusinessId() != null) {
            CitizenReport report = citizenReportService.getById(message.getBusinessId());
            if (report != null) {
                // 拷贝举报信息
                vo.setReportId(report.getReportId());
                vo.setReportUserId(report.getUserId());
                vo.setReportType(report.getReportType());
                vo.setTargetPlate(report.getTargetPlate());
                vo.setEvidenceJson(report.getEvidenceJson());
                vo.setLocationExt(report.getLocationExt());
                vo.setProcessStatus(report.getProcessStatus());
                vo.setReportCreateTime(report.getCreateTime());
                vo.setReviewerId(report.getReviewerId());
                vo.setReviewTime(report.getReviewTime());
                vo.setReviewRemark(report.getReviewRemark());

                // 如果有审核民警ID，查询民警姓名
                if (report.getReviewerId() != null) {
                    SysUser reviewer = sysUserService.getById(report.getReviewerId());
                    if (reviewer != null) {
                        vo.setReviewerName(reviewer.getRealName());
                    }
                }
            }
        }

        return vo;
    }
}
