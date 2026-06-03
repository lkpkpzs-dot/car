const request = require('../../../utils/request.js');

Page({
  data: {
    messages: [],
    loading: false
  },

  onLoad() {
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
            const date = new Date(time);
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
          return { ...item, createTime: time };
        });
        
        this.setData({ messages });
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
      url: `/pages/citizen/messages/detail/index?id=${message.msgId}`
    });
  }
});
