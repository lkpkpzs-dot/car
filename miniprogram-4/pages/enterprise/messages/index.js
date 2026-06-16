const request = require('../../../utils/request.js');
const { createCompatibleDate } = require('../../../utils/util.js');

Page({
  data: {
    messages: [],
    loading: false,
    hasUnread: false
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && !userInfo.authEnterpriseId) {
      wx.showModal({
        title: '提示',
        content: '请先完成企业资质认证',
        showCancel: false,
        success: () => {
          wx.switchTab({ url: '/pages/profile/index' });
        }
      });
      return;
    }
    this.loadMessages();
  },

  onShow() {
    // 每次显示页面时刷新消息
    this.loadMessages();
  },

  async loadMessages() {
    this.setData({ loading: true });
    
    try {
      const res = await request.get('/sysMessage/myMessages');
      if (res.code === 200) {
        const messages = (res.data || []).map(item => {
          // 格式化时间
          let time = item.createTime || '';
          if (time) {
            const date = createCompatibleDate(time);
            if (isNaN(date.getTime())) {
              time = '';
            } else {
              const now = new Date();
              const diff = now - date;
            
              // 今天
              if (diff < 24 * 60 * 60 * 1000 && date.getDate() === now.getDate()) {
                const hours = String(date.getHours()).padStart(2, '0');
                const minutes = String(date.getMinutes()).padStart(2, '0');
                time = `${hours}:${minutes}`;
              }
              // 昨天
              else if (diff < 48 * 60 * 60 * 1000) {
                time = '昨天';
              }
              // 本周内
              else if (diff < 7 * 24 * 60 * 60 * 1000) {
                const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
                time = weekdays[date.getDay()];
              }
              // 更早
              else {
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                time = `${month}-${day}`;
              }
            }
          }
          return { ...item, createTime: time };
        });
        
        const hasUnread = messages.some(item => item.isRead === 0);
        this.setData({ messages, hasUnread });
      }
    } catch (error) {
      console.error('加载消息失败:', error);
    } finally {
      this.setData({ loading: false });
    }
  },

  async onMessageTap(e) {
    const message = e.currentTarget.dataset.message;

    if (!message || !message.msgId) {
      wx.showToast({
        title: '消息不存在',
        icon: 'none'
      });
      return;
    }

    wx.navigateTo({
      url: `/pages/enterprise/messages/detail/index?id=${message.msgId}`
    });
  },

  async markAllRead() {
    const unreadMessages = this.data.messages.filter(item => item.isRead === 0);
    if (unreadMessages.length === 0) {
      wx.showToast({ title: '没有未读消息', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '提示',
      content: `确定要将 ${unreadMessages.length} 条未读消息标记为已读吗？`,
      success: async (res) => {
        if (res.confirm) {
          wx.showLoading({ title: '处理中...' });
          try {
            const res = await request.put('/sysMessage/markAllRead');
            if (res.code === 200) {
              // 更新本地数据，所有消息设为已读
              const messages = this.data.messages.map(item => ({
                ...item,
                isRead: 1
              }));
              this.setData({ messages, hasUnread: false });
              wx.showToast({ title: '标记成功', icon: 'success' });
            } else {
              wx.showToast({ title: res.msg || '操作失败', icon: 'none' });
            }
          } catch (error) {
            console.error('一键已读失败:', error);
            wx.showToast({ title: '操作失败', icon: 'none' });
          } finally {
            wx.hideLoading();
          }
        }
      }
    });
  }
});
