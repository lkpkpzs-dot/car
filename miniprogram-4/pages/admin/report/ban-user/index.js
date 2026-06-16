const request = require('../../../../utils/request.js');

Page({
  data: {
    user: null,
    loading: false,
    selectedHours: 24,
    selectedReason: '恶意举报',
    reasons: ['恶意举报', '虚假信息', '违规操作', '其他'],
    hourOptions: [
      { label: '1小时', value: 1 },
      { label: '24小时', value: 24 },
      { label: '3天', value: 72 },
      { label: '7天', value: 168 },
      { label: '自定义', value: 'custom' }
    ],
    customHours: '',
    showCustomInput: false
  },

  onLoad(options) {
    try {
      const user = JSON.parse(decodeURIComponent(options.user));
      // 准备用户头像显示文字
      const avatarText = this.getAvatarText(user);
      this.setData({ 
        user,
        avatarText
      });
    } catch (e) {
      wx.showToast({ title: '数据加载失败', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
    }
  },

  getAvatarText(user) {
    if (user.realName && user.realName.length > 0) {
      return user.realName.charAt(0);
    }
    return '用';
  },

  onSelectHours(e) {
    const value = e.currentTarget.dataset.value;
    if (value === 'custom') {
      this.setData({ 
        showCustomInput: true,
        selectedHours: 24,
        customHours: ''
      });
    } else {
      this.setData({ 
        showCustomInput: false,
        selectedHours: value
      });
    }
  },

  onCustomInput(e) {
    const value = parseInt(e.detail.value);
    if (!isNaN(value) && value > 0) {
      this.setData({ selectedHours: value });
    }
  },

  onSelectReason(e) {
    const reason = e.currentTarget.dataset.reason;
    this.setData({ selectedReason: reason });
  },

  async onConfirm() {
    const { user, selectedHours, selectedReason } = this.data;
    
    wx.showModal({
      title: '确认封禁',
      content: '确定要封禁用户 ' + (user.realName || '用户' + user.userId) + ' ' + selectedHours + ' 小时吗？',
      confirmText: '确认',
      confirmColor: '#dc2626',
      success: (res) => {
        if (res.confirm) {
          this.doBan(user.userId, selectedReason, selectedHours);
        }
      }
    });
  },

  async doBan(userId, reason, hours) {
    this.setData({ loading: true });
    wx.showLoading({ title: '处理中...' });
    
    try {
      const url = '/citizenReport/admin/ban-user?userId=' + userId + '&reason=' + encodeURIComponent(reason) + '&banHours=' + hours;
      const res = await request.post(url, {});
      
      wx.hideLoading();
      
      if (res.code === 200) {
        wx.showModal({
          title: '封禁成功',
          content: '用户已封禁，系统已发送通知用户',
          showCancel: false,
          confirmText: '确定',
          success: () => {
            wx.navigateBack();
          }
        });
      } else {
        wx.showToast({
          title: res.msg || '封禁失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('封禁用户失败:', error);
      wx.showToast({
        title: '封禁失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  }
});
