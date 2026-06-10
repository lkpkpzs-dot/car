const app = getApp();
const auth = require('../../utils/auth.js');
const enterpriseUtil = require('../../utils/enterprise.js');
const request = require('../../utils/request.js');

Page({
  data: {
    role: 'citizen',
    roleName: '市民监督端',
    userInfo: {},
    qualificationStatus: -1,
    statusLabel: '未申请',
    statusColor: '#64748b',
    statusBg: '#f1f5f9',
    unreadCount: 0,
    stats: {
      totalReport: 0,
      pendingCount: 0,
      approvedCount: 0,
      rejectedCount: 0
    },
    quickActions: [
      { id: 'report', name: '违规举报', icon: 'check', desc: '举报违规行为' },
      { id: 'myReports', name: '我的举报', icon: 'myreport', desc: '查看我的举报' },
      { id: 'apply', name: '申请企业资质', icon: 'archivee', desc: '提交企业认证申请' },
      { id: 'messages', name: '我的消息', icon: 'users', desc: '查看消息通知' },
      { id: 'feedback', name: '意见建议', icon: 'shiminsuggest', desc: '提交意见建议' },
      { id: 'about', name: '使用手册', icon: 'about', desc: '了解使用说明' }
    ],
    tips: [
      { id: 1, title: '如何举报违规行为', desc: '点击"违规举报"，选择举报类型，上传证据图片' },
      { id: 2, title: '处理进度查询', desc: '提交后可在"我的消息"中查看处理状态' }
    ]
  },

  onLoad() {
    this.loadUserInfo();
    this.loadUnreadCount();
    this.loadStats();
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      });
      this.getTabBar().updateTabList();
    }
    this.loadUserInfo();
    this.loadUnreadCount();
    this.loadStats();
  },

  async loadUnreadCount() {
    try {
      const res = await request.get('/sysMessage/unreadCount');
      if (res.code === 200 && res.data !== undefined) {
        this.setData({ unreadCount: res.data });
      }
    } catch (error) {
    }
  },

  async loadStats() {
    try {
      const res = await request.get('/enterprise/citizen/dashboard');
      if (res.code === 200 && res.data) {
        this.setData({
          stats: {
            totalReport: res.data.totalReport || 0,
            pendingCount: res.data.pendingCount || 0,
            approvedCount: res.data.approvedCount || 0,
            rejectedCount: res.data.rejectedCount || 0
          }
        });
      }
    } catch (error) {
    }
  },

  loadUserInfo() {
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    const meta = enterpriseUtil.getQualificationMeta(userInfo.qualificationStatus);
    
    // 更新快捷操作中的申请按钮文案
    let applyName = '申请企业资质';
    if (userInfo.qualificationStatus === 0) applyName = '审核中';
    else if (userInfo.qualificationStatus === 2) applyName = '重新申请';

    const quickActions = this.data.quickActions.map(action => {
      if (action.id === 'apply') {
        return { ...action, name: applyName };
      }
      return action;
    });

    this.setData({
      userInfo,
      qualificationStatus: userInfo.qualificationStatus,
      statusLabel: meta.label,
      statusColor: meta.color,
      statusBg: meta.bg,
      quickActions
    });
  },

  onQuickAction(e) {
    const actionId = e.currentTarget.dataset.actionId;
    
    if (actionId === 'apply') {
      const { qualificationStatus } = this.data;
      if (qualificationStatus === 0) {
        wx.showToast({ title: '审核中，请耐心等待', icon: 'none' });
      } else {
        wx.navigateTo({ url: '/pages/enterprise/qualification/index' });
      }
    } else if (actionId === 'report') {
      wx.navigateTo({
        url: '/pages/citizen/report/index'
      });
    } else if (actionId === 'myReports') {
      wx.navigateTo({
        url: '/pages/citizen/report/list/index'
      });
    } else if (actionId === 'messages') {
      wx.navigateTo({
        url: '/pages/citizen/messages/index'
      });
    } else if (actionId === 'feedback') {
      wx.navigateTo({
        url: '/pages/citizen/feedback/submit'
      });
    } else if (actionId === 'about') {
      wx.showModal({
        title: '使用手册',
        content: '智驾廉盟是政务专属无人车综合管理服务小程序，市民朋友可以通过本小程序进行违规举报、申请企业资质，以及查看处理进度和消息通知。',
        showCancel: false
      });
    }
  },

  goToMyReports() {
    wx.navigateTo({
      url: '/pages/citizen/report/list/index'
    });
  },

  goToMyReportsByStatus(e) {
    const status = e.currentTarget.dataset.status;
    wx.navigateTo({
      url: `/pages/citizen/report/list/index?status=${status}`
    });
  },

  onGoToMessages() {
    wx.navigateTo({
      url: '/pages/citizen/messages/index'
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
