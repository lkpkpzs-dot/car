const mockData = require('../../utils/mock.js');
const auth = require('../../utils/auth.js');

Page({
  data: {
    scanResult: null,
    hasScanned: false,
    isScanning: false
  },

  onLoad() {
    auth.requireRole(auth.ROLES.ADMIN);
  },

  onShow() {
    
  },

  onScanTap() {
    this.setData({
      isScanning: true,
      hasScanned: false,
      scanResult: null
    });

    wx.scanCode({
      success: (res) => {
        console.log('扫码成功', res);
        this.handleScanResult(res);
      },
      fail: (err) => {
        console.error('扫码失败', err);
        wx.showToast({
          title: '扫码失败',
          icon: 'none'
        });
        this.setData({
          isScanning: false
        });
      }
    });
  },

  handleScanResult(result) {
    let vehicleId = 1;
    
    if (result.result) {
      const match = result.result.match(/id[=:]?(\d+)/i);
      if (match) {
        vehicleId = parseInt(match[1]);
      }
    }
    
    const vehicle = mockData.getVehicleDetail(vehicleId);
    
    this.setData({
      scanResult: vehicle,
      hasScanned: true,
      isScanning: false
    });

    wx.showToast({
      title: '扫码成功',
      icon: 'success'
    });
  },

  onViewDetail() {
    if (this.data.scanResult) {
      wx.navigateTo({
        url: `/pages/vehicle/vehicle?id=${this.data.scanResult.id}`
      });
    }
  },

  onScanAgain() {
    this.setData({
      hasScanned: false,
      scanResult: null
    });
    this.onScanTap();
  }
});
