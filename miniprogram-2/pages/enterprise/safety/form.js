const safetyApi = require('../../../utils/safetyOfficer.js');

const defaultForm = {
  officerId: null,
  officerName: '',
  idCardNo: '',
  phone: '',
  age: '',
  licenseType: '',
  driverLicenseNo: '',
  firstLicenseDate: '',
  noFullScoreRecord: 1,
  noMajorAccidentRecord: 1,
  noDuiRecord: 1,
  noCrimeRecord: 1,
  healthy: 1,
  noAlcoholDrugRecord: 1,
  idCardUrl: '',
  driverLicenseUrl: '',
  healthCertificateUrl: '',
  noCrimeCertificateUrl: '',
  noViolationAccidentCertificateUrl: '',
  noAlcoholDrugCertificateUrl: ''
};

Page({
  data: {
    isResubmit: false,
    form: { ...defaultForm },
    licenseTypes: safetyApi.LICENSE_TYPES,
    licenseTypeIndex: -1,
    conditionFields: safetyApi.CONDITION_FIELDS,
    materialFields: safetyApi.MATERIAL_FIELDS
  },

  onLoad(options) {
    if (options.officerId) {
      this.setData({ isResubmit: true });
      this.fetchDetail(options.officerId);
    }
  },

  async fetchDetail(officerId) {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await safetyApi.getSafetyOfficerDetail(officerId);
      const detail = safetyApi.parseData(res, {});
      const form = { ...defaultForm, ...detail, officerId: detail.officerId || officerId };
      const licenseTypeIndex = this.data.licenseTypes.indexOf(form.licenseType);
      this.setData({ form, licenseTypeIndex });
    } catch (err) {
      console.error('Fetch safety officer detail failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  },

  onInput(e) {
    const { key } = e.currentTarget.dataset;
    this.setData({ [`form.${key}`]: e.detail.value });
  },

  onLicenseTypeChange(e) {
    const index = parseInt(e.detail.value, 10);
    this.setData({
      licenseTypeIndex: index,
      'form.licenseType': this.data.licenseTypes[index]
    });
  },

  onDateChange(e) {
    this.setData({ 'form.firstLicenseDate': e.detail.value });
  },

  onConditionChange(e) {
    const { key } = e.currentTarget.dataset;
    this.setData({ [`form.${key}`]: e.detail.value ? 1 : 0 });
  },

  onUploadChange(e) {
    const { key } = e.currentTarget.dataset;
    this.setData({ [`form.${key}`]: e.detail.value });
  },

  validate() {
    const { form, materialFields } = this.data;
    const age = Number(form.age);
    if (!form.officerName.trim()) {
      wx.showToast({ title: '姓名不能为空', icon: 'none' });
      return false;
    }
    if (!form.idCardNo.trim()) {
      wx.showToast({ title: '身份证号不能为空', icon: 'none' });
      return false;
    }
    if (!age || age < 21 || age > 50) {
      wx.showToast({ title: '年龄必须为21-50', icon: 'none' });
      return false;
    }
    if (!form.licenseType) {
      wx.showToast({ title: '驾驶证类型不能为空', icon: 'none' });
      return false;
    }
    if (!form.firstLicenseDate) {
      wx.showToast({ title: '初次领证日期不能为空', icon: 'none' });
      return false;
    }
    const missing = materialFields.find(item => !form[item.key]);
    if (missing) {
      wx.showToast({ title: `请上传${missing.label}`, icon: 'none' });
      return false;
    }
    return true;
  },

  async onSubmit() {
    if (!this.validate()) return;
    const officer = {
      ...this.data.form,
      age: Number(this.data.form.age)
    };

    wx.showLoading({ title: '提交中...' });
    try {
      const res = await safetyApi.applySafetyOfficer(officer);
      if (res.code === 200) {
        wx.showToast({ title: '提交成功', icon: 'success' });
        setTimeout(() => wx.redirectTo({ url: '/pages/enterprise/safety/list' }), 1200);
      } else {
        wx.showToast({ title: res.msg || '操作失败', icon: 'none' });
      }
    } catch (err) {
      console.error('Apply safety officer failed:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
