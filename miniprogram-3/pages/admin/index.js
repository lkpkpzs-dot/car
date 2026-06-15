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
      { id: 'audit', name: '资质审核', icon: 'apply', desc: '企业资质审核', unreadCount: 0 },
      { id: 'roadAudit', name: '道路审核', icon: 'monitor', desc: '审核/查验/发牌', unreadCount: 0 },
      { id: 'report', name: '举报审核', icon: 'jubao', desc: '群众举报审核处理', unreadCount: 0 },
      { id: 'feedback', name: '意见建议', icon: 'suggest', desc: '意见建议处理', unreadCount: 0 },
      { id: 'reportMgmt', name: '防恶意举报', icon: 'admin', desc: '用户举报权限管理', unreadCount: 0 },
      { id: 'safety', name: '安全员监管', icon: 'jianguan', desc: '资质审核与事故处分', unreadCount: 0 },
      { id: 'archive', name: '档案管理', icon: 'archives', desc: '车辆档案查询', unreadCount: 0 },
      { id: 'logs', name: '系统日志', icon: 'records', desc: '操作日志记录', unreadCount: 0 }
    ],
    recentApproval: []
  },

  onLoad() {
    // 数据加载统一在 onShow
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
      const dashboardRes = await request.get('/enterprise/admin/dashboard');
      let reportList = [];

      try {
        const reportRes = await request.get('/citizenReport/list', {});
        reportList = request.parseListData(reportRes);
      } catch (reportErr) {
        console.error('Load report stats failed:', reportErr);
      }

      const reportStats = this.buildReportStats(reportList);

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
      
      const res = await request.get('/audit/list', {
        isProcessed: false,
        businessType: auditUtil.BUSINESS_TYPE.ENTERPRISE
      });
      const auditList = request.parseListData(res);
      this.setData({
        recentApproval: auditList.slice(0, 3).map(item => ({
          id: item.id,
          name: item.title,
          company: item.businessType === 2 ? '企业资质' : '上牌申请',
          status: 'pending',
          businessType: item.businessType
        }))
      });

      await this.loadUnreadCounts(auditList, reportList);
    } catch (err) {
      console.error('Load admin data failed:', err);
    }
  },

  async loadUnreadCounts(auditList, reportList) {
    const quickActions = [...this.data.quickActions];
    
    try {
      if (auditList) {
        const auditIndex = quickActions.findIndex(a => a.id === 'audit');
        if (auditIndex !== -1) {
          quickActions[auditIndex].unreadCount = auditList.length;
        }
      } else {
        try {
          const auditRes = await request.get('/audit/list', {
            isProcessed: false,
            businessType: auditUtil.BUSINESS_TYPE.ENTERPRISE
          });
          const list = request.parseListData(auditRes);
          const auditIndex = quickActions.findIndex(a => a.id === 'audit');
          if (auditIndex !== -1) {
            quickActions[auditIndex].unreadCount = list.length;
          }
        } catch (e) {
          console.error('Load audit count failed:', e);
        }
      }

      if (reportList) {
        const pendingReports = reportList.filter(item => this.toNumber(item.processStatus) === 0);
        const reportIndex = quickActions.findIndex(a => a.id === 'report');
        if (reportIndex !== -1) {
          quickActions[reportIndex].unreadCount = pendingReports.length;
        }
      } else {
        try {
          const reportRes = await request.get('/citizenReport/list', {});
          const list = request.parseListData(reportRes);
          const pendingReports = list.filter(item => this.toNumber(item.processStatus) === 0);
          const reportIndex = quickActions.findIndex(a => a.id === 'report');
          if (reportIndex !== -1) {
            quickActions[reportIndex].unreadCount = pendingReports.length;
          }
        } catch (e) {
          console.error('Load report count failed:', e);
        }
      }

      // 3. 意见建议未处理数量
      try {
        const feedbackRes = await request.get('/feedback/admin/list', {});
        const feedbackList = request.parseListData(feedbackRes);
        const pendingFeedback = feedbackList.filter(item => {
          const status = item.processStatus !== undefined ? item.processStatus : item.status;
          return typeof status === 'number' ? status === 0 : status === 'pending';
        });
        const feedbackIndex = quickActions.findIndex(a => a.id === 'feedback');
        if (feedbackIndex !== -1) {
          quickActions[feedbackIndex].unreadCount = pendingFeedback.length;
        }
      } catch (e) {
        console.error('Load feedback count failed:', e);
      }

      // 4. 道路审核未处理数量
      try {
        const roadRes = await request.get('/roadAudit/list', {});
        const roadList = request.parseListData(roadRes);
        const pendingRoad = roadList.filter(item => this.toNumber(item.status) === 1);
        const roadIndex = quickActions.findIndex(a => a.id === 'roadAudit');
        if (roadIndex !== -1) {
          quickActions[roadIndex].unreadCount = pendingRoad.length;
        }
      } catch (e) {
        console.error('Load road audit count failed:', e);
      }

      this.setData({ quickActions });
    } catch (error) {
      console.error('Load unread counts failed:', error);
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
      feedback: '/pages/admin/feedback/list',
      reportMgmt: '/pages/admin/report/user-mgmt/index',
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
