const auth = require('../../utils/auth');
const enterpriseUtil = require('../../utils/enterprise.js');
const request = require('../../utils/request.js');

Page({
  data: {
    userInfo: {},
    role: '',
    roleName: '',
    roleColor: '',
    roleClass: '',
    isEnterprise: false,
    qualificationStatus: enterpriseUtil.QUALIFICATION_STATUS.NONE,
    statusLabel: '未申请',
    statusColor: '#64748b',
    statusBg: '#f1f5f9',
    qualBtnText: '申请企业资质',
    qualBtnMode: 'apply',
    avatarUrl: '/assets/images/user-icon.png', // 默认头像
    showAvatarGuide: false // 显示头像引导
  },

  onLoad() {
    const hasChosenAvatar = wx.getStorageSync('hasChosenAvatar');
    if (!hasChosenAvatar) {
      this.setData({ showAvatarGuide: true });
    }
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail;
    this.setData({
      avatarUrl: avatarUrl,
      showAvatarGuide: false
    });
    // 保存用户已选择过头像的状态
    wx.setStorageSync('hasChosenAvatar', true);
    wx.showToast({
      title: '头像设置成功',
      icon: 'success',
      duration: 1500
    });
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().updateTabList(); // 确保 tabBar 重新计算首页路径
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

    // 根据角色显示不同的名称和颜色
    let roleClass = 'role-citizen'; // 默认市民端
    if (role === 'admin') {
      roleName = '管理员用户';
      roleColor = '#3b82f6'; // 蓝色
      roleClass = 'role-admin';
    } else if (isEnterpriseCertified) {
      roleName = '企业用户';
      roleColor = '#2f855a'; // 企业绿
      roleClass = 'role-enterprise';
    }

    this.setData({
      userInfo,
      role,
      roleName,
      roleColor,
      roleClass,
      isEnterprise: isEnterpriseCertified, // 用于控制显示企业信息块
      qualificationStatus: userInfo.qualificationStatus,
      statusLabel: meta.label,
      statusColor: meta.color,
      statusBg: meta.bg,
      qualBtnText,
      qualBtnMode,
      showQualBtn
    });

    // 根据角色动态设置导航栏颜色
    let navBarColor = '#1a365d'; // 默认蓝色
    if (role === 'admin') {
      navBarColor = '#1e3a8a'; // 民警端深蓝色
    } else if (isEnterpriseCertified) {
      navBarColor = '#16606b'; // 企业端青绿色
    } else {
      navBarColor = '#ea580c'; // 市民端橙色
    }

    wx.setNavigationBarColor({
      frontColor: '#ffffff',
      backgroundColor: navBarColor,
      animation: {
        duration: 400,
        timingFunc: 'easeInOut'
      }
    });

    // 如果是已认证企业且不是民警端，获取企业详细信息
    if (isEnterpriseCertified && userInfo.authEnterpriseId && role !== 'admin') {
      this.loadEnterpriseInfo(userInfo.authEnterpriseId);
    }
  },

  async loadEnterpriseInfo(enterpriseId) {
    try {
      const res = await request.get(`/enterpriseInfo/${enterpriseId}`);
      if (res.code === 200 && res.data) {
        const enterpriseInfo = res.data;
        // 更新用户信息中的企业数据
        const userInfo = {
          ...this.data.userInfo,
          enterpriseName: enterpriseInfo.enterpriseName,
          creditCode: enterpriseInfo.creditCode,
          legalPerson: enterpriseInfo.legalPerson,
          contactPhone: enterpriseInfo.contactPhone
        };
        this.setData({ userInfo });
        // 保存到本地缓存
        wx.setStorageSync('userInfo', userInfo);
      }
    } catch (error) {
      console.error('获取企业信息失败:', error);
    }
  },

  onQualificationAction() {
    const { qualBtnMode } = this.data;
    const url = qualBtnMode === 'view'
      ? '/pages/enterprise/qualification/index?mode=view'
      : '/pages/enterprise/qualification/index';
    wx.navigateTo({ url });
  },



  onGoToSettings() {
    wx.navigateTo({ url: '/pages/account-settings/index' });
  }
});
