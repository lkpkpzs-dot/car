const request = require('../../utils/request.js');
const enterpriseUtil = require('../../utils/enterprise.js');

Page({
  data: {
    userInfo: {},
    companyName: '未认证企业',
    qualificationStatus: enterpriseUtil.QUALIFICATION_STATUS.NONE,
    statusLabel: '未申请',
    statusColor: '#64748b',
    statusBg: '#f1f5f9',
    stats: {
      vehicleCount: 0,
      applyCount: 0,
      reviewingCount: 0
    },
    quickActions: [
      { id: 'roadApply', name: '道路许可申请', icon: 'monitor', desc: '道路测试/示范应用/应用试点' },
      { id: 'safety', name: '安全员监管', icon: 'check', desc: '资质申请与人员管理' },
      { id: 'myVehicles', name: '我的车辆', icon: 'vehicle', desc: '查看已通过查验的车辆' }
    ]
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 0 });
      this.getTabBar().updateTabList();
    }
    this.loadData();
  },

  async loadData() {
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    const meta = enterpriseUtil.getQualificationMeta(userInfo.qualificationStatus);

    this.setData({
      userInfo,
      companyName: userInfo.enterpriseName || '已认证企业',
      qualificationStatus: userInfo.qualificationStatus,
      statusLabel: meta.label,
      statusColor: meta.color,
      statusBg: meta.bg
    });

    await this.loadDashboardData();
  },

  async loadDashboardData() {
    try {
      wx.showLoading({ title: '加载中...' });
      const res = await request.get('/enterprise/dashboard');
      
      if (res.code === 200 && res.data) {
        const { count } = res.data;
        this.setData({
          stats: {
            vehicleCount: count.vehicle || 0,
            applyCount: count.totalApplication || 0,
            reviewingCount: count.pending || 0
          }
        });
      }
    } catch (err) {
      console.error('[Enterprise Dashboard] Load failed:', err);
    } finally {
      wx.hideLoading();
    }
  },

  onQuickAction(e) {
    const { id } = e.currentTarget.dataset;

    if (id === 'roadApply') {
      wx.navigateTo({ url: '/pages/road/list/index' });
      return;
    }

    if (id === 'safety') {
      wx.navigateTo({ url: '/pages/enterprise/safety/list' });
      return;
    }

    if (id === 'myVehicles') {
      wx.navigateTo({ url: '/pages/enterprise/vehicles/index' });
      return;
    }

    if (id === 'qualification') {
      this.goQualification();
    }
  },

  onStatClick(e) {
    const { type } = e.currentTarget.dataset;
    
    if (type === 'myVehicles') {
      wx.navigateTo({ url: '/pages/enterprise/vehicles/index' });
    } else if (type === 'myApplications') {
      wx.navigateTo({ url: '/pages/road/list/index' });
    } else if (type === 'pending') {
      wx.navigateTo({ url: '/pages/road/list/index?tab=1' });
    }
  },

  goQualification() {
    const { userInfo, qualificationStatus } = this.data;
    if (qualificationStatus === enterpriseUtil.QUALIFICATION_STATUS.APPROVED) {
      wx.navigateTo({ url: '/pages/enterprise/qualification/index?mode=view' });
    } else {
      wx.navigateTo({ url: '/pages/enterprise/qualification/index' });
    }
  }
});
