const request = require('../../utils/request.js');
const enterpriseUtil = require('../../utils/enterprise.js');

Page({
  data: {
    realName: '',
    phone: '',
    loading: false
  },

  onLoad() {
    this.loadUserInfo();
  },

  loadUserInfo() {
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    this.setData({
      realName: userInfo.realName || userInfo.real_name || '',
      phone: userInfo.phone || ''
    });
  },

  onRealNameInput(e) {
    this.setData({ realName: e.detail.value });
  },

  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },

  async onSave() {
    const { realName, phone } = this.data;

    if (!realName || !phone) {
      wx.showToast({
        title: '请填写完整信息',
        icon: 'none'
      });
      return;
    }

    try {
      this.setData({ loading: true });
      wx.showLoading({ title: '保存中...' });

      const res = await request.post('/sysUser/updateProfile', {
        phone: phone,
        realName: realName
      });

      if (res.code === 200 && res.data) {
        // 更新本地缓存中的用户信息
        const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
        userInfo.realName = realName;
        userInfo.phone = phone;
        wx.setStorageSync('userInfo', userInfo);

        wx.showToast({
          title: '保存成功',
          icon: 'success'
        });

        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: res.msg || '保存失败',
          icon: 'none'
        });
      }
    } catch (err) {
      console.error('保存失败:', err);
      wx.showToast({
        title: '保存失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
      wx.hideLoading();
    }
  }
});
