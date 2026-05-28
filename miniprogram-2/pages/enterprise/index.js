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
      { id: 'licenseApply', name: '发起上牌申请', icon: 'apply', desc: '在线提交号牌申请' },
      { id: 'roadApply', name: '道路许可申请', icon: 'monitor', desc: '道路测试/示范应用/应用试点' },
      { id: 'applyList', name: '我的申请列表', icon: 'records', desc: '查看上牌申请进度' }
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

    await this.loadStats();
  },

  async loadStats() {
    try {
      const res = await request.get('/licenseApplication/myList');
      const list = request.parseListData(res);
      const reviewingCount = list.filter(item =>
        item.status === enterpriseUtil.LICENSE_STATUS.REVIEWING
        || item.status === enterpriseUtil.LICENSE_STATUS.INSPECTING
      ).length;
      this.setData({
        stats: {
          vehicleCount: list.filter(item => item.status === enterpriseUtil.LICENSE_STATUS.ISSUED).length,
          applyCount: list.length,
          reviewingCount
        }
      });
    } catch (err) {
      const mockList = enterpriseUtil.getMockLicenseList();
      this.setData({
        stats: {
          vehicleCount: 1,
          applyCount: mockList.length,
          reviewingCount: mockList.filter(i => i.status === 1).length
        }
      });
    }
  },

  onQuickAction(e) {
    const { id } = e.currentTarget.dataset;
    const { userInfo } = this.data;

    if (id === 'licenseApply') {
      const check = enterpriseUtil.checkLicenseApplyPermission(userInfo);
      if (!check.allowed) {
        wx.showModal({
          title: '无法申请',
          content: check.message,
          confirmText: '去认证',
          success: (res) => {
            if (res.confirm) {
              wx.navigateTo({ url: '/pages/enterprise/qualification/index' });
            }
          }
        });
        return;
      }
      wx.navigateTo({ url: '/pages/enterprise/apply/index' });
      return;
    }

    if (id === 'applyList') {
      wx.navigateTo({ url: '/pages/enterprise/apply/list' });
      return;
    }

    if (id === 'roadApply') {
      wx.navigateTo({ url: '/pages/road/list/index' });
      return;
    }

    if (id === 'qualification') {
      this.goQualification();
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
