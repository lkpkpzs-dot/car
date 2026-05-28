App({
  onLaunch() {
    const logs = wx.getStorageSync('logs') || [];
    logs.unshift(Date.now());
    wx.setStorageSync('logs', logs);

    const savedRole = wx.getStorageSync('role');
    if (savedRole) {
      this.globalData.role = savedRole;
    }

    if (!wx.cloud) {
      console.error('请使用 2.2.3 或以上的基础库以使用云能力');
    } else {
      wx.cloud.init({
        env: 'example',
        traceUser: true,
      });
    }
  },

  globalData: {
    userInfo: null,
    role: null,
    baseUrl: 'http://localhost:8080'
  }
});
