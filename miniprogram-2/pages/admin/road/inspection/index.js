const request = require('../../../../utils/request.js');

Page({
  data: {
    application: null,
    inspectionData: {
      applicationId: null,
      length: '',
      width: '',
      height: '',
      totalMass: '',
      curbWeight: '',
      ratedLoad: '',
      axleCount: '',
      tireSpec: '',
      motorPower: '',
      motorNo: '',
      maxSpeed: '',
      batteryType: '',
      batteryCapacity: '',
      photoFront_45: '',
      photoRear_45: '',
      photoVin: '',
      docVehicleCertUnmanned: '',
      auditStatus: 1
    }
  },

  onLoad(options) {
    if (options.data) {
      try {
        const item = JSON.parse(decodeURIComponent(options.data));
        console.log('[Inspection] Received application:', item);
        
        this.setData({
          application: item,
          'inspectionData.applicationId': item.id || item.applicationId
        });
      } catch (e) {
        console.error('Parse application data failed:', e);
        wx.showToast({ title: '数据解析失败', icon: 'none' });
      }
    }
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [`inspectionData.${field}`]: e.detail.value });
  },

  onUploadChange(e) {
    const { key } = e.currentTarget.dataset;
    this.setData({ [`inspectionData.${key}`]: e.detail.value });
  },

  validate() {
    const { inspectionData } = this.data;
    const requiredFields = [
      'length', 'width', 'height', 'totalMass', 'curbWeight', 
      'axleCount', 'tireSpec', 'motorPower', 'motorNo', 'maxSpeed',
      'photoFront_45', 'photoRear_45', 'photoVin', 'docVehicleCertUnmanned'
    ];
    
    for (const field of requiredFields) {
      if (!inspectionData[field]) {
        const fieldLabels = {
          length: '车长(mm)',
          width: '车宽(mm)',
          height: '车高(mm)',
          totalMass: '总质量(kg)',
          curbWeight: '整备质量(kg)',
          axleCount: '轴数',
          tireSpec: '轮胎规格',
          motorPower: '电机功率(kW)',
          motorNo: '电机编号',
          maxSpeed: '最高车速(km/h)',
          photoFront_45: '车前45°照片',
          photoRear_45: '车后45°照片',
          photoVin: 'VIN码照片',
          docVehicleCertUnmanned: '无人车车辆证书'
        };
        wx.showToast({ 
          title: `请填写${fieldLabels[field] || field}`, 
          icon: 'none' 
        });
        return false;
      }
    }
    return true;
  },

  async onSubmit() {
    if (!this.validate()) return;

    const { inspectionData } = this.data;
    
    wx.showLoading({ title: '提交中...' });
    
    try {
      const res = await request.post('/vehicle/inspection/submit', inspectionData);
      wx.hideLoading();
      
      if (res.code === 200) {
        wx.showModal({
          title: '提交成功',
          content: '车辆查验信息已提交成功！',
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
        const errorMsg = res.msg || '提交失败';
        // 检查是否是重复提交的错误
        if (errorMsg.includes('已完成查验') || errorMsg.includes('不可重复提交')) {
          wx.showModal({
            title: '提示',
            content: errorMsg,
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
            title: errorMsg, 
            icon: 'none',
            duration: 2500
          });
        }
      }
    } catch (err) {
      wx.hideLoading();
      console.error('Submit inspection failed:', err);
      wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
    }
  }
});
