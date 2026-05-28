const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');

Page({
  data: {
    detail: null,
    statusLabel: '',
    statusColor: '#64748b',
    statusBg: '#f1f5f9'
  },

  onLoad(options) {
    this.id = options.id;
    this.fetchDetail();
  },

  enrichDetail(detail) {
    const meta = enterpriseUtil.getLicenseStatusMeta(detail.status);
    return {
      ...detail,
      applyTypeLabel: enterpriseUtil.getApplyTypeLabel(detail.applyType),
      statusLabel: detail.statusDesc || meta.label,
      statusColor: meta.color,
      statusBg: meta.bg,
      uploadList: enterpriseUtil.UPLOAD_FIELDS.map(f => ({
        ...f,
        url: detail[f.key] || ''
      })).filter(f => f.url)
    };
  },

  async fetchDetail() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await request.get('/licenseApplication/detail', { id: this.id });
      const data = res.data || res;
      wx.hideLoading();
      const detail = this.enrichDetail(data);
      this.setData({
        detail,
        statusLabel: detail.statusLabel,
        statusColor: detail.statusColor,
        statusBg: detail.statusBg
      });
    } catch (err) {
      wx.hideLoading();
      const detail = this.enrichDetail(enterpriseUtil.getMockLicenseDetail(this.id));
      this.setData({
        detail,
        statusLabel: detail.statusLabel,
        statusColor: detail.statusColor,
        statusBg: detail.statusBg
      });
    }
  },

  previewImage(e) {
    const { url } = e.currentTarget.dataset;
    if (url) wx.previewImage({ urls: [url] });
  }
});
