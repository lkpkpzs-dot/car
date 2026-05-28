package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.lkp.car.entity.LicenseApplication;
import org.lkp.car.mapper.LicenseApplicationMapper;
import org.lkp.car.service.LicenseApplicationService;
import org.springframework.stereotype.Service;

import org.lkp.car.entity.EnterpriseInfo;
import org.lkp.car.service.EnterpriseInfoService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 号牌申请 服务实现类
 */
@Service
public class LicenseApplicationServiceImpl extends ServiceImpl<LicenseApplicationMapper, LicenseApplication> implements LicenseApplicationService {

    @Autowired
    private EnterpriseInfoService enterpriseInfoService;

    @Override
    public boolean save(LicenseApplication entity) {
        // 校验企业资质状态
        EnterpriseInfo enterprise = enterpriseInfoService.getById(entity.getEnterpriseId());
        if (enterprise == null) {
            throw new RuntimeException("申请企业不存在");
        }
        
        // audit_status: 1-通过
        if (enterprise.getAuditStatus() == null || enterprise.getAuditStatus() != 1) {
            throw new RuntimeException("企业资质尚未通过审核，无法申请上牌");
        }
        
        return super.save(entity);
    }
}
