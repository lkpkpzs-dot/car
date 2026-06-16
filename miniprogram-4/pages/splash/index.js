const auth = require('../../utils/auth');

Page({
  data: {
    loadingProgress: 0,
    hasAgreedPrivacy: false,
    privacyChecked: false,
    canEnter: false,
    entering: false
  },

  async onLoad() {
    console.log('[Splash] 炫酷启动页加载中...');
    wx.hideHomeButton();

    const hasAgreedPrivacy = wx.getStorageSync('hasAgreedPrivacy') === true;
    this.setData({
      hasAgreedPrivacy,
      privacyChecked: false,
      canEnter: hasAgreedPrivacy
    });

    // 先展示完整的加载动画
    await this.startLoadingAnimation();

    if (hasAgreedPrivacy) {
      await this.performLogin();
    } else {
      this.setData({ canEnter: true });
    }
  },

  // 加载动画（确保完整展示 2.5 秒）
  startLoadingAnimation() {
    return new Promise((resolve) => {
      let progress = 0;
      const interval = 30;
      
      const timer = setInterval(() => {
        const easeOutCubic = (t) => 1 - Math.pow(1 - t, 3);
        const targetProgress = Math.min(progress + 2, 100);
        const normalizedProgress = targetProgress / 100;
        const displayProgress = Math.floor(easeOutCubic(normalizedProgress) * 100);
        
        this.setData({
          loadingProgress: displayProgress
        });
        
        progress = targetProgress;
        
        if (progress >= 100) {
          clearInterval(timer);
          setTimeout(resolve, 500); // 多停留一会儿让用户欣赏
        }
      }, interval);
    });
  },

  // 登录逻辑
  async performLogin() {
    if (this.data.entering) return;
    this.setData({ entering: true });

    try {
      let loginResult = null;
      
      console.log('[Splash] 执行静默登录获取最新角色信息...');
      loginResult = await auth.login();

      if (loginResult) {
        console.log('[Splash] 登录成功，角色为:', loginResult.roleType);
        auth.navigateByRole(loginResult.roleType);
      }

    } catch (err) {
      console.error('[Splash] 登录失败:', err);
      this.setData({ entering: false });
      wx.showModal({
        title: '开启服务失败',
        content: '未能连接到政务管理系统，请检查网络或联系管理员',
        confirmText: '重新连接',
        showCancel: false,
        success: (res) => {
          if (res.confirm) {
            this.onLoad();
          }
        }
      });
    }
  },

  onPrivacyChange(e) {
    const checked = e.detail.value.includes('agree');
    this.setData({ privacyChecked: checked });
  },

  onAgreementTap(e) {
    const type = e.currentTarget.dataset.type;
    wx.navigateTo({
      url: `/pages/agreement/index?type=${type}`
    });
  },

  async onEnterTap() {
    if (!this.data.canEnter || this.data.entering) return;

    if (!this.data.privacyChecked) {
      wx.showToast({
        title: '请先阅读并勾选同意协议',
        icon: 'none'
      });
      return;
    }

    wx.setStorageSync('hasAgreedPrivacy', true);
    this.setData({
      hasAgreedPrivacy: true,
      canEnter: true
    });

    await this.performLogin();
  }
});
