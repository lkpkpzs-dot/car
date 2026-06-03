const request = require('../../../../utils/request.js');

Page({
  data: {
    id: '',
    detail: null,
    images: [],
    loading: true,
    processStatusMap: {
      0: '待核实',
      1: '已处理',
      2: '无效举报'
    }
  },

  onLoad(options) {
    const id = options.id || options.msgId;

    if (!id) {
      wx.showToast({
        title: '消息参数错误',
        icon: 'none'
      });
      setTimeout(() => wx.navigateBack(), 1200);
      return;
    }

    this.setData({ id });
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
      const res = await request.get(`/sysMessage/detail/${this.data.id}`);

      if (res.code !== 200 || !res.data) {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
        this.setData({ detail: null });
        return;
      }

      const detail = res.data;
      const images = this.safeParseImages(detail.evidenceJson);

      this.setData({
        detail,
        images
      });

      if (detail.isRead === 0) {
        this.markRead();
      }
    } catch (error) {
      console.error('加载消息详情失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
      this.setData({ detail: null });
    } finally {
      this.setData({ loading: false });
    }
  },

  async markRead() {
    try {
      const res = await request.put(`/sysMessage/markRead/${this.data.id}`);
      if (res.code === 200 && this.data.detail) {
        this.setData({
          'detail.isRead': 1
        });
      }
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  },

  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      urls: this.data.images,
      current: this.data.images[index]
    });
  }
});
