package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.VehiclePlate;
import org.lkp.car.mapper.VehiclePlateMapper;
import org.lkp.car.service.VehiclePlateService;
import org.springframework.stereotype.Service;

/**
 * 车辆牌照 服务实现类
 */
@Service
public class VehiclePlateServiceImpl extends ServiceImpl<VehiclePlateMapper, VehiclePlate> implements VehiclePlateService {
}
