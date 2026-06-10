const request = require('../../../utils/request.js');
const { createCompatibleDate } = require('../../../utils/util.js');

Page({
  data: {
    list: [],
    loading: false,
    empty: false
  },

  onLoad() {
    this.loadList();
  },

  onShow() {
    this.loadList();
  },

  async loadList() {
    this.setData({ loading: true, empty: false });
    
    try {
      const res = await request.get('/feedback/myList');
      if (res.code === 200) {
        const list = (res.data || []).map(item => {
          // 处理后端返回的数字状态
          let status = item.processStatus !== undefined ? item.processStatus : item.status;
          // 支持两种格式：数字或字符串
          if (typeof status === 'number') {
            if (status === 0) status = 'pending';
            else if (status === 1) status = 'processing';
            else if (status === 2) status = 'completed';
          }
          
          let time = item.createTime || '';
          if (time) {
            const date = createCompatibleDate(time);
            if (!isNaN(date.getTime())) {
              const year = date.getFullYear();
              const month = String(date.getMonth() + 1).padStart(2, '0');
              const day = String(date.getDate()).padStart(2, '0');
              time = `${year}-${month}-${day}`;
            }
          }
          return { 
            ...item, 
            feedbackId: item.feedbackId,
            type: item.feedbackType || item.type,
            status, 
            createTime: time 
          };
        });
        
        this.setData({ 
          list,
          empty: list.length === 0
        });
      }
    } catch (error) {
      console.error('加载列表失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  onGoToDetail(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/citizen/feedback/detail?feedbackId=${id}`
    });
  },

  getStatusConfig(status) {
    const configs = {
      'pending': { text: '待处理', color: '#9ca3af', bgColor: '#f3f4f6' },
      'processing': { text: '处理中', color: '#f59e0b', bgColor: '#fffbeb' },
      'completed': { text: '已处理', color: '#10b981', bgColor: '#ecfdf5' }
    };
    return configs[status] || configs['pending'];
  },

  getTypeText(type) {
    const types = {
      1: '功能建议',
      2: 'Bug反馈',
      3: '其他',
      'suggestion': '功能建议',
      'bug': 'Bug反馈',
      'other': '其他'
    };
    return types[type] || type;
  }
});
