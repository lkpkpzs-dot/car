package org.lkp.car.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.lkp.car.dto.EnterpriseApplyRequest;
import org.lkp.car.dto.EnterpriseAuditRequest;
import org.lkp.car.dto.MyEnterpriseStatusResponse;
import org.lkp.car.entity.EnterpriseInfo;

/**
 * 企业资质 服务类
 */
public interface EnterpriseInfoService extends IService<EnterpriseInfo> {

    /**
     * 获取指定用户的企业资质状态
     */
    MyEnterpriseStatusResponse getMyStatus(Long userId);

    /**
     * 企业资质申请（保存/更新企业信息 + 写入提交留痕）
     *
     * @return 企业ID
     */
    Long apply(EnterpriseApplyRequest applyRequest);

    /**
     * 民警审核企业资质
     */
    boolean audit(EnterpriseAuditRequest auditRequest);
}
