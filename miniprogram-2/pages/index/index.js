const app = getApp();
const auth = require('../../utils/auth');

Page({
  data: {
    showLogo: false,
    showLoading: false
  },

  onLoad() {
    this.startAnimation();
    this.performSilentLogin();
  },

  startAnimation() {
    // 延迟一小会儿开始 Logo 渐显
    setTimeout(() => {
      this.setData({ showLogo: true });
    }, 200);

    // 1秒后显示加载文字和动画
    setTimeout(() => {
      this.setData({ showLoading: true });
    }, 1000);
  },

  async performSilentLogin() {
    // 保证动画至少持续 1.5 秒
    const startTime = Date.now();
    
    try {
      const data = await auth.login();
      const endTime = Date.now();
      const elapsed = endTime - startTime;
      
      // 如果登录太快，补足动画时间
      const minDuration = 1500;
      if (elapsed < minDuration) {
        await new Promise(resolve => setTimeout(resolve, minDuration - elapsed));
      }

      console.log('登录成功，角色为:', data.roleType);
      auth.navigateByRole(data.roleType);
    } catch (err) {
      console.error('Silent login failed:', err);
      
      // 如果是后端服务没开，提供重试选项
      wx.showModal({
        title: '开启服务失败',
        content: '未能连接到政务管理系统，请检查网络或联系管理员',
        confirmText: '重新连接',
        showCancel: false,
        success: (res) => {
          if (res.confirm) {
            this.performSilentLogin();
          }
        }
      });
    }
  }
});
