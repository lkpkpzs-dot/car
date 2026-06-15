const request = require('../../../../utils/request.js');
const enterpriseUtil = require('../../../../utils/enterprise.js');
const { formatRelativeTime } = require('../../../../utils/util.js');

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
      const res = await request.get('/citizenReport/list', {});
      
      if (res.code === 200) {
        let rawList = request.parseListData(res);
        let detail = rawList.find(item => String(item.reportId) === String(this.data.reportId));

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

          const processStatus = Number(detail.processStatus);
          const statusMeta = this.data.statusMap[processStatus] || {
            label: '未知状态',
            className: 'unknown'
          };

          const riskLevel = Number(detail.riskLevel);
          const riskMeta = this.data.riskLevelMap[riskLevel] || {
            label: '未知等级',
            className: 'unknown'
          };

          this.setData({
            detail: { 
              ...detail, 
              locationExt, 
              statusLabel: statusMeta.label,
              statusClass: statusMeta.className,
              riskLabel: riskMeta.label,
              riskClass: riskMeta.className,
              createTime: formatRelativeTime(detail.createTime || detail.reportCreateTime)
            },
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

    const title = processStatus === 2 ? '处理通过' : '判定无效';
    wx.showModal({
      title,
      content: `确定要${title}吗？`,
      confirmText: '确认',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.submitReview(processStatus);
        }
      }
    });
  },

  async submitReview(processStatus) {
    this.setData({ submitting: true });

    try {
      const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));

      const res = await request.put('/citizenReport/review', {
        reportId: this.data.reportId,
        processStatus,
        reviewRemark: '',
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
    this.showReviewModal(2);
  },

  onReject() {
    this.showReviewModal(3);
  }
});
