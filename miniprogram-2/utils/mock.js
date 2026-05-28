const mockData = {
  getDashboardStat: () => {
    return {
      total: 120,
      online: 98,
      warning: 5,
      todayAdd: 12
    };
  },

  getVehicleList: () => {
    return [
      {
        id: 1,
        name: '无人车-001',
        latitude: 31.23,
        longitude: 121.47,
        status: 'online'
      },
      {
        id: 2,
        name: '无人车-002',
        latitude: 31.25,
        longitude: 121.49,
        status: 'online'
      },
      {
        id: 3,
        name: '无人车-003',
        latitude: 31.22,
        longitude: 121.45,
        status: 'offline'
      },
      {
        id: 4,
        name: '无人车-004',
        latitude: 31.27,
        longitude: 121.50,
        status: 'warning'
      },
      {
        id: 5,
        name: '无人车-005',
        latitude: 31.20,
        longitude: 121.43,
        status: 'online'
      },
      {
        id: 6,
        name: '无人车-006',
        latitude: 31.24,
        longitude: 121.48,
        status: 'online'
      },
      {
        id: 7,
        name: '无人车-007',
        latitude: 31.21,
        longitude: 121.46,
        status: 'warning'
      },
      {
        id: 8,
        name: '无人车-008',
        latitude: 31.26,
        longitude: 121.51,
        status: 'offline'
      }
    ];
  },

  getVehicleDetail: (id) => {
    const vehicles = {
      1: {
        id: 1,
        name: '无人车-001',
        status: 'online',
        license: '沪A12345',
        company: '某科技公司',
        isValid: true,
        speed: '45km/h',
        battery: '85%',
        driver: '自动驾驶',
        route: '浦东机场-张江高科',
        lastUpdate: '2026-05-21 10:30:00'
      },
      2: {
        id: 2,
        name: '无人车-002',
        status: 'online',
        license: '沪B67890',
        company: '智慧出行科技',
        isValid: true,
        speed: '38km/h',
        battery: '72%',
        driver: '自动驾驶',
        route: '虹桥枢纽-静安寺',
        lastUpdate: '2026-05-21 10:28:00'
      },
      3: {
        id: 3,
        name: '无人车-003',
        status: 'offline',
        license: '沪C11111',
        company: '未来交通研究院',
        isValid: false,
        speed: '0km/h',
        battery: '15%',
        driver: '离线状态',
        route: '未知',
        lastUpdate: '2026-05-21 09:45:00'
      },
      4: {
        id: 4,
        name: '无人车-004',
        status: 'warning',
        license: '沪D22222',
        company: '新能源智能汽车',
        isValid: true,
        speed: '20km/h',
        battery: '10%',
        driver: '自动驾驶',
        route: '徐汇滨江-世博园',
        lastUpdate: '2026-05-21 10:29:00'
      },
      5: {
        id: 5,
        name: '无人车-005',
        status: 'online',
        license: '沪E33333',
        company: '智能物流科技',
        isValid: true,
        speed: '50km/h',
        battery: '90%',
        driver: '自动驾驶',
        route: '宝山物流园-杨浦港',
        lastUpdate: '2026-05-21 10:31:00'
      },
      6: {
        id: 6,
        name: '无人车-006',
        status: 'online',
        license: '沪F44444',
        company: '城市智慧交通',
        isValid: true,
        speed: '42km/h',
        battery: '68%',
        driver: '自动驾驶',
        route: '陆家嘴-外滩',
        lastUpdate: '2026-05-21 10:27:00'
      },
      7: {
        id: 7,
        name: '无人车-007',
        status: 'warning',
        license: '沪G55555',
        company: '自动驾驶研究院',
        isValid: false,
        speed: '5km/h',
        battery: '5%',
        driver: '紧急模式',
        route: '异常区域',
        lastUpdate: '2026-05-21 10:32:00'
      },
      8: {
        id: 8,
        name: '无人车-008',
        status: 'offline',
        license: '沪H66666',
        company: '科技创新企业',
        isValid: true,
        speed: '0km/h',
        battery: '0%',
        driver: '维修中',
        route: '维修站',
        lastUpdate: '2026-05-21 08:00:00'
      }
    };
    return vehicles[id] || vehicles[1];
  },

  getRunningTrend: () => {
    return {
      dates: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      data: [85, 92, 88, 95, 98, 102, 110]
    };
  },

  getWarningTrend: () => {
    return {
      dates: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      data: [8, 5, 7, 4, 6, 3, 5]
    };
  },

  getApproveList: () => {
    return [
      {
        id: 1,
        name: '智能物流配送车-01',
        company: '智慧物流科技',
        type: '配送',
        status: 'pending',
        applyTime: '2026-05-21 09:30:00',
        reason: '日常运营申请'
      },
      {
        id: 2,
        name: '园区接驳车-03',
        company: '张江高科技园区',
        type: '接驳',
        status: 'approved',
        applyTime: '2026-05-20 14:20:00',
        reason: '园区内固定线路运营'
      },
      {
        id: 3,
        name: '道路巡检车-02',
        company: '市政养护公司',
        type: '巡检',
        status: 'rejected',
        applyTime: '2026-05-19 10:15:00',
        reason: '申请材料不完整'
      }
    ];
  },

  submitApproval: (data) => {
    return {
      success: true,
      message: '提交成功',
      id: Math.floor(Math.random() * 10000)
    };
  },

  processApproval: (id, action) => {
    return {
      success: true,
      message: action === 'approve' ? '审批通过' : '审批驳回'
    };
  }
};

module.exports = mockData;
