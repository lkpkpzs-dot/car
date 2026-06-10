const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');
const validator = require('../../../utils/validator.js');

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
    const hasEnterpriseId = !!userInfo.authEnterpriseId;

    // 如果没有 authEnterpriseId，即使 qualificationStatus 是1也允许编辑
    const isPending = status === enterpriseUtil.QUALIFICATION_STATUS.PENDING;
    const isApproved = status === enterpriseUtil.QUALIFICATION_STATUS.APPROVED && hasEnterpriseId;
    const readonly = mode === 'view' || isPending || isApproved;

    this.setData({
      mode,
      readonly,
      enterpriseName: userInfo.enterpriseName || '',
      creditCode: userInfo.creditCode || '',
      legalPerson: userInfo.legalPerson || '',
      contactPhone: userInfo.contactPhone || '',
      licenseImg: userInfo.licenseImg || '',
      qualificationStatus: status,
      btnText: status === enterpriseUtil.QUALIFICATION_STATUS.REJECTED || !hasEnterpriseId ? '重新申请' : '提交申请'
    });
  },

  onInput(e) {
    if (this.data.readonly) return;
    const field = e.currentTarget.dataset.field;
    let value = e.detail.value;

    if (field === 'creditCode') {
      value = value.toUpperCase();
      if (value.length > 18) value = value.slice(0, 18);
    }
    if (field === 'contactPhone') {
      value = validator.filterPositiveInteger(value);
      if (value.length > 11) value = value.slice(0, 11);
    }

    this.setData({ [field]: value });
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
    
    if (!enterpriseName) {
      wx.showToast({ title: '请填写企业名称', icon: 'none' });
      return false;
    }
    if (enterpriseName.length < 2 || enterpriseName.length > 100) {
      wx.showToast({ title: '企业名称长度需在2-100字之间', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(enterpriseName)) {
      wx.showToast({ title: '企业名称不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!creditCode) {
      wx.showToast({ title: '请填写统一社会信用代码', icon: 'none' });
      return false;
    }
    if (!validator.isCreditCode(creditCode)) {
      wx.showToast({ title: '统一社会信用代码应为18位', icon: 'none' });
      return false;
    }

    if (!legalPerson) {
      wx.showToast({ title: '请填写法定代表人', icon: 'none' });
      return false;
    }
    if (legalPerson.length < 2 || legalPerson.length > 20) {
      wx.showToast({ title: '法定代表人姓名长度需在2-20字之间', icon: 'none' });
      return false;
    }

    if (!contactPhone) {
      wx.showToast({ title: '请填写联系电话', icon: 'none' });
      return false;
    }
    if (!validator.isPhone(contactPhone)) {
      wx.showToast({ title: '请填写正确的手机号', icon: 'none' });
      return false;
    }

    if (!licenseImg) {
      wx.showToast({ title: '请上传营业执照', icon: 'none' });
      return false;
    }
    return true;
  },

  checkUserProfile() {
    const userInfo = wx.getStorageSync('userInfo');
    const hasRealName = userInfo && (userInfo.realName || userInfo.real_name);
    const hasPhone = userInfo && userInfo.phone;
    
    return hasRealName && hasPhone;
  },

  async onSubmit() {
    if (this.data.readonly) {
      wx.navigateBack();
      return;
    }
    
    // 检查是否填写了真实姓名和手机号
    if (!this.checkUserProfile()) {
      wx.showModal({
        title: '需要完善信息',
        content: '申请企业资质前需要先填写您的真实姓名和手机号',
        confirmText: '去填写',
        cancelText: '取消',
        success: (res) => {
          if (res.confirm) {
            wx.navigateTo({ url: '/pages/account-settings/index' });
          }
        }
      });
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
        wx.showModal({
          title: '提示',
          content: res.msg || '提交失败',
          showCancel: false
        });
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
      let errorMsg = '提交失败，请稍后重试';
      // 尝试从错误中提取更友好的提示
      if (err && err.data && err.data.msg) {
        errorMsg = err.data.msg;
      } else if (err && err.msg) {
        errorMsg = err.msg;
      } else if (err && err.message) {
        errorMsg = err.message;
      }
      wx.showModal({
        title: '提示',
        content: errorMsg,
        showCancel: false
      });
    } finally {
      wx.hideLoading();
    }
  }
});
