const mockData = require('../../utils/mock.js');

Page({
  data: {
    stats: {
      total: 0,
      online: 0,
      warning: 0,
      todayAdd: 0
    },
    markers: [],
    scale: 12,
    latitude: 31.23,
    longitude: 121.47,
    runningTrend: null,
    warningTrend: null
  },

  onLoad() {
    this.loadData();
  },

  onShow() {
    this.loadData();
  },

  loadData() {
    const stats = mockData.getDashboardStat();
    const vehicleList = mockData.getVehicleList();
    const runningTrend = mockData.getRunningTrend();
    const warningTrend = mockData.getWarningTrend();

    const markers = vehicleList.map(vehicle => {
      let iconPath = '/assets/images/car-online.png';
      if (vehicle.status === 'offline') {
        iconPath = '/assets/images/car-offline.png';
      } else if (vehicle.status === 'warning') {
        iconPath = '/assets/images/car-warning.png';
      }

      return {
        id: vehicle.id,
        latitude: vehicle.latitude,
        longitude: vehicle.longitude,
        iconPath: iconPath,
        width: 30,
        height: 30,
        callout: {
          content: vehicle.name,
          color: '#333',
          fontSize: 12,
          borderRadius: 4,
          padding: 6,
          display: 'BYCLICK',
          bgColor: '#fff'
        }
      };
    });

    this.setData({
      stats: stats,
      markers: markers,
      runningTrend: runningTrend,
      warningTrend: warningTrend
    });
  },

  onMarkerTap(e) {
    const markerId = e.detail.markerId;
    wx.navigateTo({
      url: `/pages/vehicle/vehicle?id=${markerId}`
    });
  }
});
