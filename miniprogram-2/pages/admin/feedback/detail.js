const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');
const { formatRelativeTime } = require('../../../utils/util.js');

Page({
  data: {
    feedbackId: null,
    detail: null,
    images: [],
    loading: true,
    submitting: false,
    handleRemark: '',
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

  onLoad(options) {
    const feedbackId = options.feedbackId;
    if (!feedbackId) {
      wx.showToast({
        title: '参数错误',
        icon: 'none'
      });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    this.setData({ feedbackId });
    this.loadDetail();
  },

  safeParseImages(str) {
    if (!str) return [];
    if (Array.isArray(str)) return str.filter(Boolean);

    if (typeof str !== 'string') return [];

    let text = str.trim();
    if (!text) return [];

    // 移除多余的反引号
    text = text.replace(/`/g, '');

    try {
      const parsed = JSON.parse(text);
      if (Array.isArray(parsed)) return parsed.filter(Boolean);
      if (typeof parsed === 'string' && parsed) return [parsed];
      return [];
    } catch (e) {
      return text
        .split(',')
        .map(item => item.replace(/[\[\]"]/g, '').trim())
        .filter(Boolean);
    }
  },

  async loadDetail() {
    this.setData({ loading: true });

    try {
      const res = await request.get(`/feedback/admin/detail?feedbackId=${this.data.feedbackId}`);
      
      if (res.code === 200 && res.data) {
        const detail = res.data;
        const images = this.safeParseImages(detail.images);
        
        // 处理后端返回的数字状态
        let status = detail.processStatus !== undefined ? detail.processStatus : detail.status;
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

        this.setData({
          detail: { 
            ...detail, 
            feedbackId: detail.feedbackId,
            status,
            typeName: this.data.typeMap[detail.feedbackType] || detail.feedbackType || '其他',
            statusLabel: statusMeta.label,
            statusClass: statusMeta.className,
            createTime: formatRelativeTime(detail.createTime)
          },
          images,
          handleRemark: detail.processRemark || detail.handleRemark || ''
        });
      } else {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('Load detail failed:', error);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  onRemarkInput(e) {
    this.setData({ handleRemark: e.detail.value });
  },

  async submitHandle(status) {
    if (this.data.submitting) return;
    if (status !== 'processing' && !this.data.handleRemark.trim()) {
      wx.showToast({
        title: '请填写处理备注',
        icon: 'none'
      });
      return;
    }

    // 转换状态为后端期望的数字
    let processStatus;
    if (status === 'pending') {
      processStatus = 0;
    } else if (status === 'processing') {
      processStatus = 1;
    } else if (status === 'completed') {
      processStatus = 2;
    }

    this.setData({ submitting: true });

    try {
      const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));

      // 构建查询参数字符串
      const queryParts = [];
      queryParts.push(`feedbackId=${encodeURIComponent(this.data.feedbackId)}`);
      queryParts.push(`processStatus=${encodeURIComponent(processStatus)}`);
      if (this.data.handleRemark) {
        queryParts.push(`processRemark=${encodeURIComponent(this.data.handleRemark)}`);
      }
      queryParts.push(`handlerId=${encodeURIComponent(userInfo.userId)}`);
      const queryString = queryParts.join('&');

      const res = await request.post(
        `/feedback/admin/handle?${queryString}`,
        {} // 空对象，不传递 body
      );

      if (res.code === 200) {
        wx.showToast({
          title: '处理成功',
          icon: 'success',
          duration: 1500
        });
        
        setTimeout(() => {
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({
          title: res.msg || '处理失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('Handle failed:', error);
      wx.showToast({
        title: '处理失败',
        icon: 'none'
      });
    } finally {
      this.setData({ submitting: false });
    }
  },

  onStartProcessing() {
    wx.showModal({
      title: '开始处理',
      content: '确定要开始处理此反馈吗？',
      confirmText: '确认',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.submitHandle('processing');
        }
      }
    });
  },

  onComplete() {
    wx.showModal({
      title: '完成处理',
      content: '确定要标记为已处理吗？',
      confirmText: '确认',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.submitHandle('completed');
        }
      }
    });
  },

  onPreviewImage(e) {
    const index = e.currentTarget.dataset.index;
    wx.previewImage({
      urls: this.data.images,
      current: this.data.images[index]
    });
  }
});
