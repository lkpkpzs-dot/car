const request = require('../../../utils/request.js');

const PLATE_TYPE_MAP = {
  1: '道路测试',
  2: '示范应用',
  3: '应用试点'
};

const STATUS_MAP = {
  1: { label: '正常营运', className: 'status-normal' },
  2: { label: '异常整改', className: 'status-warning' },
  3: { label: '已吊销', className: 'status-revoked' }
};

Page({
  data: {
    loading: false,
    keyword: '',
    activePlateType: 0,
    activeStatus: 0,
    plateTabs: [
      { value: 0, label: '全部号牌' },
      { value: 1, label: '道路测试' },
      { value: 2, label: '示范应用' },
      { value: 3, label: '应用试点' }
    ],
    statusFilters: [
      { value: 0, label: '全部' },
      { value: 1, label: '正常' },
      { value: 2, label: '整改' },
      { value: 3, label: '吊销' }
    ],
    archives: [],
    displayArchives: [],
    selectedArchive: null,
    stats: {
      total: 0,
      normal: 0,
      warning: 0,
      revoked: 0
    }
  },

  onLoad(options) {
    if (options.vin) {
      this.setData({ keyword: options.vin });
    }
    this.fetchArchives();
  },

  async fetchArchives() {
    this.setData({ loading: true });
    wx.showLoading({ title: '加载中...' });

    try {
      const res = await request.get('/carArchive/list');
      const list = request.parseListData(res);
      wx.hideLoading();
      this.applyArchiveData(list.length ? list : this.getMockArchives());
    } catch (err) {
      wx.hideLoading();
      console.error('Fetch car archive list failed:', err);
      this.applyArchiveData(this.getMockArchives());
      wx.showToast({ title: '已展示本地样例数据', icon: 'none' });
    }
  },

  applyArchiveData(list) {
    const archives = list.map(item => this.normalizeArchive(item));
    const stats = {
      total: archives.length,
      normal: archives.filter(item => item.status === 1).length,
      warning: archives.filter(item => item.status === 2).length,
      revoked: archives.filter(item => item.status === 3).length
    };

    this.setData({ archives, stats, loading: false }, () => {
      this.filterArchives();
    });
  },

  normalizeArchive(item) {
    const statusInfo = STATUS_MAP[item.status] || { label: '未知状态', className: 'status-unknown' };
    const techParams = this.parseJsonLike(item.techParams);
    const images = this.parseJsonLike(item.imagesJson);
    const photoList = Array.isArray(images) ? images : Object.keys(images || {}).map(key => images[key]).filter(Boolean);
    const mileage = item.totalMileage === undefined || item.totalMileage === null ? '0' : String(item.totalMileage);

    return {
      ...item,
      title: `${item.vehicleBrand || '未知品牌'} ${item.vehicleModel || ''}`.trim(),
      plateTypeLabel: PLATE_TYPE_MAP[item.currentPlateType] || '未发牌',
      plateNumberText: item.plateNumber || '未发牌',
      statusLabel: statusInfo.label,
      statusClass: statusInfo.className,
      totalMileageText: `${mileage} km`,
      violationText: `${item.violationCount || 0} 次`,
      techParamList: this.toKeyValueList(techParams),
      photoList,
      createTimeText: this.shortTime(item.createTime),
      updateTimeText: this.shortTime(item.updateTime)
    };
  },

  parseJsonLike(value) {
    if (!value) return {};
    if (typeof value === 'object') return value;
    try {
      return JSON.parse(value);
    } catch (e) {
      return {};
    }
  },

  toKeyValueList(value) {
    if (!value || Array.isArray(value)) return [];
    return Object.keys(value).map(key => ({
      key,
      value: value[key]
    }));
  },

  shortTime(value) {
    if (!value) return '-';
    return String(value).replace('T', ' ').slice(0, 16);
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value }, () => {
      this.filterArchives();
    });
  },

  onClearKeyword() {
    this.setData({ keyword: '' }, () => {
      this.filterArchives();
    });
  },

  onPlateTabChange(e) {
    const value = Number(e.currentTarget.dataset.value);
    this.setData({ activePlateType: value }, () => {
      this.filterArchives();
    });
  },

  onStatusFilterChange(e) {
    const value = Number(e.currentTarget.dataset.value);
    this.setData({ activeStatus: value }, () => {
      this.filterArchives();
    });
  },

  filterArchives() {
    const keyword = this.data.keyword.trim().toLowerCase();
    const { activePlateType, activeStatus, archives } = this.data;

    const displayArchives = archives.filter(item => {
      const matchesKeyword = !keyword || [
        item.vin,
        item.plateNumber,
        item.vehicleBrand,
        item.vehicleModel,
        String(item.enterpriseId || '')
      ].some(value => String(value || '').toLowerCase().includes(keyword));
      const matchesPlate = activePlateType === 0 || item.currentPlateType === activePlateType;
      const matchesStatus = activeStatus === 0 || item.status === activeStatus;
      return matchesKeyword && matchesPlate && matchesStatus;
    });

    this.setData({
      displayArchives,
      selectedArchive: displayArchives[0] || null
    });
  },

  onSelectArchive(e) {
    const vin = e.currentTarget.dataset.vin;
    const selectedArchive = this.data.archives.find(item => item.vin === vin);
    if (selectedArchive) {
      this.setData({ selectedArchive });
    }
  },

  onCopyVin() {
    if (!this.data.selectedArchive) return;
    wx.setClipboardData({
      data: this.data.selectedArchive.vin,
      success: () => wx.showToast({ title: 'VIN已复制', icon: 'success' })
    });
  },

  onRefresh() {
    this.fetchArchives();
  },

  onPullDownRefresh() {
    this.fetchArchives().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  getMockArchives() {
    return [
      {
        vin: 'LKP202605280001ABCD',
        applicationId: 2026052701,
        vehicleInfoId: 88001,
        enterpriseId: 10088,
        vehicleBrand: '智行者',
        vehicleModel: 'A1 低速无人配送车',
        currentPlateType: 1,
        plateNumber: '测A-0018',
        status: 1,
        totalMileage: 12860.5,
        violationCount: 0,
        techParams: JSON.stringify({ 自动驾驶等级: 'L4', 核载: '200kg', 最高时速: '45km/h', 传感器: '激光雷达/毫米波雷达/摄像头' }),
        imagesJson: JSON.stringify(['/assets/images/car-online.png', '/assets/images/vehicle-icon.png']),
        createTime: '2026-05-20 09:30:00',
        updateTime: '2026-05-28 10:12:00'
      },
      {
        vin: 'LKP202605260002WXYZ',
        applicationId: 2026052508,
        vehicleInfoId: 88012,
        enterpriseId: 10089,
        vehicleBrand: '文远知行',
        vehicleModel: 'Mini Robobus',
        currentPlateType: 2,
        plateNumber: '示B-0216',
        status: 2,
        totalMileage: 6422,
        violationCount: 2,
        techParams: JSON.stringify({ 自动驾驶等级: 'L4', 核载: '8人', 最高时速: '40km/h', 运行区域: '示范应用区' }),
        imagesJson: JSON.stringify(['/assets/images/car-warning.png']),
        createTime: '2026-05-18 14:20:00',
        updateTime: '2026-05-27 16:45:00'
      },
      {
        vin: 'LKP202604180003MNOP',
        applicationId: 2026041503,
        vehicleInfoId: 87936,
        enterpriseId: 10090,
        vehicleBrand: '百度阿波罗',
        vehicleModel: 'Robotaxi',
        currentPlateType: 3,
        plateNumber: '试C-0309',
        status: 3,
        totalMileage: 20318.75,
        violationCount: 5,
        techParams: JSON.stringify({ 自动驾驶等级: 'L4', 核载: '4人', 最高时速: '60km/h', 远程接管: '支持' }),
        imagesJson: JSON.stringify(['/assets/images/car-offline.png']),
        createTime: '2026-04-18 08:10:00',
        updateTime: '2026-05-21 11:00:00'
      }
    ];
  }
});
