const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');

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

  enrichList(list) {
    return (list || []).map(item => {
      const meta = enterpriseUtil.getLicenseStatusMeta(item.status);
      return {
        ...item,
        applyTypeLabel: enterpriseUtil.getApplyTypeLabel(item.applyType),
        statusLabel: item.statusDesc || meta.label,
        statusColor: meta.color,
        statusBg: meta.bg
      };
    });
  },

  async fetchList() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await request.get('/licenseApplication/myList');
      const list = this.enrichList(request.parseListData(res));
      wx.hideLoading();
      this.setData({ list });
    } catch (err) {
      wx.hideLoading();
      this.setData({ list: this.enrichList(enterpriseUtil.getMockLicenseList()) });
    }
  },

  goDetail(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/enterprise/apply/detail?id=${id}` });
  }
});
