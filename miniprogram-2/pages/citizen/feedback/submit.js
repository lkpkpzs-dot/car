const request = require('../../../utils/request.js');

Page({
  data: {
    submitting: false,
    types: [
      { id: 1, name: '功能建议', icon: '💡' },
      { id: 2, name: 'Bug反馈', icon: '🐛' },
      { id: 3, name: '其他', icon: '📝' }
    ],
    selectedType: null,
    formData: {
      title: '',
      content: '',
      contact: '',
      images: []
    }
  },

  onSelectType(e) {
    const typeId = e.currentTarget.dataset.type;
    this.setData({ selectedType: typeId });
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
    const { selectedType, formData } = this.data;
    
    if (!selectedType) {
      wx.showToast({ title: '请选择类型', icon: 'none' });
      return false;
    }
    if (!formData.title.trim()) {
      wx.showToast({ title: '请输入标题', icon: 'none' });
      return false;
    }
    if (!formData.content.trim()) {
      wx.showToast({ title: '请输入详细内容', icon: 'none' });
      return false;
    }
    return true;
  },

  async onSubmit() {
    if (!this.validate()) return;
    if (this.data.submitting) return;

    this.setData({ submitting: true });
    try {
      const res = await request.post('/feedback/submit', {
        feedbackType: this.data.selectedType,
        title: this.data.formData.title,
        content: this.data.formData.content,
        contact: this.data.formData.contact,
        images: JSON.stringify(this.data.formData.images)
      });
      
      if (res.code === 200) {
        wx.showModal({
          title: '提示',
          content: '感谢您的反馈！您可以在"我的消息"中查看处理进度。',
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
      console.error('提交失败:', error);
      wx.showToast({
        title: '提交失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
