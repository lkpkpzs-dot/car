package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.SysMessage;
import org.lkp.car.mapper.SysMessageMapper;
import org.lkp.car.service.SysMessageService;
import org.springframework.stereotype.Service;

/**
 * 系统消息 服务实现类
 */
@Service
public class SysMessageServiceImpl extends ServiceImpl<SysMessageMapper, SysMessage> implements SysMessageService {
}
