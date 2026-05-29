const request = require('../../../utils/request.js');
const auth = require('../../../utils/auth.js');
const auditUtil = require('../../../utils/audit.js');

Page({
  data: {
    currentTab: 0,
    filterType: auditUtil.BUSINESS_TYPE.ENTERPRISE,
    keyword: '',
    rawList: [],
    list: [],
    pendingCount: 0,
    filterOptions: [
      { value: auditUtil.BUSINESS_TYPE.ENTERPRISE, label: '企业资质' },
      { value: auditUtil.BUSINESS_TYPE.PLATE, label: '上牌申请' }
    ]
  },

  onLoad() {
    this.fetchAuditList();
  },

  onShow() {
    this.fetchAuditList();
  },

  onPullDownRefresh() {
    this.fetchAuditList().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  switchTab(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10);
    this.setData({ currentTab: index }, () => {
      this.fetchAuditList();
    });
  },

  onFilterChange(e) {
    const filterType = parseInt(e.currentTarget.dataset.type, 10);
    this.setData({ filterType }, () => {
      this.fetchAuditList();
    });
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value }, () => {
      this.applyFilters();
    });
  },

  onSearchClear() {
    this.setData({ keyword: '' }, () => {
      this.applyFilters();
    });
  },

  applyFilters() {
    const { rawList, keyword } = this.data;
    const list = auditUtil.filterByKeyword(rawList, keyword);
    this.setData({ list });
  },

  buildListParams() {
    const isProcessed = this.data.currentTab === 1;
    const params = {
      isProcessed,
      businessType: this.data.filterType
    };

    if (isProcessed) {
      const reviewerId = auth.getReviewerId();
      if (reviewerId) {
        params.reviewerId = reviewerId;
      }
    }

    return params;
  },

  async fetchAuditList() {
    const isProcessed = this.data.currentTab === 1;
    wx.showLoading({ title: '加载中...' });

    try {
      const res = await request.get('/audit/list', this.buildListParams());
      const rawList = request.parseListData(res);

      this.setData({ rawList }, () => {
        this.applyFilters();
      });

      if (!isProcessed) {
        this.setData({ pendingCount: rawList.length });
      } else {
        this.refreshPendingCount();
      }
    } catch (err) {
      console.error('Fetch audit list failed:', err);
      wx.showToast({
        title: '加载失败',
        icon: 'none',
        duration: 2000
      });
      this.setData({
        rawList: isProcessed ? this.getMockProcessedList() : this.getMockPendingList(),
        pendingCount: isProcessed ? this.data.pendingCount : this.getMockPendingList().length
      }, () => {
        this.applyFilters();
      });
    } finally {
      wx.hideLoading();
    }
  },

  async refreshPendingCount() {
    try {
      const res = await request.get('/audit/list', {
        isProcessed: false,
        businessType: auditUtil.BUSINESS_TYPE.ENTERPRISE
      });
      const list = request.parseListData(res);
      this.setData({ pendingCount: list.length });
    } catch (err) {
      // 静默失败
    }
  },

  getMockPendingList() {
    return [
      {
        id: 10003,
        businessType: 2,
        title: '上海海纳百川商贸有限公司',
        statusDesc: '待审核',
        status: 0,
        createTime: '2026-05-25 16:21:00'
      }
    ];
  },

  getMockProcessedList() {
    return [
      {
        id: 10086,
        businessType: 2,
        title: '北京星辰创新科技有限公司',
        statusDesc: '通过',
        status: 1,
        createTime: '2024-03-15 09:30:00'
      }
    ];
  },

  goToDetail(e) {
    const item = e.currentTarget.dataset.item;
    const isProcessed = this.data.currentTab === 1;
    wx.navigateTo({
      url: `/pages/admin/audit/detail/index?applyId=${item.id}&businessType=${item.businessType}&title=${encodeURIComponent(item.title)}&statusDesc=${encodeURIComponent(item.statusDesc || '')}&isProcessed=${isProcessed ? 1 : 0}`
    });
  }
});
