const safetyApi = require('../../../utils/safetyOfficer.js');

Page({
  data: {
    officerId: null,
    detail: null,
    form: {
      accidentDate: '',
      liabilityLevel: null,
      casualtyType: null,
      reason: ''
    },
    liabilityOptions: safetyApi.LIABILITY_OPTIONS,
    casualtyOptions: safetyApi.CASUALTY_OPTIONS,
    liabilityIndex: -1,
    casualtyIndex: -1,
    canOperate: false
  },

  onLoad(options) {
    this.setData({ officerId: options.officerId });
    this.fetchOfficerDetail();
  },

  async fetchOfficerDetail() {
    try {
      const res = await safetyApi.getSafetyOfficerDetail(this.data.officerId);
      const detail = safetyApi.enrichOfficer(safetyApi.parseData(res, {}));
      const canOperate = detail.status === 1;
      this.setData({ detail, canOperate });
      
      if (!canOperate) {
        wx.showModal({
          title: '无法操作',
          content: '该安全员状态无效，无法录入事故处分',
          showCancel: false,
          success: () => wx.navigateBack()
        });
      }
    } catch (err) {
      console.error('Fetch officer detail failed:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1000);
    }
  },

  onDateChange(e) {
    this.setData({ 'form.accidentDate': e.detail.value });
  },

  onLiabilityChange(e) {
    const index = parseInt(e.detail.value, 10);
    const option = this.data.liabilityOptions[index];
    this.setData({
      liabilityIndex: index,
      'form.liabilityLevel': option.value
    });
  },

  onCasualtyChange(e) {
    const index = parseInt(e.detail.value, 10);
    const option = this.data.casualtyOptions[index];
    this.setData({
      casualtyIndex: index,
      'form.casualtyType': option.value
    });
  },

  onReasonInput(e) {
    this.setData({ 'form.reason': e.detail.value });
  },

  validate() {
    const { form } = this.data;
    if (!form.accidentDate) {
      wx.showToast({ title: '请选择事故日期', icon: 'none' });
      return false;
    }
    if (!form.liabilityLevel) {
      wx.showToast({ title: '请选择责任等级', icon: 'none' });
      return false;
    }
    if (form.casualtyType === null || form.casualtyType === undefined) {
      wx.showToast({ title: '请选择伤亡情况', icon: 'none' });
      return false;
    }
    if (!form.reason.trim()) {
      wx.showToast({ title: '请输入事故说明', icon: 'none' });
      return false;
    }
    return true;
  },

  async onSubmit() {
    if (!this.data.canOperate) {
      wx.showToast({ title: '该安全员状态无效，无法操作', icon: 'none' });
      return;
    }
    if (!this.validate()) return;
    wx.showLoading({ title: '提交中...' });
    try {
      const res = await safetyApi.handleSafetyOfficerAccident({
        officerId: Number(this.data.officerId),
        ...this.data.form,
        reason: this.data.form.reason.trim()
      });
      if (res.code === 200) {
        wx.showToast({ title: '处理成功', icon: 'success' });
        setTimeout(() => wx.navigateBack(), 1200);
      } else {
        wx.showToast({ title: res.msg || '操作失败', icon: 'none' });
      }
    } catch (err) {
      console.error('Handle safety officer accident failed:', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
