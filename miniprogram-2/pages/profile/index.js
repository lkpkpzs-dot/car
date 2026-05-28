const auth = require('../../utils/auth');
const enterpriseUtil = require('../../utils/enterprise.js');

Page({
  data: {
    userInfo: {},
    role: '',
    roleName: '',
    roleColor: '',
    isEnterprise: false,
    qualificationStatus: enterpriseUtil.QUALIFICATION_STATUS.NONE,
    statusLabel: '未申请',
    statusColor: '#64748b',
    statusBg: '#f1f5f9',
    qualBtnText: '申请企业资质',
    qualBtnMode: 'apply'
  },

  onLoad() {
    this.loadUserInfo();
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().setData({ selected: 1 });
    }
    this.loadUserInfo();
  },

  loadUserInfo() {
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    const role = auth.getRole();
    
    // 关键逻辑：必须有 authEnterpriseId 且 qualificationStatus 为 1 才是已认证企业
    const isEnterpriseCertified = !!userInfo.authEnterpriseId && userInfo.qualificationStatus === enterpriseUtil.QUALIFICATION_STATUS.APPROVED;
    
    const meta = enterpriseUtil.getQualificationMeta(userInfo.qualificationStatus);

    let qualBtnText = '申请企业资质';
    let qualBtnMode = 'apply';
    let showQualBtn = true;
    
    // 根据资质状态显示不同按钮
    if (userInfo.qualificationStatus === enterpriseUtil.QUALIFICATION_STATUS.PENDING) {
      // 审核中 (0)
      qualBtnText = '查看审核进度';
      qualBtnMode = 'view';
    } else if (userInfo.qualificationStatus === enterpriseUtil.QUALIFICATION_STATUS.REJECTED) {
      // 已驳回 (2)
      qualBtnText = '重新申请';
      qualBtnMode = 'reapply';
    } else if (userInfo.qualificationStatus === enterpriseUtil.QUALIFICATION_STATUS.APPROVED) {
      // 已通过 (1)
      showQualBtn = false; // 已通过后隐藏操作按钮，仅保留状态展示
    } else {
      // 未申请 (-1 或其他)
      qualBtnText = '申请企业资质';
      qualBtnMode = 'apply';
    }

    let roleName = auth.getRoleName(role);
    let roleColor = auth.getRoleColor(role);

    // 如果是已认证的企业，角色显示改为“企业用户”
    if (isEnterpriseCertified) {
      roleName = '企业用户';
      roleColor = '#2f855a'; // 企业绿
    }

    this.setData({
      userInfo,
      role,
      roleName,
      roleColor,
      isEnterprise: isEnterpriseCertified, // 用于控制显示企业信息块
      qualificationStatus: userInfo.qualificationStatus,
      statusLabel: meta.label,
      statusColor: meta.color,
      statusBg: meta.bg,
      qualBtnText,
      qualBtnMode,
      showQualBtn
    });
  },

  onQualificationAction() {
    const { qualBtnMode } = this.data;
    const url = qualBtnMode === 'view'
      ? '/pages/enterprise/qualification/index?mode=view'
      : '/pages/enterprise/qualification/index';
    wx.navigateTo({ url });
  },

  previewLicense() {
    const { licenseImg } = this.data.userInfo;
    if (licenseImg) wx.previewImage({ urls: [licenseImg] });
  },

  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          auth.clearRole();
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');
          wx.reLaunch({ url: '/pages/index/index' });
        }
      }
    });
  }
});
