const request = require('../../../../utils/request.js');

Page({
  data: {
    list: [],
    loading: false,
    currentTab: 0, // 0: 未审核, 1: 已审核
    reportTypeMap: {
      1: '违规占道',
      2: '乱停乱放',
      3: '违规行驶',
      4: '意见建议'
    },
    processStatusMap: {
      0: '待审核',
      1: '已处理',
      2: '无效举报'
    }
  },

  onLoad() {
    this.loadList();
  },

  onShow() {
    this.loadList();
  },

  onTabChange(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ currentTab: index, list: [] });
    this.loadList();
  },

  safeParseJson(str) {
    if (!str) return [];
    try {
      let cleanStr = str.replace(/`/g, '').trim();
      return JSON.parse(cleanStr);
    } catch (e) {
      return [];
    }
  },

  async loadList() {
    if (this.data.loading) return;

    this.setData({ loading: true });

    try {
      // 获取所有数据，前端进行筛选
      const res = await request.get('/citizenReport/list', {});

      if (res.code === 200) {
        let rawList = request.parseListData(res);
        
        // 根据 Tab 进行筛选
        if (this.data.currentTab === 0) {
          // 未审核：只显示 processStatus === 0
          rawList = rawList.filter(item => item.processStatus === 0);
        } else {
          // 已审核：只显示 processStatus === 1 或 processStatus === 2
          rawList = rawList.filter(item => item.processStatus === 1 || item.processStatus === 2);
        }

        const list = rawList.map(item => {
          let images = this.safeParseJson(item.evidenceJson);
      
          let locationExt = item.locationExt;
          if (locationExt && typeof locationExt === 'string') {
            try {
              const locObj = JSON.parse(locationExt);
              if (locObj.address) {
                locationExt = locObj.address;
              }
            } catch (e) {
            }
          }
      
          return {
            ...item,
            reportTypeName: this.data.reportTypeMap[item.reportType] || '未知类型',
            createTime: this.formatTime(item.createTime),
            locationExt,
            images
          };
        });
      
        this.setData({ list });
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

  formatTime(timeStr) {
    if (!timeStr) return '';
    
    const date = new Date(timeStr);
    const now = new Date();
    const diff = now - date;
    
    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hour}:${minute}`;
  },

  onViewDetail(e) {
    const reportId = e.currentTarget.dataset.reportId;
    wx.navigateTo({
      url: `/pages/admin/report/detail/index?reportId=${reportId}`
    });
  },

  onPullDownRefresh() {
    this.loadList().then(() => {
      wx.stopPullDownRefresh();
    });
  }
});
