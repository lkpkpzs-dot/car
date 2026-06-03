const app = getApp();
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
      { id: 'report', name: '违规举报', icon: 'report', desc: '举报违规行为' },
      { id: 'apply', name: '申请企业资质', icon: 'archive', desc: '提交企业认证申请' },
      { id: 'about', name: '关于我们', icon: 'about', desc: '了解详情' }
    ],
    tips: [
      { id: 1, title: '如何举报违规行为', desc: '点击"违规举报"，选择举报类型，上传证据图片' },
      { id: 2, title: '处理进度查询', desc: '提交后可在"查询进度"中查看处理状态' }
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
    } else if (actionId === 'query') {
      wx.showToast({
        title: '暂无待处理事项',
        icon: 'none'
      });
    } else if (actionId === 'about') {
      wx.showModal({
        title: '关于智车通',
        content: '智车通是政务专属无人车综合管理服务小程序，旨在提供便捷高效的政务服务体验。',
        showCancel: false
      });
    }
  },

  onGoToMessages() {
    wx.navigateTo({
      url: '/pages/citizen/messages/index'
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
