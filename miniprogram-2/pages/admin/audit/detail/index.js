const request = require('../../../../utils/request.js');
const auditUtil = require('../../../../utils/audit.js');

Page({
  data: {
    applyId: null,
    businessType: null,
    title: '',
    statusDesc: '',
    isProcessed: false,
    history: [],
    showModal: false,
    reason: '',
    auditAction: null
  },

  onLoad(options) {
    const { applyId, businessType, title, statusDesc, isProcessed } = options;
    this.setData({
      applyId,
      businessType: parseInt(businessType, 10),
      title: decodeURIComponent(title || ''),
      statusDesc: decodeURIComponent(statusDesc || ''),
      isProcessed: isProcessed === '1'
    });
    this.fetchHistory();
  },

  parseHistoryResponse(res) {
    if (!res) return [];
    if (Array.isArray(res)) return res;
    if (res.code === 200 && Array.isArray(res.data)) return res.data;
    if (Array.isArray(res.data)) return res.data;
    return [];
  },

  async fetchHistory() {
    const { applyId, businessType } = this.data;
    try {
      const res = await request.get('/approvalRecord/history', { applyId, businessType });
      const history = this.parseHistoryResponse(res).map(item => ({
        ...item,
        actionLabel: auditUtil.getActionTypeLabel(item.actionType),
        actionColor: auditUtil.getActionTypeColor(item.actionType)
      }));
      this.setData({ history });
    } catch (err) {
      console.error('Fetch history failed:', err);
      wx.showToast({
        title: '加载历程失败',
        icon: 'none',
        duration: 2000
      });
      this.setData({
        history: this.getMockHistory(applyId, businessType)
      });
    }
  },

  getMockHistory(applyId, businessType) {
    const raw = [
      {
        recordId: 50003,
        applyId,
        businessType,
        nodeName: '民警审核',
        reviewerId: 30002,
        actionType: auditUtil.ACTION_TYPE.PASS,
        comment: '合理',
        createTime: '2026-05-25 15:12:00'
      },
      {
        recordId: 50002,
        applyId,
        businessType,
        nodeName: '资质审核',
        reviewerId: 30002,
        actionType: auditUtil.ACTION_TYPE.SUBMIT,
        comment: '审核一下',
        createTime: '2026-05-25 14:21:48'
      }
    ];
    return raw.map(item => ({
      ...item,
      actionLabel: auditUtil.getActionTypeLabel(item.actionType),
      actionColor: auditUtil.getActionTypeColor(item.actionType)
    }));
  },

  onApprove() {
    this.setData({
      showModal: true,
      auditAction: auditUtil.ACTION_TYPE.PASS,
      reason: ''
    });
  },

  onReject() {
    this.setData({
      showModal: true,
      auditAction: auditUtil.ACTION_TYPE.REJECT,
      reason: ''
    });
  },

  hideModal() {
    this.setData({ showModal: false });
  },

  onReasonInput(e) {
    this.setData({ reason: e.detail.value });
  },

  async submitAudit() {
    const { applyId, businessType, auditAction, reason } = this.data;

    if (auditAction === auditUtil.ACTION_TYPE.REJECT && !reason.trim()) {
      wx.showToast({ title: '请输入驳回原因', icon: 'none' });
      return;
    }

    if (businessType !== auditUtil.BUSINESS_TYPE.ENTERPRISE) {
      wx.showToast({ title: '当前仅支持企业资质审核', icon: 'none' });
      return;
    }

    // 获取审核人ID
    const userInfo = wx.getStorageSync('userInfo');
    const reviewerId = userInfo?.userId;

    // 根据 actionType 转换为 auditStatus
    let auditStatus;
    if (auditAction === auditUtil.ACTION_TYPE.PASS) {
      auditStatus = 1; // 通过
    } else if (auditAction === auditUtil.ACTION_TYPE.REJECT) {
      auditStatus = 2; // 驳回
    }

    wx.showLoading({ title: '提交中...' });
    try {
      const res = await request.put('/enterpriseInfo/audit', {
        enterpriseId: parseInt(applyId, 10),
        reviewerId: reviewerId,
        auditStatus: auditStatus,
        reason: reason.trim()
      });

      wx.hideLoading();

      if (res.code !== undefined && res.code !== 200) {
        wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
        return;
      }

      wx.showToast({ title: '处理成功', icon: 'success' });
      this.setData({ showModal: false, isProcessed: true });

      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (err) {
      wx.hideLoading();
      console.error('Submit enterprise audit failed:', err);
      wx.showToast({ title: '提交失败，请重试', icon: 'none' });
    }
  }
});
