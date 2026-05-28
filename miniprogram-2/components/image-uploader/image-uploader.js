const request = require('../../utils/request.js');

Component({
  properties: {
    label: { type: String, value: '上传图片' },
    value: { type: String, value: '' }
  },

  methods: {
    async onChoose() {
      wx.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const path = res.tempFilePaths[0];
          
          wx.showLoading({ title: '上传中...' });
          try {
            const url = await request.uploadFile(path);
            wx.hideLoading();
            this.triggerEvent('change', { value: url });
          } catch (err) {
            wx.hideLoading();
            console.error('Upload failed:', err);
            wx.showToast({ title: '上传失败', icon: 'none' });
          }
        }
      });
    },

    onPreview() {
      if (!this.data.value) return;
      wx.previewImage({ urls: [this.data.value] });
    },

    onRemove() {
      this.triggerEvent('change', { value: '' });
    }
  }
});
