package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.ApprovalRecord;
import org.lkp.car.mapper.ApprovalRecordMapper;
import org.lkp.car.service.ApprovalRecordService;
import org.springframework.stereotype.Service;

/**
 * 审批流转记录 服务实现类
 */
@Service
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord> implements ApprovalRecordService {
}
