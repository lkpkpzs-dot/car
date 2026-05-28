package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.mapper.CitizenReportMapper;
import org.lkp.car.service.CitizenReportService;
import org.springframework.stereotype.Service;

/**
 * 群众举报 服务实现类
 */
@Service
public class CitizenReportServiceImpl extends ServiceImpl<CitizenReportMapper, CitizenReport> implements CitizenReportService {
}
