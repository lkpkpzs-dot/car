const safetyApi = require('../../../utils/safetyOfficer.js');

Page({
  data: {
    statusTabs: [
      { label: '全部', value: '' },
      { label: '待审核', value: 0 },
      { label: '有效', value: 1 },
      { label: '驳回', value: 2 },
      { label: '暂停', value: 3 },
      { label: '取消', value: 4 }
    ],
    currentStatus: '',
    enterpriseId: '',
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

  onTabChange(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({ currentStatus: value === '' || value === undefined ? '' : Number(value) }, () => this.fetchList());
  },

  onEnterpriseInput(e) {
    this.setData({ enterpriseId: e.detail.value });
  },

  onSearch() {
    this.fetchList();
  },

  onClearEnterprise() {
    this.setData({ enterpriseId: '' }, () => this.fetchList());
  },

  buildParams() {
    const params = {};
    if (this.data.currentStatus !== '') params.status = this.data.currentStatus;
    if (this.data.enterpriseId) params.enterpriseId = this.data.enterpriseId;
    return params;
  },

  async fetchList() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await safetyApi.getSafetyOfficerList(this.buildParams());
      const list = safetyApi.parseList(res).map(safetyApi.enrichOfficer);
      this.setData({ list });
    } catch (err) {
      console.error('Fetch admin safety officer list failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/admin/safety/detail?id=${id}` });
  }
});
