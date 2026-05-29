App({
  async onLaunch() {
    this.installLoadingGuard();

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

    // 自动登录
    await this.autoLogin();
  },

  // 自动登录
  async autoLogin() {
    const token = wx.getStorageSync('token');
    if (token) {
      console.log('[AutoLogin] Token 已存在，跳过登录');
      return;
    }

    try {
      console.log('[AutoLogin] 开始自动登录...');
      
      // 先调用 wx.login 获取 code
      const loginRes = await new Promise((resolve, reject) => {
        wx.login({
          success: resolve,
          fail: reject
        });
      });

      if (!loginRes.code) {
        throw new Error('wx.login 获取 code 失败');
      }

      // 调用后端登录接口
      const res = await this.requestWithoutToken({
        url: '/auth/login',
        method: 'POST',
        data: { code: loginRes.code }
      });

      if (res.code === 200 && res.data && res.data.token) {
        wx.setStorageSync('token', res.data.token);
        
        // 保存用户信息
        if (res.data.user) {
          wx.setStorageSync('userInfo', res.data.user);
        }
        
        // 保存角色
        if (res.data.roleType !== undefined || res.data.role_type !== undefined) {
          const roleType = res.data.roleType !== undefined ? res.data.roleType : res.data.role_type;
          const auth = require('./utils/auth');
          const role = auth.mapRoleType(roleType);
          auth.setRole(role);
          this.globalData.role = role;
        }
        
        console.log('[AutoLogin] 自动登录成功');
      } else {
        throw new Error(res.msg || '登录失败');
      }
    } catch (error) {
      console.error('[AutoLogin] 自动登录失败:', error);
      // 登录失败不影响使用，后续请求会触发 401 重试
    }
  },

  // 不带 token 的请求（用于登录时）
  requestWithoutToken(options) {
    return new Promise((resolve, reject) => {
      const API_BASE_URL = 'http://10.10.4.192:8080';
      wx.request({
        url: options.url.startsWith('http') ? options.url : API_BASE_URL + options.url,
        method: options.method || 'GET',
        data: options.data || {},
        timeout: options.timeout || 10000,
        header: {
          'Content-Type': 'application/json',
          ...options.header
        },
        success: (res) => {
          if (res.statusCode === 200) {
            resolve(res.data);
          } else {
            reject(res);
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },

  installLoadingGuard() {
    if (wx.__loadingGuardInstalled) return;
    wx.__loadingGuardInstalled = true;

    const rawShowLoading = wx.showLoading.bind(wx);
    const rawHideLoading = wx.hideLoading.bind(wx);
    let loadingTimer = null;

    wx.showLoading = (options = {}) => {
      if (loadingTimer) clearTimeout(loadingTimer);
      rawShowLoading(options);
      loadingTimer = setTimeout(() => {
        loadingTimer = null;
        rawHideLoading();
      }, options.timeout || 12000);
    };

    wx.hideLoading = (options = {}) => {
      if (loadingTimer) {
        clearTimeout(loadingTimer);
        loadingTimer = null;
      }
      rawHideLoading(options);
    };
  },

  globalData: {
    userInfo: null,
    role: null,
    baseUrl: 'http://localhost:8080'
  }
});
