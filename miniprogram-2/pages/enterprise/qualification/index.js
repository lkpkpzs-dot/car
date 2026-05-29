const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');

Page({
  data: {
    mode: 'edit',
    readonly: false,
    enterpriseName: '',
    creditCode: '',
    legalPerson: '',
    contactPhone: '',
    licenseImg: '',
    qualificationStatus: enterpriseUtil.QUALIFICATION_STATUS.NONE,
    btnText: '提交申请'
  },

  onLoad(options) {
    const mode = options.mode || 'edit';
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    const status = userInfo.qualificationStatus;

    // 审核中(0) 或 已通过(1) 时，页面设为只读
    const isPending = status === enterpriseUtil.QUALIFICATION_STATUS.PENDING;
    const isApproved = status === enterpriseUtil.QUALIFICATION_STATUS.APPROVED;
    const readonly = mode === 'view' || isPending || isApproved;

    console.log('[Qualification] Status:', status, 'Readonly:', readonly);

    this.setData({
      mode,
      readonly,
      enterpriseName: userInfo.enterpriseName || '',
      creditCode: userInfo.creditCode || '',
      legalPerson: userInfo.legalPerson || '',
      contactPhone: userInfo.contactPhone || '',
      licenseImg: userInfo.licenseImg || '',
      qualificationStatus: status,
      btnText: status === enterpriseUtil.QUALIFICATION_STATUS.REJECTED ? '重新申请' : '提交申请'
    });
  },

  onInput(e) {
    if (this.data.readonly) return;
    const field = e.currentTarget.dataset.field;
    this.setData({ [field]: e.detail.value });
  },

  onLicenseChange(e) {
    if (this.data.readonly) return;
    this.setData({ licenseImg: e.detail.value });
  },

  previewLicense() {
    const { licenseImg } = this.data;
    if (licenseImg) wx.previewImage({ urls: [licenseImg] });
  },

  validate() {
    const { enterpriseName, creditCode, legalPerson, contactPhone, licenseImg } = this.data;
    if (!enterpriseName || !creditCode || !legalPerson || !contactPhone) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return false;
    }
    if (!licenseImg) {
      wx.showToast({ title: '请上传营业执照', icon: 'none' });
      return false;
    }
    return true;
  },

  async onSubmit() {
    if (this.data.readonly) {
      wx.navigateBack();
      return;
    }
    if (!this.validate()) return;

    const { enterpriseName, creditCode, legalPerson, contactPhone, licenseImg } = this.data;
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    
    wx.showLoading({ title: '提交中...' });

    // 格式化当前时间为 yyyy-MM-dd HH:mm:ss
    const now = new Date();
    const formatDate = (date) => {
      const pad = (n) => (n < 10 ? '0' + n : n);
      return date.getFullYear() + '-' + 
             pad(date.getMonth() + 1) + '-' + 
             pad(date.getDate()) + ' ' + 
             pad(date.getHours()) + ':' + 
             pad(date.getMinutes()) + ':' + 
             pad(date.getSeconds());
    };
    const currentTime = formatDate(now);

    try {
      const res = await request.post('/enterpriseInfo/apply', {
        applicantId: userInfo.userId || 30002,
        comment: "首次提交企业资质认证申请，材料齐全，请审核",
        enterprise: {
          enterpriseName: enterpriseName,
          creditCode: creditCode,
          legalPerson: legalPerson,
          contactPhone: contactPhone,
          businessLicenseUrl: licenseImg,
          createTime: currentTime,
          updateTime: currentTime,
          isDeleted: 0
        }
      });

      if (res.code !== undefined && res.code !== 200) {
        wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
        return;
      }

      // 更新本地存储的状态
      userInfo.qualificationStatus = enterpriseUtil.QUALIFICATION_STATUS.PENDING;
      wx.setStorageSync('userInfo', userInfo);

      wx.showModal({
        title: '提交成功',
        content: '企业资质申请已提交，请等待交警部门审核。',
        showCancel: false,
        success: () => wx.navigateBack()
      });
    } catch (err) {
      console.error('Enterprise qualification apply failed:', err);
      wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
    } finally {
      wx.hideLoading();
    }
  }
});
