const request = require('../../../../utils/request.js');

Page({
  data: {
    application: null,
    formData: {
      vin: '',
      plateType: 1,
      plateNumber: '',
      issueDate: '',
      expiryDate: '',
      issuerId: null,
      issueComment: ''
    },
    plateTypeOptions: [
      { value: 1, label: '道路测试牌照' },
      { value: 2, label: '示范应用牌照' },
      { value: 3, label: '应用试点牌照' }
    ]
  },

  onLoad(options) {
    if (options.data) {
      try {
        const item = JSON.parse(decodeURIComponent(options.data));
        console.log('[IssuePlate] Received application:', item);
        
        // 获取当前日期作为默认发牌日期
        const today = new Date();
        const issueDate = this.formatDate(today);
        
        // 默认到期日期为一年后
        const expiryDate = new Date(today);
        expiryDate.setFullYear(expiryDate.getFullYear() + 1);
        const formattedExpiryDate = this.formatDate(expiryDate);
        
        // 获取用户ID
        const userInfo = wx.getStorageSync('userInfo');
        const issuerId = userInfo?.userId || 30002;
        
        this.setData({
          application: item,
          'formData.vin': item.vin || '',
          'formData.plateType': item.type || 1,
          'formData.issueDate': issueDate,
          'formData.expiryDate': formattedExpiryDate,
          'formData.issuerId': issuerId
        });
      } catch (e) {
        console.error('Parse application data failed:', e);
        wx.showToast({ title: '数据解析失败', icon: 'none' });
      }
    }
  },

  formatDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`formData.${field}`]: e.detail.value });
  },

  onPlateTypeChange(e) {
    const index = parseInt(e.detail.value, 10);
    const selectedOption = this.data.plateTypeOptions[index];
    this.setData({
      'formData.plateType': selectedOption.value
    });
  },

  onDateChange(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`formData.${field}`]: e.detail.value });
  },

  validate() {
    const { formData } = this.data;
    
    if (!formData.vin || formData.vin.trim() === '') {
      wx.showToast({ title: 'VIN码不能为空', icon: 'none' });
      return false;
    }
    
    if (!formData.plateNumber || formData.plateNumber.trim() === '') {
      wx.showToast({ title: '车牌号不能为空', icon: 'none' });
      return false;
    }
    
    if (!formData.issuerId) {
      wx.showToast({ title: '发牌民警ID不能为空', icon: 'none' });
      return false;
    }
    
    return true;
  },

  formatDateTimeForBackend(dateStr) {
    if (!dateStr) return null;
    // 格式化为 yyyy-MM-dd HH:mm:ss
    return `${dateStr} 00:00:00`;
  },

  async onSubmit() {
    if (!this.validate()) return;

    const { formData } = this.data;
    
    // 格式化日期供后端使用
    const submitData = {
      ...formData,
      issueDate: this.formatDateTimeForBackend(formData.issueDate),
      expiryDate: formData.expiryDate ? this.formatDateTimeForBackend(formData.expiryDate) : null
    };
    
    wx.showLoading({ title: '提交中...' });
    
    try {
      const res = await request.post('/carArchive/issuePlate', submitData);
      wx.hideLoading();
      
      if (res.code === 200) {
        wx.showModal({
          title: '发牌成功',
          content: '车辆牌照已成功发放！',
          showCancel: false,
          success: () => {
            const pages = getCurrentPages();
            const prevPage = pages[pages.length - 2];
            if (prevPage && prevPage.fetchList) {
              prevPage.fetchList();
            }
            wx.navigateBack();
          }
        });
      } else {
        wx.showToast({ 
          title: res.msg || '发牌失败', 
          icon: 'none',
          duration: 2500
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('Issue plate failed:', err);
      wx.showToast({ title: '发牌失败，请稍后重试', icon: 'none' });
    }
  }
});
