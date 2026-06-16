const request = require('../../../utils/request.js');
const { formatRelativeTime } = require('../../../utils/util.js');
const reportStatus = require('../../../utils/reportStatus.js');

Page({
  data: {
    reportId: null,
    reportDetail: null,
    loading: false,
    submitting: false,
    remark: '',
    selectedStatus: null, // 2=已处理，3=无效举报
    reportTypeMap: {
      1: '违规占道',
      2: '乱停乱放',
      3: '违规行驶',
      4: '意见建议'
    },
    statusMap: reportStatus.getReportStatusLabelMap(),
    riskLevelMap: {
      1: '低风险',
      2: '高风险'
    }
  },

  onLoad(options) {
    console.log('handle.js onLoad options:', options);
    if (options.reportId) {
      this.setData({ reportId: options.reportId });
      console.log('设置 reportId 为:', options.reportId);
      this.loadDetail();
    }
  },

  async loadDetail() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    try {
      // 先尝试从本地存储获取数据（从列表页面传递过来）
      let reportDetail = wx.getStorageSync('currentReportDetail');
      
      // 检查数据是否匹配 reportId
      if (reportDetail && String(reportDetail.reportId) === String(this.data.reportId)) {
        // 数据已完整，直接使用
        this.setData({
          reportDetail: {
            ...reportDetail,
            createTimeText: reportDetail.createTimeText || formatRelativeTime(reportDetail.reportCreateTime || reportDetail.createTime)
          }
        });
        // 清除临时存储
        wx.removeStorageSync('currentReportDetail');
      } else {
        // 本地存储没有数据，尝试调用企业端专用接口
        // 如果没有企业详情接口，也可以尝试重新获取企业列表
        try {
          const res = await request.get('/citizenReport/enterpriseList', {});

          if (res.code === 200) {
            const rawList = request.parseListData(res);
            reportDetail = rawList.find(item => String(item.reportId) === String(this.data.reportId));
            
            if (reportDetail) {
              // 处理图片
              let images = [];
              if (reportDetail.evidenceJson) {
                try {
                  const parsed = JSON.parse(reportDetail.evidenceJson.trim());
                  if (Array.isArray(parsed)) {
                    images = parsed.filter(Boolean);
                  }
                } catch (e) {}
              }

              // 处理地址
              let locationText = reportDetail.locationExt;
              if (locationText && typeof locationText === 'string') {
                try {
                  const location = JSON.parse(locationText);
                  if (location.address) {
                    locationText = location.address;
                  }
                } catch (e) {}
              }

              this.setData({
                reportDetail: {
                  ...reportDetail,
                  images,
                  locationText,
                  createTimeText: formatRelativeTime(reportDetail.reportCreateTime || reportDetail.createTime)
                }
              });
            } else {
              throw new Error('未找到该举报');
            }
          } else {
            throw new Error(res.msg || '加载失败');
          }
        } catch (error) {
          throw error;
        }
      }
    } catch (error) {
      console.error('加载举报详情失败:', error);
      wx.showToast({
        title: error.message || '加载失败',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } finally {
      this.setData({ loading: false });
    }
  },

  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    if (this.data.reportDetail && this.data.reportDetail.images && this.data.reportDetail.images.length > 0) {
      wx.previewImage({
        urls: this.data.reportDetail.images,
        current: this.data.reportDetail.images[index]
      });
    }
  },

  onRemarkInput(e) {
    this.setData({ remark: e.detail.value });
  },

  onSelectStatus(e) {
    const status = parseInt(e.currentTarget.dataset.status);
    this.setData({ selectedStatus: status });
  },

  async onHandleSubmit() {
    if (this.data.submitting) return;
    
    if (!this.data.selectedStatus) {
      wx.showToast({
        title: '请选择处理结果',
        icon: 'none'
      });
      return;
    }
    
    if (!this.data.remark.trim()) {
      wx.showToast({
        title: '请填写处理说明',
        icon: 'none'
      });
      return;
    }

    // 确保 reportId 存在
    let reportId = this.data.reportId;
    if (!reportId && this.data.reportDetail) {
      reportId = this.data.reportDetail.reportId;
    }
    
    if (!reportId) {
      wx.showToast({
        title: '缺少举报ID',
        icon: 'none'
      });
      return;
    }

    this.setData({ submitting: true });

    console.log('提交处理参数:', {
      reportId: reportId,
      processStatus: this.data.selectedStatus,
      remark: this.data.remark
    });

    try {
      // 使用 JSON 格式发送请求
      const res = await request.post('/citizenReport/enterpriseHandle', {
        reportId: reportId,
        processStatus: this.data.selectedStatus,
        remark: this.data.remark
      });

      if (res.code === 200) {
        wx.showModal({
          title: '处理成功',
          content: '您的处理已提交',
          showCancel: false,
          success: () => {
            wx.navigateBack();
          }
        });
      } else {
        wx.showToast({
          title: res.msg || '处理失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('处理举报失败:', error);
      wx.showToast({
        title: '处理失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({ submitting: false });
    }
  }
});
