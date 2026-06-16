package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.entity.Feedback;

import java.util.List;

/**
 * 意见建议 服务类
 */
public interface FeedbackService extends IService<Feedback> {

    /**
     * 提交意见建议
     */
    Long submitFeedback(Feedback feedback, Long userId);

    /**
     * 获取当前用户的意见建议列表
     */
    List<Feedback> getMyFeedbackList(Long userId);

    /**
     * 获取所有意见建议列表（管理员/民警使用）
     */
    List<Feedback> getAllFeedbackList(Integer processStatus);

    /**
     * 处理意见建议
     */
    boolean handleFeedback(Long feedbackId, Integer processStatus, String processRemark, Long handlerId);
}
