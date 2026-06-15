const request = require('../../../../utils/request.js');
const { formatRelativeTime } = require('../../../../utils/util.js');

Page({
  data: {
    allList: [],
    list: [],
    loading: false,
    currentTab: 0, // 0: 未审核, 1: 企业审核, 2: 民警审核
    tabs: ['未审核', '企业审核', '民警审核'],
    reportTypeMap: {
      1: '违规占道',
      2: '乱停乱放',
      3: '违规行驶',
      4: '意见建议'
    },
    processStatusMap: {
      0: '待核实',
      1: '企业处理中',
      2: '已处理',
      3: '无效举报',
      4: '待民警审核'
    },
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
      // 获取所有数据
      const res = await request.get('/citizenReport/list', {});

      if (res.code === 200) {
        let rawList = request.parseListData(res);
        
        // 处理所有数据
        const processedList = rawList.map(item => {
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
            reportTypeName: this.data.reportTypeMap[item.reportType] || '未知类型',
            createTime: formatRelativeTime(item.createTime || item.reportCreateTime),
            statusLabel: statusMeta.label,
            statusClass: statusMeta.className,
            riskLabel: riskMeta.label,
            riskClass: riskMeta.className,
            locationExt,
            images
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
      // 未审核：显示待核实、待民警审核，以及高风险企业处理中
      list = validList.filter(item => item && (
        item.processStatus === 0 || 
        item.processStatus === 4 || 
        (item.processStatus === 1 && item.riskLevel === 2)
      ));
    } else if (currentTab === 1) {
      // 企业审核：显示企业处理中
      list = validList.filter(item => item && item.processStatus === 1);
    } else if (currentTab === 2) {
      // 民警审核：显示已处理或无效举报
      list = validList.filter(item => item && (item.processStatus === 2 || item.processStatus === 3));
    }

    this.setData({ list });
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
