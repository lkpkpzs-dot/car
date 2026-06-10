const request = require('../../../../utils/request.js');

Page({
  data: {
    fullList: [], // 存储全量原始数据
    displayList: [], // 当前 Tab 显示的过滤数据
    loading: false,
    activeTab: 0, // 0: 未审核, 1: 已通过, 2: 已驳回
    tabs: ['未审核', '已通过', '已驳回'],
    typeMap: {
      1: '道路测试',
      2: '示范应用',
      3: '应用试点'
    },
    statusMap: {
      1: { label: '待审核', class: 'status-pending' },
      2: { label: '已通过', class: 'status-approved' },
      3: { label: '已驳回', class: 'status-rejected' }
    },
    inspectionStatusMap: {
      1: { label: '未查验', class: 'inspection-pending' },
      2: { label: '查验通过', class: 'inspection-approved' },
      3: { label: '查验驳回', class: 'inspection-rejected' }
    },
    plateStatusMap: {
      0: { label: '未发牌', class: 'plate-pending' },
      1: { label: '已发牌', class: 'plate-issued' }
    },
    plateTypeMap: {
      1: '道路测试牌照',
      2: '示范应用牌照',
      3: '应用试点牌照'
    }
  },

  onShow() {
    this.fetchList();
  },

  // 切换 Tab
  onTabChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ activeTab: index }, () => {
      this.filterList();
    });
  },

  async fetchList() {
    this.setData({ loading: true });
    wx.showLoading({ title: '加载中...' });
    
    try {
      // 获取全量数据
      const res = await request.get('/roadAudit/list');
      wx.hideLoading();
      
      const rawList = request.parseListData(res);
      const fullList = rawList.map(item => ({
        ...item,
        typeLabel: this.data.typeMap[item.type] || '其他',
        statusLabel: this.data.statusMap[item.status]?.label || '未知',
        statusClass: this.data.statusMap[item.status]?.class || '',
        inspectionStatusLabel: this.data.inspectionStatusMap[item.inspectionStatus]?.label || '未查验',
        inspectionStatusClass: this.data.inspectionStatusMap[item.inspectionStatus]?.class || '',
        plateStatusLabel: this.data.plateStatusMap[item.plateStatus]?.label || '未发牌',
        plateStatusClass: this.data.plateStatusMap[item.plateStatus]?.class || '',
        createTimeShort: item.createTime ? item.createTime.split(' ')[0] : ''
      }));

      this.setData({ fullList, loading: false }, () => {
        this.filterList();
      });
    } catch (err) {
      wx.hideLoading();
      this.setData({ loading: false });
      console.error('Fetch road audit list failed', err);
      // Mock 数据供预览
      const mockList = this.getMockList().map(item => ({
        ...item,
        statusLabel: this.data.statusMap[item.status]?.label || '未知',
        statusClass: this.data.statusMap[item.status]?.class || '',
        inspectionStatusLabel: this.data.inspectionStatusMap[item.inspectionStatus]?.label || '未查验',
        inspectionStatusClass: this.data.inspectionStatusMap[item.inspectionStatus]?.class || '',
        plateStatusLabel: this.data.plateStatusMap[item.plateStatus]?.label || '未发牌',
        plateStatusClass: this.data.plateStatusMap[item.plateStatus]?.class || '',
      }));
      this.setData({ fullList: mockList }, () => {
        this.filterList();
      });
    }
  },

  // 根据当前 Tab 过滤列表
  filterList() {
    const { fullList, activeTab } = this.data;
    let displayList = [];
    
    if (activeTab === 0) {
      // 未审核：status === 1
      displayList = fullList.filter(item => item.status === 1);
    } else if (activeTab === 1) {
      // 已通过：status === 2
      displayList = fullList.filter(item => item.status === 2);
    } else if (activeTab === 2) {
      // 已驳回：status === 3
      displayList = fullList.filter(item => item.status === 3);
    }
    
    this.setData({ displayList });
  },

  getMockList() {
    return [
      { id: 101, enterpriseId: 10088, enterpriseName: '广州海纳进出口贸易有限公司', type: 1, typeLabel: '道路测试', status: 1, vehicleBrand: '智行者', vehicleModel: 'A1', vin: 'LH1234567890ABCDE', createTimeShort: '2026-05-27', inspectionStatus: 1, plateStatus: 0, officerName: '张三' },
      { id: 102, enterpriseId: 10089, enterpriseName: '极速自动驾驶科技', type: 2, typeLabel: '示范应用', status: 1, vehicleBrand: '文远知行', vehicleModel: 'W1', vin: 'LH9876543210FEDCB', createTimeShort: '2026-05-26', inspectionStatus: 1, plateStatus: 0, officerName: '李四' },
      { id: 103, enterpriseId: 10088, enterpriseName: '广州海纳进出口贸易有限公司', type: 1, typeLabel: '道路测试', status: 2, vehicleBrand: '智行者', vehicleModel: 'A1', vin: 'VIN003_PASSED', createTimeShort: '2026-05-25', inspectionStatus: 2, plateStatus: 0, officerName: '王五' },
      { id: 104, enterpriseId: 10088, enterpriseName: '广州海纳进出口贸易有限公司', type: 1, typeLabel: '道路测试', status: 2, vehicleBrand: '智行者', vehicleModel: 'A1', vin: 'VIN004_INSPECTION_REJECTED', createTimeShort: '2026-05-24', inspectionStatus: 3, plateStatus: 0, officerName: '赵六' },
      { id: 105, enterpriseId: 10090, enterpriseName: '智驾科技有限公司', type: 3, typeLabel: '应用试点', status: 2, vehicleBrand: '百度阿波罗', vehicleModel: 'Robotaxi', vin: 'VIN005_PLATE_ISSUED', createTimeShort: '2026-05-20', inspectionStatus: 2, plateStatus: 1, officerName: '孙七' }
    ];
  },

  onDetail(e) {
    const { item } = e.currentTarget.dataset;
    console.log('[AuditList] Navigating to detail with item:', item);
    wx.navigateTo({
      url: `/pages/admin/road/detail/index?data=${encodeURIComponent(JSON.stringify(item))}`
    });
  },

  onInspect(e) {
    const { item } = e.currentTarget.dataset;
    console.log('[AuditList] Navigating to inspection page with item:', item);
    wx.navigateTo({
      url: `/pages/admin/road/inspection/index?data=${encodeURIComponent(JSON.stringify(item))}`
    });
  },

  onIssuePlate(e) {
    const { item } = e.currentTarget.dataset;
    console.log('[AuditList] Navigating to issue plate page with item:', item);
    wx.navigateTo({
      url: `/pages/admin/road/issue-plate/index?data=${encodeURIComponent(JSON.stringify(item))}`
    });
  },

  onPullDownRefresh() {
    this.fetchList().then(() => {
      wx.stopPullDownRefresh();
    });
  }
});
