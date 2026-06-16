package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.Feedback;
import org.lkp.car.mapper.FeedbackMapper;
import org.lkp.car.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    public Long submitFeedback(Feedback feedback, Long userId) {
        // 设置用户ID
        feedback.setUserId(userId);
        // 初始状态为待处理
        feedback.setProcessStatus(0);
        // 保存到数据库
        this.save(feedback);
        return feedback.getFeedbackId();
    }

    @Override
    public List<Feedback> getMyFeedbackList(Long userId) {
        return this.list(
                new LambdaQueryWrapper<Feedback>()
                        .eq(Feedback::getUserId, userId)
                        .orderByDesc(Feedback::getCreateTime)
        );
    }

    @Override
    public List<Feedback> getAllFeedbackList(Integer processStatus) {
        LambdaQueryWrapper<Feedback> wrapper = new LambdaQueryWrapper<>();
        // 如果有状态筛选，添加条件
        if (processStatus != null) {
            wrapper.eq(Feedback::getProcessStatus, processStatus);
        }
        wrapper.orderByDesc(Feedback::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean handleFeedback(Long feedbackId, Integer processStatus, String processRemark, Long handlerId) {
        Feedback feedback = new Feedback();
        feedback.setFeedbackId(feedbackId);
        feedback.setProcessStatus(processStatus);
        feedback.setProcessRemark(processRemark);
        feedback.setHandlerId(handlerId);
        feedback.setHandleTime(new Date());
        return this.updateById(feedback);
    }
}
