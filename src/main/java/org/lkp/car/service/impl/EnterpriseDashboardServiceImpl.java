package org.lkp.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.lkp.car.common.enums.VehicleStatusEnum;
import org.lkp.car.entity.CarArchive;
import org.lkp.car.entity.CitizenReport;
import org.lkp.car.entity.RoadPermissionApplication;
import org.lkp.car.entity.VehicleInfo;
import org.lkp.car.entity.VehiclePlate;
import org.lkp.car.service.CarArchiveService;
import org.lkp.car.service.CitizenReportService;
import org.lkp.car.service.EnterpriseDashboardService;
import org.lkp.car.service.RoadPermissionApplicationService;
import org.lkp.car.service.VehicleInfoService;
import org.lkp.car.service.VehiclePlateService;
import org.lkp.car.vo.AdminDashboardVO;
import org.lkp.car.vo.CitizenDashboardVO;
import org.lkp.car.vo.DashboardApplicationVO;
import org.lkp.car.vo.DashboardCountVO;
import org.lkp.car.vo.DashboardVehicleVO;
import org.lkp.car.vo.EnterpriseDashboardVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnterpriseDashboardServiceImpl implements EnterpriseDashboardService {

    @Autowired
    private RoadPermissionApplicationService roadPermissionApplicationService;

    @Autowired
    private VehicleInfoService vehicleInfoService;

    @Autowired
    private VehiclePlateService vehiclePlateService;

    @Autowired
    private CarArchiveService carArchiveService;

    @Autowired
    private CitizenReportService citizenReportService;

    @Override
    public EnterpriseDashboardVO getDashboardData(Long enterpriseId) {
        EnterpriseDashboardVO result = new EnterpriseDashboardVO();

        // 一、查询"我的申请"
        List<RoadPermissionApplication> applications = roadPermissionApplicationService.list(
                new LambdaQueryWrapper<RoadPermissionApplication>()
                        .eq(RoadPermissionApplication::getEnterpriseId, enterpriseId)
                        .orderByDesc(RoadPermissionApplication::getCreateTime)
        );

        // 获取所有申请ID
        List<Long> applicationIds = applications.stream()
                .map(RoadPermissionApplication::getId)
                .collect(Collectors.toList());

        // 二、关联查验状态
        Map<Long, VehicleInfo> vehicleInfoMap = new java.util.HashMap<>();
        if (!applicationIds.isEmpty()) {
            List<VehicleInfo> vehicleInfos = vehicleInfoService.list(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .in(VehicleInfo::getApplicationId, applicationIds)
            );
            vehicleInfoMap = vehicleInfos.stream()
                    .collect(Collectors.toMap(VehicleInfo::getApplicationId, v -> v, (v1, v2) -> v1));
        }

        // 转换为申请列表VO
        List<DashboardApplicationVO> applicationList = new ArrayList<>();
        for (RoadPermissionApplication app : applications) {
            DashboardApplicationVO vo = new DashboardApplicationVO();
            BeanUtils.copyProperties(app, vo);

            // 设置查验状态
            VehicleInfo vehicleInfo = vehicleInfoMap.get(app.getId());
            if (vehicleInfo != null) {
                // 映射后端状态到前端状态：0=待审核，1=通过，2=驳回 → 1=未查验，2=已通过，3=已驳回
                if (VehicleStatusEnum.PASSED.getCode().equals(vehicleInfo.getStatus())) {
                    vo.setInspectionStatus(2);
                } else if (VehicleStatusEnum.REJECTED.getCode().equals(vehicleInfo.getStatus())) {
                    vo.setInspectionStatus(3);
                } else {
                    vo.setInspectionStatus(1);
                }
            } else {
                vo.setInspectionStatus(1);
            }

            // 设置发牌状态（来自申请表）
            vo.setPlateStatus(app.getPlateStatus());

            applicationList.add(vo);
        }
        result.setApplicationList(applicationList);

        // 三、生成"审核中列表" (status = 1)
        List<DashboardApplicationVO> pendingList = applicationList.stream()
                .filter(vo -> vo.getStatus() != null && vo.getStatus() == 1)
                .collect(Collectors.toList());
        result.setPendingList(pendingList);

        // 四、查询"我的车辆" - 正确逻辑：基于 car_archive（正式档案）和 vehicle_plate（已发牌）
        // 1. 先查企业的所有车辆档案
        List<CarArchive> carArchives = carArchiveService.list(
                new LambdaQueryWrapper<CarArchive>()
                        .eq(CarArchive::getEnterpriseId, enterpriseId)
        );

        // 2. 获取这些车辆的 VIN
        List<String> vins = carArchives.stream()
                .map(CarArchive::getVin)
                .collect(Collectors.toList());

        // 3. 查 vehicle_plate 中 status = 1（有效牌照）的记录
        List<VehiclePlate> vehiclePlates = new ArrayList<>();
        Map<String, VehiclePlate> vehiclePlateMap = new java.util.HashMap<>();
        if (!vins.isEmpty()) {
            vehiclePlates = vehiclePlateService.list(
                    new LambdaQueryWrapper<VehiclePlate>()
                            .eq(VehiclePlate::getEnterpriseId, enterpriseId)
                            .in(VehiclePlate::getVin, vins)
                            .eq(VehiclePlate::getStatus, 1) // 1=有效
            );
            vehiclePlateMap = vehiclePlates.stream()
                    .collect(Collectors.toMap(VehiclePlate::getVin, v -> v, (v1, v2) -> v1));
        }

        // 4. 构建车辆列表：必须同时存在档案和有效牌照
        List<DashboardVehicleVO> vehicleList = new ArrayList<>();
        for (CarArchive archive : carArchives) {
            VehiclePlate plate = vehiclePlateMap.get(archive.getVin());
            if (plate != null) {
                DashboardVehicleVO vo = new DashboardVehicleVO();
                vo.setVehicleId(archive.getVehicleInfoId()); // 用 vehicleInfoId 作为 vehicleId
                vo.setVin(archive.getVin());
                vo.setVehicleBrand(archive.getVehicleBrand());
                vo.setPlateNumber(plate.getPlateNumber());
                vo.setPlateType(plate.getPlateType());
                vo.setIssueDate(plate.getIssueDate());
                vehicleList.add(vo);
            }
        }
        result.setVehicleList(vehicleList);

        // 五、统计数量
        DashboardCountVO count = new DashboardCountVO();
        count.setTotalApplication(applications.size());
        count.setPending(pendingList.size());
        count.setVehicle(vehicleList.size());
        result.setCount(count);

        return result;
    }

    @Override
    public AdminDashboardVO getAdminDashboardData() {
        AdminDashboardVO result = new AdminDashboardVO();

        // 统计待审核数量（status = 1）
        long pendingCount = roadPermissionApplicationService.count(
                new LambdaQueryWrapper<RoadPermissionApplication>()
                        .eq(RoadPermissionApplication::getStatus, 1)
        );
        result.setPendingCount((int) pendingCount);

        // 统计已通过数量（status = 2）
        long approvedCount = roadPermissionApplicationService.count(
                new LambdaQueryWrapper<RoadPermissionApplication>()
                        .eq(RoadPermissionApplication::getStatus, 2)
        );
        result.setApprovedCount((int) approvedCount);

        // 统计已驳回数量（status = 3）
        long rejectedCount = roadPermissionApplicationService.count(
                new LambdaQueryWrapper<RoadPermissionApplication>()
                        .eq(RoadPermissionApplication::getStatus, 3)
        );
        result.setRejectedCount((int) rejectedCount);

        // 统计今日办理数量
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        long todayProcess = roadPermissionApplicationService.count(
                new LambdaQueryWrapper<RoadPermissionApplication>()
                        .apply("DATE(audit_time) = {0}", today)
                        .isNotNull(RoadPermissionApplication::getAuditTime)
        );
        result.setTodayProcess((int) todayProcess);

        return result;
    }

    @Override
    public CitizenDashboardVO getCitizenDashboardData(Long userId) {
        CitizenDashboardVO result = new CitizenDashboardVO();

        // 统计我的举报总数
        long totalReport = citizenReportService.count(
                new LambdaQueryWrapper<CitizenReport>()
                        .eq(CitizenReport::getUserId, userId)
        );
        result.setTotalReport((int) totalReport);

        // 统计待核实数量（status = 0）
        long pendingCount = citizenReportService.count(
                new LambdaQueryWrapper<CitizenReport>()
                        .eq(CitizenReport::getUserId, userId)
                        .eq(CitizenReport::getProcessStatus, 0)
        );
        result.setPendingCount((int) pendingCount);

        // 统计已处理数量（status = 1）
        long approvedCount = citizenReportService.count(
                new LambdaQueryWrapper<CitizenReport>()
                        .eq(CitizenReport::getUserId, userId)
                        .eq(CitizenReport::getProcessStatus, 1)
        );
        result.setApprovedCount((int) approvedCount);

        // 统计无效举报数量（status = 2）
        long rejectedCount = citizenReportService.count(
                new LambdaQueryWrapper<CitizenReport>()
                        .eq(CitizenReport::getUserId, userId)
                        .eq(CitizenReport::getProcessStatus, 2)
        );
        result.setRejectedCount((int) rejectedCount);

        return result;
    }
}
