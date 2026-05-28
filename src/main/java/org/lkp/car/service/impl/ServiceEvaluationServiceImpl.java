package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.ServiceEvaluation;
import org.lkp.car.mapper.ServiceEvaluationMapper;
import org.lkp.car.service.ServiceEvaluationService;
import org.springframework.stereotype.Service;

/**
 * 服务评价 服务实现类
 */
@Service
public class ServiceEvaluationServiceImpl extends ServiceImpl<ServiceEvaluationMapper, ServiceEvaluation> implements ServiceEvaluationService {
}
