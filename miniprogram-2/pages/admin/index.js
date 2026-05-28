const app = getApp();
const request = require('../../utils/request.js');
const auditUtil = require('../../utils/audit.js');

Page({
  data: {
    role: 'admin',
    roleName: '车管所管理端',
    stats: {
      pendingCount: 0,
      approvedCount: 120,
      rejectedCount: 8,
      todayProcess: 23
    },
    quickActions: [
      { id: 'audit', name: '资质审核', icon: 'apply', desc: '企业资质与号牌申请' },
      { id: 'roadAudit', name: '道路审核', icon: 'monitor', desc: '道路测试与示范应用' },
      { id: 'scan', name: '执勤核验', icon: 'scan', desc: '扫码核验车辆' },
      { id: 'archive', name: '档案管理', icon: 'archive', desc: '车辆档案查询' },
      { id: 'logs', name: '系统日志', icon: 'records', desc: '操作日志记录' }
    ],
    recentApproval: []
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    this.loadData();
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      });
      this.getTabBar().updateTabList();
    }
  },

  async loadData() {
    try {
      const res = await request.get('/audit/list', {
        isProcessed: false,
        businessType: auditUtil.BUSINESS_TYPE.ENTERPRISE
      });
      const list = request.parseListData(res);
      this.setData({
        stats: {
          ...this.data.stats,
          pendingCount: list.length
        },
        recentApproval: list.slice(0, 3).map(item => ({
          id: item.id,
          name: item.title,
          company: item.businessType === 2 ? '企业资质' : '上牌申请',
          status: 'pending',
          businessType: item.businessType
        }))
      });
    } catch (err) {
      console.error('Load admin pending list failed:', err);
    }
  },

  onQuickAction(e) {
    const actionId = e.currentTarget.dataset.actionId;
    const actionRoutes = {
      audit: '/pages/admin/audit/index',
      roadAudit: '/pages/admin/road/list/index',
      archive: '/pages/admin/archive/index',
      scan: '/pages/scan/scan',
      logs: '/pages/logs/logs'
    };

    if (actionRoutes[actionId]) {
      wx.navigateTo({
        url: actionRoutes[actionId]
      });
    }
  },

  onViewAllAudit() {
    wx.navigateTo({
      url: '/pages/admin/audit/index'
    });
  },

  onViewDetail(e) {
    const item = e.currentTarget.dataset.item;
    if (!item) return;
    wx.navigateTo({
      url: `/pages/admin/audit/detail/index?applyId=${item.id}&businessType=${item.businessType}&title=${encodeURIComponent(item.name)}&isProcessed=0`
    });
  },

  onLogout() {
    app.globalData.role = null;
    wx.removeStorageSync('role');
    wx.reLaunch({
      url: '/pages/index/index'
    });
  }
});
