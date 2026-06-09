const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');

Page({
  data: {
    submitting: false,
    reportTypes: [
      { id: 1, name: '违规占道', icon: '🚫' },
      { id: 2, name: '乱停乱放', icon: '🅿️' },
      { id: 3, name: '违规行驶', icon: '🚗' },
      { id: 4, name: '意见建议', icon: '💡' }
    ],
    selectedType: null,
    riskLevels: [
      { value: 1, name: '低风险' },
      { value: 2, name: '高风险' }
    ],
    selectedRiskLevel: 1,
    formData: {
      targetPlate: '',
      locationExt: '',
      images: []
    }
  },

  onLoad() {
  },

  onSelectType(e) {
    const typeId = e.currentTarget.dataset.type;
    this.setData({ selectedType: typeId });
  },

  onSelectRiskLevel(e) {
    const level = e.currentTarget.dataset.level;
    this.setData({ selectedRiskLevel: level });
  },

  onInput(e) {
    const field = e.currentTarget.dataset.field;
    const value = e.detail.value;
    this.setData({ [`formData.${field}`]: value });
  },

  onChooseImage() {
    const remaining = 9 - this.data.formData.images.length;
    wx.chooseImage({
      count: remaining,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        await this.uploadImages(res.tempFilePaths);
      }
    });
  },

  async uploadImages(tempFilePaths) {
    wx.showLoading({ title: '上传中...' });
    
    try {
      const urls = [];
      for (let i = 0; i < tempFilePaths.length; i++) {
        const url = await request.uploadFile(tempFilePaths[i]);
        urls.push(url);
      }
      
      this.setData({
        'formData.images': [...this.data.formData.images, ...urls]
      });
      
      wx.hideLoading();
    } catch (error) {
      wx.hideLoading();
      wx.showToast({
        title: '上传失败',
        icon: 'none'
      });
    }
  },

  onDeleteImage(e) {
    const index = e.currentTarget.dataset.index;
    const images = [...this.data.formData.images];
    images.splice(index, 1);
    this.setData({
      'formData.images': images
    });
  },

  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      urls: this.data.formData.images,
      current: this.data.formData.images[index]
    });
  },

  validate() {
    const { selectedType, formData, selectedRiskLevel } = this.data;
    
    if (!selectedType) {
      wx.showToast({ title: '请选择举报类型', icon: 'none' });
      return false;
    }
    if (!selectedRiskLevel) {
      wx.showToast({ title: '请选择风险等级', icon: 'none' });
      return false;
    }
    if (!formData.locationExt.trim()) {
      wx.showToast({ title: '请输入发生地点', icon: 'none' });
      return false;
    }
    if (formData.images.length === 0) {
      wx.showToast({ title: '请上传至少一张图片', icon: 'none' });
      return false;
    }
    
    return true;
  },

  async onSubmit() {
    if (this.data.submitting) return;
    if (!this.validate()) return;

    this.setData({ submitting: true });

    try {
      const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
      
      const res = await request.post('/citizenReport/submit', {
        reportType: this.data.selectedType,
        riskLevel: this.data.selectedRiskLevel,
        targetPlate: this.data.formData.targetPlate,
        locationExt: this.data.formData.locationExt,
        evidenceJson: JSON.stringify(this.data.formData.images),
        userId: userInfo.userId
      });
      
      if (res.code === 200) {
        wx.showModal({
          title: '提交成功',
          content: '感谢您的举报，我们会尽快处理！',
          showCancel: false,
          success: () => {
            wx.navigateBack();
          }
        });
      } else {
        wx.showToast({
          title: res.msg || '提交失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
