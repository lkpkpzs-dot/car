const request = require('../../../utils/request.js');

Page({
  data: {
    allList: [],
    list: [],
    currentTab: 0,
    tabs: ['全部', '待审核', '已通过', '已驳回'],
    statusMap: {
      1: { label: '待审核', class: 'pending' },
      2: { label: '已通过', class: 'approved' },
      3: { label: '已驳回', class: 'rejected' }
    },
    typeMap: {
      1: '道路测试',
      2: '示范应用',
      3: '应用试点'
    }
  },

  onLoad(options) {
    if (options.tab !== undefined) {
      this.setData({ currentTab: parseInt(options.tab) });
    }
  },

  onShow() {
    this.fetchList();
  },

  async fetchList() {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await request.get('/roadApplication/myList');
      
      let rawList = [];
      if (res.code === 200 && res.data) {
        rawList = res.data;
      } else {
        rawList = this.getMockList();
      }
      
      const list = rawList.map(item => ({
        ...item,
        statusLabel: this.data.statusMap[item.status]?.label || '未知',
        typeLabel: this.data.typeMap[item.type] || '其他',
        createTime: item.createTime ? item.createTime.split(' ')[0] : ''
      }));

      this.setData({ allList: list });
      this.filterList();
    } catch (err) {
      console.error('Fetch list failed', err);
      // Mock 数据供预览
      this.setData({ allList: this.getMockList() });
      this.filterList();
    } finally {
      wx.hideLoading();
    }
  },

  getMockList() {
    return [
      { id: 101, vehicleBrand: '宝马', vehicleModel: '宝马001', vin: '123123', type: 1, typeLabel: '道路测试', status: 1, statusLabel: '待审核', createTime: '2026-05-29', inspectionStatus: 1, plateStatus: 0, officerName: '张三' },
      { id: 102, vehicleBrand: '奔驰', vehicleModel: 'bc001', vin: '123', type: 1, typeLabel: '道路测试', status: 2, statusLabel: '已通过', createTime: '2026-05-28', inspectionStatus: 2, plateStatus: 1, officerName: '李四' },
      { id: 103, vehicleBrand: '智行者', vehicleModel: 'A1', vin: '456789', type: 1, typeLabel: '道路测试', status: 3, statusLabel: '已驳回', createTime: '2026-05-20', rejectReason: '车辆检测报告不完整，请补充上传。', officerName: '王五' }
    ];
  },

  onTabChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentTab: index });
    this.filterList();
  },

  filterList() {
    const { currentTab, allList } = this.data;
    let filteredList = [...allList];
    
    if (currentTab === 1) {
      // 待审核
      filteredList = allList.filter(item => item.status === 1);
    } else if (currentTab === 2) {
      // 已通过
      filteredList = allList.filter(item => item.status === 2);
    } else if (currentTab === 3) {
      // 已驳回
      filteredList = allList.filter(item => item.status === 3);
    }
    
    this.setData({ list: filteredList });
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
