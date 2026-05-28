package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.SafetyOfficerPenalty;
import org.lkp.car.mapper.SafetyOfficerPenaltyMapper;
import org.lkp.car.service.SafetyOfficerPenaltyService;
import org.springframework.stereotype.Service;

/**
 * 安全员事故处分 服务实现类
 */
@Service
public class SafetyOfficerPenaltyServiceImpl extends ServiceImpl<SafetyOfficerPenaltyMapper, SafetyOfficerPenalty>
        implements SafetyOfficerPenaltyService {
}
