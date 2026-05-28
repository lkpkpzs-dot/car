const app = getApp();
const enterpriseUtil = require('../../utils/enterprise.js');

Page({
  data: {
    role: 'citizen',
    roleName: '市民监督端',
    userInfo: {},
    qualificationStatus: -1,
    statusLabel: '未申请',
    statusColor: '#64748b',
    statusBg: '#f1f5f9',
    stats: {
      suggestions: 156,
      reports: 42,
      processed: 185,
      pending: 13
    },
    quickActions: [
      { id: 'suggest', name: '意见建议', icon: 'suggest', desc: '提交优化建议' },
      { id: 'report', name: '违规举报', icon: 'report', desc: '举报违规行为' },
      { id: 'apply', name: '申请企业资质', icon: 'archive', desc: '提交企业认证申请' },
      { id: 'about', name: '关于我们', icon: 'about', desc: '了解详情' }
    ],
    tips: [
      { id: 1, title: '如何举报违规行为', desc: '点击"违规举报"，选择举报类型，上传证据图片' },
      { id: 2, title: '意见建议提交', desc: '欢迎对无人车通行、路线规划等提出宝贵建议' },
      { id: 3, title: '处理进度查询', desc: '提交后可在"查询进度"中查看处理状态' }
    ]
  },

  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({
        selected: 0
      });
      this.getTabBar().updateTabList();
    }
    this.loadUserInfo();
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
    } else if (actionId === 'suggest') {
      wx.showModal({
        title: '意见建议',
        content: '请输入您的建议：',
        editable: true,
        placeholderText: '请输入您对无人车通行、路线规划等方面的建议...',
        success: (res) => {
          if (res.confirm && res.content && res.content.trim()) {
            wx.showToast({
              title: '建议已提交',
              icon: 'success'
            });
          }
        }
      });
    } else if (actionId === 'report') {
      wx.showActionSheet({
        itemList: ['违规占道', '乱停乱放', '违规行驶', '违规运营', '其他'],
        success: (res) => {
          const types = ['违规占道', '乱停乱放', '违规行驶', '违规运营', '其他'];
          wx.showToast({
            title: `已举报：${types[res.tapIndex]}`,
            icon: 'success'
          });
        }
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

  onLogout() {
    app.globalData.role = null;
    wx.removeStorageSync('role');
    wx.reLaunch({
      url: '/pages/index/index'
    });
  }
});
