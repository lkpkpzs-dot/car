const request = require('../../../utils/request.js');
const { formatRelativeTime } = require('../../../utils/util.js');

Page({
  data: {
    allList: [],
    list: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部', '待处理', '已处理', '待民警审核'],
    statusMap: {
      0: { label: '待核实', className: 'pending' },
      1: { label: '企业处理中', className: 'processing' },
      2: { label: '已处理', className: 'approved' },
      3: { label: '无效举报', className: 'rejected' },
      4: { label: '待民警审核', className: 'escalated' }
    },
    riskLevelMap: {
      1: { label: '低风险', className: 'low' },
      2: { label: '高风险', className: 'high' }
    },
    reportTypeMap: {
      1: '违规占道',
      2: '乱停乱放',
      3: '违规行驶',
      4: '意见建议'
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
    this.loadList();
  },

  safeParseImages(value) {
    if (!value) return [];
    if (Array.isArray(value)) return value.filter(Boolean);
    if (typeof value !== 'string') return [];

    try {
      const parsed = JSON.parse(value.trim());
      if (Array.isArray(parsed)) return parsed.filter(Boolean);
      if (typeof parsed === 'string' && parsed) return [parsed];
      return [];
    } catch (error) {
      return value
        .split(',')
        .map(item => item.replace(/[\[\]"]/g, '').trim())
        .filter(Boolean);
    }
  },

  normalizeLocation(value) {
    if (!value || typeof value !== 'string') return value || '';
    try {
      const location = JSON.parse(value);
      return location.address || value;
    } catch (error) {
      return value;
    }
  },

  async loadList() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    try {
      const res = await request.get('/citizenReport/enterpriseList', {});

      if (res.code !== 200) {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
        return;
      }

      let rawList = request.parseListData(res);
      
      const allList = rawList.map(item => {
        const processStatus = Number(item.processStatus);
        const statusMeta = this.data.statusMap[processStatus] || {
          label: '未知状态',
          className: 'unknown'
        };
        
        const riskLevel = Number(item.riskLevel);
        const riskMeta = this.data.riskLevelMap[riskLevel] || {
          label: '未知等级',
          className: 'unknown'
        };

        return {
          ...item,
          processStatus,
          riskLevel,
          reportTypeName: this.data.reportTypeMap[item.reportType] || '其他',
          statusLabel: statusMeta.label,
          statusClass: statusMeta.className,
          riskLabel: riskMeta.label,
          riskClass: riskMeta.className,
          locationText: this.normalizeLocation(item.locationExt),
          createTimeText: formatRelativeTime(item.reportCreateTime || item.createTime),
          images: this.safeParseImages(item.evidenceJson)
        };
      });

      this.setData({ allList });
      this.filterList();
    } catch (error) {
      console.error('加载举报记录失败:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  onTabChange(e) {
    const currentTab = Number(e.currentTarget.dataset.index);
    this.setData({ currentTab });
    this.filterList();
  },

  filterList() {
    const { currentTab, allList } = this.data;
    let list = [];
    
    const validList = Array.isArray(allList) ? allList : [];
    
    if (currentTab === 0) {
      list = validList;
    } else if (currentTab === 1) {
      list = validList.filter(item => item.processStatus === 1);
    } else if (currentTab === 2) {
      list = validList.filter(item => item.processStatus === 2);
    } else if (currentTab === 3) {
      list = validList.filter(item => item.processStatus === 4);
    }

    this.setData({ list });
  },

  onPreviewImage(e) {
    const { index, imageIndex } = e.currentTarget.dataset;
    const item = this.data.list[index];
    if (!item || !item.images || item.images.length === 0) return;

    wx.previewImage({
      urls: item.images,
      current: item.images[imageIndex || 0]
    });
  },

  onHandleReport(e) {
    const reportId = e.currentTarget.dataset.reportId;
    console.log('onHandleReport - reportId:', reportId, 'dataset:', e.currentTarget.dataset);
    const item = this.data.list.find(i => String(i.reportId) === String(reportId));
    if (item) {
      wx.setStorageSync('currentReportDetail', item);
    }
    wx.navigateTo({
      url: `/pages/enterprise/report/handle?reportId=${reportId}`
    });
  },

  onViewDetail(e) {
    const reportId = e.currentTarget.dataset.reportId;
    console.log('onViewDetail - reportId:', reportId, 'dataset:', e.currentTarget.dataset);
    const item = this.data.list.find(i => String(i.reportId) === String(reportId));
    if (item) {
      wx.setStorageSync('currentReportDetail', item);
    }
    wx.navigateTo({
      url: `/pages/enterprise/report/handle?reportId=${reportId}`
    });
  },

  onPullDownRefresh() {
    this.loadList().then(() => {
      wx.stopPullDownRefresh();
    });
  }
});
