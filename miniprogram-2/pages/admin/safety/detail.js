const safetyApi = require('../../../utils/safetyOfficer.js');

Page({
  data: {
    id: null,
    detail: null,
    penalties: [],
    showRejectModal: false,
    rejectReason: ''
  },

  onLoad(options) {
    this.setData({ id: options.id });
    this.fetchData();
  },

  onShow() {
    if (this.data.id) this.fetchData();
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
      console.error('Fetch admin safety officer detail failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset;
    if (url) wx.previewImage({ urls: [url] });
  },

  async submitAudit(status, comment) {
    wx.showLoading({ title: '提交中...' });
    try {
      const res = await safetyApi.auditSafetyOfficer({
        officerId: Number(this.data.id),
        status,
        comment
      });
      if (res.code === 200) {
        wx.showToast({ title: '处理成功', icon: 'success' });
        this.setData({ showRejectModal: false, rejectReason: '' });
        setTimeout(() => this.fetchData(), 800);
      } else {
        wx.showToast({ title: res.msg || '操作失败', icon: 'none' });
      }
    } catch (err) {
      console.error('Audit safety officer failed:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  onApprove() {
    wx.showModal({
      title: '审核通过',
      content: '确认通过该安全员资质申请吗？',
      success: (res) => {
        if (res.confirm) this.submitAudit(1, '审核通过');
      }
    });
  },

  onReject() {
    this.setData({ showRejectModal: true, rejectReason: '' });
  },

  onRejectReasonInput(e) {
    this.setData({ rejectReason: e.detail.value });
  },

  hideRejectModal() {
    this.setData({ showRejectModal: false });
  },

  stopBubble() {},

  confirmReject() {
    const reason = this.data.rejectReason.trim();
    if (!reason) {
      wx.showToast({ title: '请填写驳回原因', icon: 'none' });
      return;
    }
    this.submitAudit(2, reason);
  },

  goPenalty() {
    if (this.data.detail.status !== 1) {
      wx.showToast({ title: '该安全员状态无效，无法操作', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/admin/safety/penalty?officerId=${this.data.id}` });
  }
});
