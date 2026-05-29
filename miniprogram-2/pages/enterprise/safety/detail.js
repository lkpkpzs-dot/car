const safetyApi = require('../../../utils/safetyOfficer.js');

Page({
  data: {
    id: null,
    detail: null,
    penalties: []
  },

  onLoad(options) {
    this.setData({ id: options.id });
    this.fetchData();
  },

  onPullDownRefresh() {
    this.fetchData().then(() => wx.stopPullDownRefresh());
  },

  async fetchData() {
    if (!this.data.id) return;
    wx.showLoading({ title: '加载中...' });
    try {
      const [detailRes, penaltiesRes] = await Promise.all([
        safetyApi.getSafetyOfficerDetail(this.data.id),
        safetyApi.getSafetyOfficerPenalties(this.data.id)
      ]);
      const detail = safetyApi.enrichOfficer(safetyApi.parseData(detailRes, {}));
      const penalties = safetyApi.parseList(penaltiesRes).map(item => ({
        ...item,
        liabilityLabel: safetyApi.getLiabilityLabel(item.liabilityLevel),
        casualtyLabel: safetyApi.getCasualtyLabel(item.casualtyType)
      }));
      this.setData({ detail, penalties });
    } catch (err) {
      console.error('Fetch safety officer detail failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset;
    if (url) wx.previewImage({ urls: [url] });
  },

  goResubmit() {
    wx.navigateTo({ url: `/pages/enterprise/safety/form?officerId=${this.data.id}` });
  },

  onDelete() {
    if (this.data.detail.status !== 0 && this.data.detail.status !== 2) {
      wx.showToast({ title: '该安全员状态无法删除', icon: 'none' });
      return;
    }
    wx.showModal({
      title: '删除安全员',
      content: '确认删除该安全员记录吗？',
      success: async (modalRes) => {
        if (!modalRes.confirm) return;
        wx.showLoading({ title: '删除中...' });
        try {
          const res = await safetyApi.deleteSafetyOfficer(this.data.id);
          if (res.code === 200) {
            wx.showToast({ title: '删除成功', icon: 'success' });
            setTimeout(() => wx.navigateBack(), 1000);
          } else {
            wx.showToast({ title: res.msg || '操作失败', icon: 'none' });
          }
        } catch (err) {
          console.error('Delete safety officer failed:', err);
          wx.showToast({ title: '操作失败', icon: 'none' });
        } finally {
          wx.hideLoading();
        }
      }
    });
  }
});
