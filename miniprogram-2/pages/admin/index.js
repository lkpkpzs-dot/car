const app = getApp();
const auth = require('../../utils/auth.js');
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
      { id: 'report', name: '举报审核', icon: 'jubao', desc: '群众举报审核处理' },
      { id: 'safety', name: '安全员监管', icon: 'jianguan', desc: '资质审核与事故处分' },
      { id: 'archive', name: '档案管理', icon: 'archives', desc: '车辆档案查询' },
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
      // 加载统计数据，并合并群众举报审核数据
      const dashboardRes = await request.get('/enterprise/admin/dashboard');
      let reportStats = this.buildReportStats([]);

      try {
        const reportRes = await request.get('/citizenReport/list', {});
        reportStats = this.buildReportStats(request.parseListData(reportRes));
      } catch (reportErr) {
        console.error('Load report stats failed:', reportErr);
      }

      if (dashboardRes.code === 200 && dashboardRes.data) {
        const dashboardStats = dashboardRes.data;
        this.setData({
          stats: {
            pendingCount: this.toNumber(dashboardStats.pendingCount) + reportStats.pendingCount,
            approvedCount: this.toNumber(dashboardStats.approvedCount) + reportStats.approvedCount,
            rejectedCount: this.toNumber(dashboardStats.rejectedCount) + reportStats.rejectedCount,
            todayProcess: this.toNumber(dashboardStats.todayProcess) + reportStats.todayProcess
          }
        });
      }
      
      // 加载待审核列表
      const res = await request.get('/audit/list', {
        isProcessed: false,
        businessType: auditUtil.BUSINESS_TYPE.ENTERPRISE
      });
      const list = request.parseListData(res);
      this.setData({
        recentApproval: list.slice(0, 3).map(item => ({
          id: item.id,
          name: item.title,
          company: item.businessType === 2 ? '企业资质' : '上牌申请',
          status: 'pending',
          businessType: item.businessType
        }))
      });
    } catch (err) {
      console.error('Load admin data failed:', err);
    }
  },

  toNumber(value) {
    const num = Number(value);
    return Number.isFinite(num) ? num : 0;
  },

  buildReportStats(list) {
    const today = new Date();
    const isToday = (time) => {
      if (!time) return false;
      const date = new Date(String(time).replace(/-/g, '/'));
      return date.getFullYear() === today.getFullYear()
        && date.getMonth() === today.getMonth()
        && date.getDate() === today.getDate();
    };

    return (list || []).reduce((stats, item) => {
      const processStatus = this.toNumber(item.processStatus);
      if (processStatus === 0) {
        stats.pendingCount += 1;
      } else if (processStatus === 1) {
        stats.approvedCount += 1;
        if (isToday(item.reviewTime)) stats.todayProcess += 1;
      } else if (processStatus === 2) {
        stats.rejectedCount += 1;
        if (isToday(item.reviewTime)) stats.todayProcess += 1;
      }
      return stats;
    }, {
      pendingCount: 0,
      approvedCount: 0,
      rejectedCount: 0,
      todayProcess: 0
    });
  },

  onQuickAction(e) {
    const actionId = e.currentTarget.dataset.actionId;
    const actionRoutes = {
      audit: '/pages/admin/audit/index',
      roadAudit: '/pages/admin/road/list/index',
      report: '/pages/admin/report/list/index',
      safety: '/pages/admin/safety/list',
      archive: '/pages/admin/archive/index',
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
    } catch (error) {
      console.error('Refresh failed:', error);
      wx.showToast({ title: '刷新失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
