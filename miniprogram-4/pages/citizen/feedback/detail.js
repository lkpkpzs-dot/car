const request = require('../../../utils/request.js');
const { createCompatibleDate } = require('../../../utils/util.js');

Page({
  data: {
    feedbackId: '',
    detail: null,
    images: [],
    loading: true
  },

  onLoad(options) {
    const id = options.feedbackId;
    if (!id) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    this.setData({ feedbackId: id });
    this.loadDetail();
  },

  safeParseImages(value) {
    if (!value) return [];
    if (Array.isArray(value)) return value.filter(Boolean);

    if (typeof value !== 'string') return [];

    const text = value.trim();
    if (!text) return [];

    try {
      const parsed = JSON.parse(text);
      if (Array.isArray(parsed)) return parsed.filter(Boolean);
      if (typeof parsed === 'string' && parsed) return [parsed];
      return [];
    } catch (error) {
      return text
        .split(',')
        .map(item => item.replace(/[\[\]"]/g, '').trim())
        .filter(Boolean);
    }
  },

  async loadDetail() {
    this.setData({ loading: true });
    
    try {
      const res = await request.get(`/feedback/detail?feedbackId=${this.data.feedbackId}`);
      
      if (res.code === 200 && res.data) {
        const detail = res.data;
        const images = this.safeParseImages(detail.images);
        
        // 处理后端返回的数字状态
        let status = detail.processStatus !== undefined ? detail.processStatus : detail.status;
        // 支持两种格式：数字或字符串
        if (typeof status === 'number') {
          if (status === 0) status = 'pending';
          else if (status === 1) status = 'processing';
          else if (status === 2) status = 'completed';
        }
        
        let createTime = detail.createTime || '';
        if (createTime) {
          const date = createCompatibleDate(createTime);
          if (!isNaN(date.getTime())) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hour = String(date.getHours()).padStart(2, '0');
            const minute = String(date.getMinutes()).padStart(2, '0');
            createTime = `${year}-${month}-${day} ${hour}:${minute}`;
          }
        }

        let handleTime = detail.handleTime || '';
        if (handleTime) {
          const date = createCompatibleDate(handleTime);
          if (!isNaN(date.getTime())) {
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hour = String(date.getHours()).padStart(2, '0');
            const minute = String(date.getMinutes()).padStart(2, '0');
            handleTime = `${year}-${month}-${day} ${hour}:${minute}`;
          }
        }

        this.setData({
          detail: { 
            ...detail, 
            feedbackId: detail.feedbackId,
            type: detail.feedbackType || detail.type,
            status,
            createTime, 
            handleTime,
            // 支持两种备注字段名
            handleRemark: detail.processRemark || detail.handleRemark 
          },
          images
        });
      } else {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('加载详情失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      urls: this.data.images,
      current: this.data.images[index]
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
