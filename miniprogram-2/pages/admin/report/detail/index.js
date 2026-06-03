const request = require('../../../../utils/request.js');
const enterpriseUtil = require('../../../../utils/enterprise.js');

Page({
  data: {
    reportId: null,
    detail: null,
    loading: true,
    submitting: false,
    reportTypeMap: {
      1: '违规占道',
      2: '乱停乱放',
      3: '违规行驶',
      4: '意见建议'
    },
    processStatusMap: {
      0: '待核实',
      1: '已处理',
      2: '无效举报'
    },
    images: []
  },

  onLoad(options) {
    const reportId = options.reportId;
    if (!reportId) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    this.setData({ reportId });
    this.loadDetail();
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

  async loadDetail() {
    this.setData({ loading: true });

    try {
      // 调用详情接口
      const res = await request.get('/citizenReport/detail', { reportId: this.data.reportId });
      
      if (res.code === 200) {
        let detail = res.data;
        
        // 如果返回的是数组，取第一个元素
        if (Array.isArray(detail)) {
          detail = detail[0];
        }

        if (detail) {
          let images = this.safeParseJson(detail.evidenceJson);
          
          let locationExt = detail.locationExt;
          if (locationExt && typeof locationExt === 'string') {
            try {
              const locObj = JSON.parse(locationExt);
              if (locObj.address) {
                locationExt = locObj.address;
              }
            } catch (e) {
            }
          }

          this.setData({
            detail: { ...detail, locationExt },
            images
          });
        } else {
          wx.showToast({
            title: '举报信息不存在',
            icon: 'none'
          });
        }
      } else {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      urls: this.data.images,
      current: this.data.images[index]
    });
  },

  showReviewModal(processStatus) {
    if (this.data.submitting) return;

    const title = processStatus === 1 ? '处理通过' : '判定无效';
    wx.showModal({
      title,
      editable: true,
      placeholderText: '请输入审核备注（选填）',
      confirmText: '确认',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.submitReview(processStatus, res.content || '');
        }
      }
    });
  },

  async submitReview(processStatus, reviewRemark) {
    this.setData({ submitting: true });

    try {
      const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));

      const res = await request.put('/citizenReport/review', {
        reportId: this.data.reportId,
        processStatus,
        reviewRemark,
        reviewerId: userInfo.userId
      });

      if (res.code === 200) {
        wx.showToast({
          title: '审核成功',
          icon: 'success',
          duration: 1500
        });
        
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: res.msg || '审核失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.showToast({
        title: '审核失败',
        icon: 'none'
      });
    } finally {
      this.setData({ submitting: false });
    }
  },

  onApprove() {
    this.showReviewModal(1);
  },

  onReject() {
    this.showReviewModal(2);
  }
});
