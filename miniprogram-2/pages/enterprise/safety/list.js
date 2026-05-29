const safetyApi = require('../../../utils/safetyOfficer.js');

Page({
  data: {
    list: []
  },

  onLoad() {
    this.fetchList();
  },

  onShow() {
    this.fetchList();
  },

  onPullDownRefresh() {
    this.fetchList().then(() => wx.stopPullDownRefresh());
  },

  async fetchList() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await safetyApi.getMySafetyOfficers();
      const list = safetyApi.parseList(res).map(safetyApi.enrichOfficer);
      this.setData({ list });
    } catch (err) {
      console.error('Fetch safety officer list failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  goForm() {
    wx.navigateTo({ url: '/pages/enterprise/safety/form' });
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/enterprise/safety/detail?id=${id}` });
  }
});
