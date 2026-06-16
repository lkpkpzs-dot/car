const request = require('../../../utils/request.js');

Page({
  data: {
    vehicleList: [],
    plateTypeMap: {
      1: '试验用机动车临时行驶车号牌',
      2: '其他'
    }
  },

  onLoad() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && !userInfo.authEnterpriseId) {
      wx.showModal({
        title: '提示',
        content: '请先完成企业资质认证',
        showCancel: false,
        success: () => {
          wx.switchTab({ url: '/pages/profile/index' });
        }
      });
    }
  },

  onShow() {
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && !userInfo.authEnterpriseId) {
      return;
    }
    this.fetchVehicleList();
  },

  async fetchVehicleList() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await request.get('/enterprise/dashboard');
      
      if (res.code === 200 && res.data && res.data.vehicleList) {
        this.setData({
          vehicleList: res.data.vehicleList
        });
      }
    } catch (err) {
      console.error('[Vehicles] Load failed:', err);
      // 使用 Mock 数据供预览
      this.setData({
        vehicleList: this.getMockVehicleList()
      });
    } finally {
      wx.hideLoading();
    }
  },

  getMockVehicleList() {
    return [
      {
        vehicleId: 10,
        vin: '123',
        vehicleBrand: '奔驰',
        plateNumber: '晋MPK562',
        plateType: 1,
        issueDate: '2026-05-28'
      }
    ];
  },

  onVehicleDetail(e) {
    const { vehicle } = e.currentTarget.dataset;
    wx.showModal({
      title: '车辆详情',
      content: `品牌：${vehicle.vehicleBrand}\nVIN码：${vehicle.vin}\n车牌号：${vehicle.plateNumber}\n号牌类型：${this.data.plateTypeMap[vehicle.plateType] || '其他'}\n发证日期：${vehicle.issueDate}`,
      showCancel: false,
      confirmText: '知道了'
    });
  }
});
