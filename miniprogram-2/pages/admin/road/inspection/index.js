const request = require('../../../../utils/request.js');
const validator = require('../../../../utils/validator.js');

Page({
  data: {
    submitting: false,
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
    let value = e.detail.value;

    const numericFields = ['length', 'width', 'height', 'totalMass', 'curbWeight', 'ratedLoad', 'axleCount', 'motorPower', 'maxSpeed', 'batteryCapacity'];
    
    if (numericFields.includes(field)) {
      value = validator.filterNumber(value);
    }
    
    if (field === 'tireSpec' || field === 'motorNo' || field === 'batteryType') {
      if (validator.hasSpecialChars(value)) {
        wx.showToast({ title: '不能输入特殊符号', icon: 'none', duration: 1000 });
        return;
      }
    }

    this.setData({ [`inspectionData.${field}`]: value });
  },

  onUploadChange(e) {
    const { key } = e.currentTarget.dataset;
    this.setData({ [`inspectionData.${key}`]: e.detail.value });
  },

  validate() {
    const { inspectionData } = this.data;
    const d = inspectionData;
    
    if (!d.length) {
      wx.showToast({ title: '请填写车长', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.length, 500, 20000)) {
      wx.showToast({ title: '车长范围应为500-20000mm', icon: 'none' });
      return false;
    }

    if (!d.width) {
      wx.showToast({ title: '请填写车宽', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.width, 300, 6000)) {
      wx.showToast({ title: '车宽范围应为300-6000mm', icon: 'none' });
      return false;
    }

    if (!d.height) {
      wx.showToast({ title: '请填写车高', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.height, 300, 8000)) {
      wx.showToast({ title: '车高范围应为300-8000mm', icon: 'none' });
      return false;
    }

    if (!d.totalMass) {
      wx.showToast({ title: '请填写总质量', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.totalMass, 1, 100000)) {
      wx.showToast({ title: '总质量范围应为1-100000kg', icon: 'none' });
      return false;
    }

    if (!d.curbWeight) {
      wx.showToast({ title: '请填写整备质量', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.curbWeight, 1, 50000)) {
      wx.showToast({ title: '整备质量范围应为1-50000kg', icon: 'none' });
      return false;
    }

    if (!d.ratedLoad) {
      wx.showToast({ title: '请填写额定载质量', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.ratedLoad, 0, 50000)) {
      wx.showToast({ title: '额定载质量范围应为0-50000kg', icon: 'none' });
      return false;
    }

    if (!d.axleCount) {
      wx.showToast({ title: '请填写轴数', icon: 'none' });
      return false;
    }
    if (!validator.isValidPositiveInteger(d.axleCount) || parseInt(d.axleCount) > 10) {
      wx.showToast({ title: '轴数应为1-10的整数', icon: 'none' });
      return false;
    }

    if (!d.tireSpec) {
      wx.showToast({ title: '请填写轮胎规格', icon: 'none' });
      return false;
    }
    if (d.tireSpec.length < 2 || d.tireSpec.length > 50) {
      wx.showToast({ title: '轮胎规格长度应为2-50字', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(d.tireSpec)) {
      wx.showToast({ title: '轮胎规格不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!d.motorPower) {
      wx.showToast({ title: '请填写电机功率', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.motorPower, 0.1, 10000)) {
      wx.showToast({ title: '电机功率范围应为0.1-10000kW', icon: 'none' });
      return false;
    }

    if (!d.motorNo) {
      wx.showToast({ title: '请填写电机编号', icon: 'none' });
      return false;
    }
    if (d.motorNo.length < 1 || d.motorNo.length > 100) {
      wx.showToast({ title: '电机编号长度应为1-100字', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(d.motorNo)) {
      wx.showToast({ title: '电机编号不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!d.maxSpeed) {
      wx.showToast({ title: '请填写最高车速', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.maxSpeed, 1, 300)) {
      wx.showToast({ title: '最高车速范围应为1-300km/h', icon: 'none' });
      return false;
    }

    if (!d.batteryType) {
      wx.showToast({ title: '请填写电池类型', icon: 'none' });
      return false;
    }
    if (d.batteryType.length < 1 || d.batteryType.length > 100) {
      wx.showToast({ title: '电池类型长度应为1-100字', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(d.batteryType)) {
      wx.showToast({ title: '电池类型不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!d.batteryCapacity) {
      wx.showToast({ title: '请填写电池容量', icon: 'none' });
      return false;
    }
    if (!validator.isValidNumber(d.batteryCapacity, 0.1, 10000)) {
      wx.showToast({ title: '电池容量范围应为0.1-10000kWh', icon: 'none' });
      return false;
    }

    if (!d.photoFront_45 || !d.photoRear_45 || !d.photoVin || !d.docVehicleCertUnmanned) {
      wx.showToast({ title: '请上传所有必要照片', icon: 'none' });
      return false;
    }

    return true;
  },

  async onSubmit() {
    if (this.data.submitting) return;
    if (!this.validate()) return;

    const { inspectionData } = this.data;
    
    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });
    
    try {
      const res = await request.post('/vehicle/inspection/submit', inspectionData);
      
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
        // 检查是否是重复提交的错误或VIN已存在
        if (errorMsg.includes('已完成查验') || errorMsg.includes('不可重复提交') || errorMsg.includes('VIN已存在')) {
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
      console.error('Submit inspection failed:', err);
      wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' });
    } finally {
      wx.hideLoading();
      this.setData({ submitting: false });
    }
  }
});
