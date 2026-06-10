const app = getApp();
const auth = require('../../utils/auth.js');
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
    unreadCount: 0,
    quickActions: [
      { id: 'roadApply', name: '道路许可申请', icon: 'monitor', desc: '道路测试/示范应用/应用试点', badgeCount: 0 },
      { id: 'safety', name: '安全员监管', icon: 'monitor', desc: '资质申请与人员管理', badgeCount: 0 },
      { id: 'myVehicles', name: '我的车辆', icon: 'vehicle', desc: '查看已通过查验的车辆', badgeCount: 0 },
      { id: 'reportManage', name: '举报管理', icon: 'jubao', desc: '处理市民举报', badgeCount: 0 },
      { id: 'messages', name: '消息中心', icon: 'messages', desc: '查看系统通知', badgeCount: 0 }
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

    await Promise.all([
      this.loadDashboardData(),
      this.loadPendingCounts()
    ]);
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

  onGoToMessages() {
    wx.navigateTo({ url: '/pages/enterprise/messages/index' });
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

    if (id === 'reportManage') {
      wx.navigateTo({ url: '/pages/enterprise/report/list' });
      return;
    }

    if (id === 'messages') {
      wx.navigateTo({ url: '/pages/enterprise/messages/index' });
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

  async loadPendingCounts() {
    try {
      // 获取待处理举报数量
      const reportRes = await request.get('/citizenReport/enterpriseList');
      let reportPendingCount = 0;
      if (reportRes.code === 200 && reportRes.data) {
        const list = request.parseListData(reportRes) || [];
        reportPendingCount = list.filter(item => item.processStatus === 1).length;
      }

      // 获取未读消息数量
      let unreadCount = 0;
      try {
        const messagesRes = await request.get('/sysMessage/myMessages');
        if (messagesRes.code === 200 && messagesRes.data) {
          const messages = messagesRes.data || [];
          unreadCount = messages.filter(item => item.isRead === 0).length;
        }
      } catch (msgErr) {
        console.error('[Load Unread Messages] Failed:', msgErr);
      }

      // 更新 quickActions 中的 badgeCount 和右上角的 unreadCount
      const quickActions = this.data.quickActions.map(item => {
        if (item.id === 'reportManage') {
          return { ...item, badgeCount: reportPendingCount };
        }
        if (item.id === 'messages') {
          return { ...item, badgeCount: unreadCount };
        }
        return { ...item, badgeCount: 0 };
      });

      this.setData({ 
        quickActions,
        unreadCount
      });
    } catch (err) {
      console.error('[Load Pending Counts] Failed:', err);
    }
  },

  async onRefresh() {
    wx.showLoading({ title: '刷新中...' });
    try {
      // 清理缓存并重新登录
      wx.removeStorageSync('token');
      wx.removeStorageSync('userInfo');
      wx.removeStorageSync('role');
      if (app && app.globalData) {
        app.globalData.role = null;
      }
      
      // 重新登录
      const loginResult = await auth.login();
      if (loginResult) {
        // 根据最新角色跳转到正确页面
        auth.navigateByRole(loginResult.roleType);
        return;
      }
      wx.showToast({ title: '刷新成功', icon: 'success' });
    } catch (err) {
      console.error('[Refresh] Failed:', err);
      wx.showToast({ title: '刷新失败', icon: 'none' });
    } finally {
      wx.hideLoading();
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
