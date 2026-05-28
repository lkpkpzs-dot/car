const mockData = require('../../utils/mock.js');

Page({
  data: {
    vehicle: null,
    statusText: '',
    statusColor: '',
    isValidText: '',
    isValidColor: ''
  },

  onLoad(options) {
    const vehicleId = options.id || 1;
    this.loadVehicleDetail(vehicleId);
  },

  loadVehicleDetail(id) {
    const vehicle = mockData.getVehicleDetail(parseInt(id));
    
    let statusText = '';
    let statusColor = '';
    switch (vehicle.status) {
      case 'online':
        statusText = '在线';
        statusColor = '#38a169';
        break;
      case 'offline':
        statusText = '离线';
        statusColor = '#718096';
        break;
      case 'warning':
        statusText = '异常';
        statusColor = '#e53e3e';
        break;
      default:
        statusText = '未知';
        statusColor = '#999';
    }

    const isValidText = vehicle.isValid ? '合规' : '不合规';
    const isValidColor = vehicle.isValid ? '#38a169' : '#e53e3e';

    this.setData({
      vehicle: vehicle,
      statusText: statusText,
      statusColor: statusColor,
      isValidText: isValidText,
      isValidColor: isValidColor
    });
  },

  onShareAppMessage() {
    return {
      title: '智车通 - 车辆详情',
      path: `/pages/vehicle/vehicle?id=${this.data.vehicle.id}`
    };
  }
});
