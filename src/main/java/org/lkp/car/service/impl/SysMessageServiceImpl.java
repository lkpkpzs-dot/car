package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {

    @Autowired
    @Lazy
    private CitizenReportService citizenReportService;

    @Autowired
    private SysUserService sysUserService;

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
