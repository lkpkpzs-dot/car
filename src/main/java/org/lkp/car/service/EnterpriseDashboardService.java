package org.lkp.car.service;

import org.lkp.car.vo.AdminDashboardVO;
import org.lkp.car.vo.EnterpriseDashboardVO;

public interface EnterpriseDashboardService {

    EnterpriseDashboardVO getDashboardData(Long enterpriseId);

    AdminDashboardVO getAdminDashboardData();
}
