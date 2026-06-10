const request = require('../../../utils/request.js');
const { formatRelativeTime } = require('../../../utils/util.js');

Page({
  data: {
    allList: [],
    list: [],
    loading: false,
    currentTab: 0, // 0: 待处理, 1: 处理中, 2: 已处理
    tabs: ['待处理', '处理中', '已处理'],
    typeMap: {
      1: '功能建议',
      2: 'Bug反馈',
      3: '其他',
      'suggestion': '功能建议',
      'bug': 'Bug反馈',
      'other': '其他'
    },
    statusMap: {
      'pending': { label: '待处理', className: 'pending' },
      'processing': { label: '处理中', className: 'processing' },
      'completed': { label: '已处理', className: 'approved' }
    }
  },

  onLoad() {
    this.loadList();
  },

  onShow() {
    this.loadList();
  },

  onTabChange(e) {
    const index = parseInt(e.currentTarget.dataset.index, 10);
    this.setData({ currentTab: index });
    this.filterList();
  },

  async loadList() {
    if (this.data.loading) return;

    this.setData({ loading: true });

    try {
      // 获取所有意见建议列表
      const res = await request.get('/feedback/admin/list', {});

      if (res.code === 200) {
        let rawList = request.parseListData(res);
        
        // 处理所有数据
        const processedList = rawList.map(item => {
          // 处理后端返回的数字状态
          let status = item.processStatus !== undefined ? item.processStatus : item.status;
          // 支持两种格式：数字或字符串
          if (typeof status === 'number') {
            if (status === 0) status = 'pending';
            else if (status === 1) status = 'processing';
            else if (status === 2) status = 'completed';
          }
          status = status || 'pending';
          
          const statusMeta = this.data.statusMap[status] || {
            label: '未知状态',
            className: 'unknown'
          };

          return {
            ...item,
            feedbackId: item.feedbackId,
            status,
            typeName: this.data.typeMap[item.feedbackType] || item.feedbackType || '其他',
            createTime: formatRelativeTime(item.createTime),
            statusLabel: statusMeta.label,
            statusClass: statusMeta.className
          };
        });
        
        this.setData({ allList: processedList });
        this.filterList();
      } else {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
      }
    } catch (error) {
      let errorMsg = '加载失败';
      if (error.errMsg) {
        errorMsg = error.errMsg;
      } else if (error.data && error.data.msg) {
        errorMsg = error.data.msg;
      } else if (error.msg) {
        errorMsg = error.msg;
      }
      wx.showToast({
        title: errorMsg,
        icon: 'none',
        duration: 3000
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  filterList() {
    const { currentTab, allList } = this.data;
    let list = [];
    
    const validList = Array.isArray(allList) ? allList : [];
    
    if (currentTab === 0) {
      list = validList.filter(item => item && item.status === 'pending');
    } else if (currentTab === 1) {
      list = validList.filter(item => item && item.status === 'processing');
    } else if (currentTab === 2) {
      list = validList.filter(item => item && item.status === 'completed');
    }

    this.setData({ list });
  },

  onViewDetail(e) {
    const feedbackId = e.currentTarget.dataset.feedbackId;
    wx.navigateTo({
      url: `/pages/admin/feedback/detail?feedbackId=${feedbackId}`
    });
  },

  onPullDownRefresh() {
    this.loadList().then(() => {
      wx.stopPullDownRefresh();
    });
  }
});
