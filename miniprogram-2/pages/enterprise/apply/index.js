const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');

Page({
  data: {
    vin: '',
    applyType: 1,
    applyTypeLabel: '道路测试',
    applyTypeOptions: enterpriseUtil.APPLY_TYPE_OPTIONS,
    uploads: {
      certificateImg: '',
      idCardImg: '',
      testReportImg: '',
      insuranceImg: '',
      authorizationImg: '',
      applicationFormImg: ''
    },
    uploadFields: enterpriseUtil.UPLOAD_FIELDS
  },

  onLoad() {
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    const check = enterpriseUtil.checkLicenseApplyPermission(userInfo);
    if (!check.allowed) {
      wx.showModal({
        title: '无法申请',
        content: check.message,
        showCancel: false,
        success: () => wx.navigateBack()
      });
    }
  },

  onInput(e) {
    this.setData({ vin: e.detail.value });
  },

  onTypeChange(e) {
    const idx = parseInt(e.detail.value, 10);
    const opt = this.data.applyTypeOptions[idx];
    this.setData({
      applyType: opt.value,
      applyTypeLabel: opt.label
    });
  },

  onUploadChange(e) {
    const key = e.currentTarget.dataset.key;
    this.setData({ [`uploads.${key}`]: e.detail.value });
  },

  validate() {
    const { vin, uploads, uploadFields } = this.data;
    if (!vin || vin.length < 11) {
      wx.showToast({ title: '请输入正确VIN码', icon: 'none' });
      return false;
    }
    const missing = uploadFields.find(f => !uploads[f.key]);
    if (missing) {
      wx.showToast({ title: `请上传${missing.label}`, icon: 'none' });
      return false;
    }
    return true;
  },

  async onSubmit() {
    if (!this.validate()) return;

    const { vin, applyType, uploads } = this.data;
    wx.showLoading({ title: '提交中...' });

    try {
      const res = await request.post('/licenseApplication/apply', {
        vin,
        applyType,
        ...uploads
      });
      wx.hideLoading();

      if (res.code !== undefined && res.code !== 200) {
        wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
        return;
      }

      wx.showModal({
        title: '提交成功',
        content: '上牌申请已提交，请在「我的申请列表」查看进度。',
        showCancel: false,
        success: () => {
          wx.redirectTo({ url: '/pages/enterprise/apply/list' });
        }
      });
    } catch (err) {
      wx.hideLoading();
      console.error('License apply failed:', err);
      wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
    }
  }
});
