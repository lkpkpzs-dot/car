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
      locationLat: '', // 纬度
      locationLng: '', // 经度
      images: []
    },
    hasLocation: false
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

    if (field === 'locationExt' && this.data.formData.locationLat && this.data.formData.locationLng) {
      this.setData({ hasLocation: true });
    }
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

  async onGetLocation() {
    await this.chooseLocationOnMap();
  },

  async chooseLocationOnMap() {
    wx.chooseLocation({
      success: (res) => {
        const address = res.address || res.name || `${res.latitude.toFixed(4)},${res.longitude.toFixed(4)}`;

        this.setData({
          'formData.locationExt': address,
          'formData.locationLat': res.latitude,
          'formData.locationLng': res.longitude,
          hasLocation: true
        });

        wx.showToast({
          title: '位置已选择',
          icon: 'success'
        });
      },
      fail: (error) => {
        console.error('选择位置失败:', error);
        wx.showToast({
          title: '选择位置失败',
          icon: 'none'
        });
      }
    });
  },

  /*
  // getLocation 权限未通过，暂不启用“获取当前位置”。
  async getCurrentLocation() {
    wx.showLoading({ title: '获取位置中...' });
    let toastOptions = null;

    try {
      const location = await this.getLocation();
      if (location) {
        const address = await this.reverseGeocode(location.latitude, location.longitude);
        this.setData({
          'formData.locationExt': address,
          'formData.locationLat': location.latitude,
          'formData.locationLng': location.longitude,
          hasLocation: true
        });
        toastOptions = {
          title: '获取位置成功',
          icon: 'success'
        };
      }
    } catch (error) {
      console.error('获取位置失败:', error);
      toastOptions = {
        title: '获取位置失败，请手动输入',
        icon: 'none'
      };
    } finally {
      wx.hideLoading();
    }

    if (toastOptions) {
      wx.showToast(toastOptions);
    }
  },

  getLocation() {
    return new Promise((resolve, reject) => {
      wx.getLocation({
        type: 'gcj02',
        altitude: false,
        success: (res) => {
          resolve({
            latitude: res.latitude,
            longitude: res.longitude
          });
        },
        fail: (error) => {
          if (error.errMsg && error.errMsg.includes('auth deny')) {
            wx.showModal({
              title: '需要位置权限',
              content: '请授权允许使用位置信息，才能自动获取地址',
              confirmText: '去授权',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  wx.openSetting();
                }
              }
            });
          }
          reject(error);
        }
      });
    });
  },

  async reverseGeocode(latitude, longitude) {
    try {
      const res = await request.get('/common/reverseGeocode', {
        latitude,
        longitude
      });

      if (res.code === 200 && res.data) {
        return res.data.address || res.data.formatted_address || `${latitude.toFixed(4)},${longitude.toFixed(4)}`;
      }
    } catch (error) {
      console.error('逆地理编码API调用失败:', error);
    }

    return `${latitude.toFixed(4)},${longitude.toFixed(4)}`;
  },
  */

  // 车牌格式校验
  validatePlateNumber(plate) {
    if (!plate || !plate.trim()) {
      return false; // 车牌是必填的，不能为空
    }
    
    // 中国车牌正则：包含新能源
    const plateReg = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]?$/;
    return plateReg.test(plate);
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
    if (!formData.targetPlate.trim()) {
      wx.showToast({ title: '请输入车辆号牌', icon: 'none' });
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
    // 车牌格式校验
    if (!this.validatePlateNumber(formData.targetPlate)) {
      wx.showModal({
        title: '提示',
        content: '您填写的车牌格式不正确。车牌格式应为：省份简称+字母+5-6位字符（如：京A12345）。是否继续提交？',
        confirmText: '继续提交',
        cancelText: '重新填写',
        success: (res) => {
          if (res.confirm) {
            // 用户选择继续，直接提交
            this.doSubmit();
          }
        }
      });
      return false;
    }
    
    return true;
  },

  async doSubmit() {
    this.setData({ submitting: true });
    try {
      const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
      
      const res = await request.post('/citizenReport/submit', {
        reportType: this.data.selectedType,
        riskLevel: this.data.selectedRiskLevel,
        targetPlate: this.data.formData.targetPlate,
        locationExt: this.data.formData.locationExt,
        locationLat: this.data.formData.locationLat,
        locationLng: this.data.formData.locationLng,
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
        // 处理防恶意举报相关的错误信息
        let message = res.msg || '提交失败';
        
        // 根据不同的错误类型显示更友好的提示
        if (res.code === 403) {
          if (res.msg && res.msg.includes('封禁')) {
            message = res.msg;
          } else if (res.msg && res.msg.includes('次数')) {
            message = res.msg;
          } else if (res.msg && res.msg.includes('间隔')) {
            message = res.msg;
          }
        }
        
        wx.showToast({
          title: message,
          icon: 'none',
          duration: 3000
        });
      }
    } catch (error) {
      console.error('提交举报失败:', error);
      let message = '提交失败，请重试';
      
      if (error.statusCode === 403) {
        try {
          const data = error.data;
          if (data && data.msg) {
            message = data.msg;
          }
        } catch (e) {
          message = '您的举报权限可能已被限制';
        }
      }
      
      wx.showToast({
        title: message,
        icon: 'none',
        duration: 3000
      });
    } finally {
      this.setData({ submitting: false });
    }
  },

  checkUserProfile() {
    const userInfo = wx.getStorageSync('userInfo');
    const hasRealName = userInfo && (userInfo.realName || userInfo.real_name);
    const hasPhone = userInfo && userInfo.phone;
    
    return hasRealName && hasPhone;
  },

  async onSubmit() {
    if (this.data.submitting) return;
    
    // 检查是否填写了真实姓名和手机号
    if (!this.checkUserProfile()) {
      wx.showModal({
        title: '需要完善信息',
        content: '提交举报前需要先填写您的真实姓名和手机号',
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

    // 正常校验通过，直接提交
    await this.doSubmit();
  }
});
