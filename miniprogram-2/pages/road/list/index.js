const request = require('../../../utils/request.js');

Page({
  data: {
    list: [],
    statusMap: {
      1: { label: '待审核', class: 'pending' },
      2: { label: '通过', class: 'approved' },
      3: { label: '已驳回', class: 'rejected' }
    },
    typeMap: {
      1: '道路测试',
      2: '示范应用',
      3: '应用试点'
    }
  },

  onShow() {
    this.fetchList();
  },

  async fetchList() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await request.get('/roadApplication/myList');
      wx.hideLoading();
      
      const rawList = request.parseListData(res);
      const list = rawList.map(item => ({
        ...item,
        statusLabel: this.data.statusMap[item.status]?.label || '未知',
        typeLabel: this.data.typeMap[item.type] || '其他',
        createTime: item.createTime ? item.createTime.split(' ')[0] : ''
      }));

      this.setData({ list });
    } catch (err) {
      wx.hideLoading();
      console.error('Fetch list failed', err);
      // Mock 数据供预览
      this.setData({ list: this.getMockList() });
    }
  },

  getMockList() {
    return [
      { id: 101, type: 1, typeLabel: '道路测试', status: 1, statusLabel: '待审核', vehicleBrand: '智行者', vehicleModel: 'A1', testArea: '南沙区', createTime: '2026-05-27' },
      { id: 102, type: 2, typeLabel: '示范应用', status: 2, statusLabel: '通过', vehicleBrand: '文远知行', vehicleModel: 'W1', testArea: '黄埔区', createTime: '2026-05-25' },
      { id: 103, type: 1, typeLabel: '道路测试', status: 3, statusLabel: '已驳回', vehicleBrand: '小马智行', vehicleModel: 'P1', testArea: '海珠区', createTime: '2026-05-20', rejectReason: '保障计划不够详细，请细化风险应对措施。' }
    ];
  },

  onDetail(e) {
    const { item } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/road/apply/index?data=${encodeURIComponent(JSON.stringify(item))}`
    });
  },

  onAdd() {
    wx.navigateTo({
      url: '/pages/road/apply/index'
    });
  }
});
