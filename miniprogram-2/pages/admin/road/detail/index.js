const request = require('../../../../utils/request.js');

Page({
  data: {
    id: null,
    info: {},
    materials: [],
    showModal: false,
    modalType: '', // 'pass' or 'reject'
    rejectReason: '',
    auditComment: '',
    typeMap: {
      1: '道路测试',
      2: '示范应用',
      3: '应用试点'
    }
  },

  onLoad(options) {
    if (options.data) {
      try {
        const item = JSON.parse(decodeURIComponent(options.data));
        console.log('[AuditDetail] Received raw item:', item);
        
        // 兼容处理所有可能的字段名（驼峰和下划线）
        const testProject = item.testProjects || item.test_projects || item.testProject || item.test_project || '';
        const safetyPlan = item.supportPlan || item.support_plan || item.safetyPlan || item.safety_plan || '';
        
        console.log('[AuditDetail] Extracted testProject:', testProject);
        console.log('[AuditDetail] Extracted safetyPlan:', safetyPlan);

        this.setData({
          id: item.id || item.applicationId,
          info: item,
          testProject, // 直接存入 data，方便 WXML 访问
          safetyPlan   // 直接存入 data
        });
        this.parseMaterials(item);
      } catch (e) {
        console.error('Parse detail data failed', e);
        wx.showToast({ title: '数据解析失败', icon: 'none' });
      }
    }
  },

  parseMaterials(item) {
    const materialFields = [
      { key: 'docVehicleCert', label: '整车合格证' },
      { key: 'docOwnerId', label: '所有人证明' },
      { key: 'docSafetyInspection', label: '安全检验报告' },
      { key: 'docInsurance', label: '交强险证明' },
      { key: 'docOwnerProxy', label: '委托书' },
      { key: 'docAgentId', label: '代理人身份证' },
      { key: 'docSafetyDeclaration', label: '安全声明' },
      { key: 'docApplicationDoc', label: '申请书' }
    ];

    const parseJson = (str) => {
      try {
        if (!str) return [];
        // 处理已经是数组的情况，或者解析 JSON 字符串
        const res = typeof str === 'string' ? JSON.parse(str) : (Array.isArray(str) ? str : []);
        return Array.isArray(res) ? res : [res];
      } catch (e) {
        console.error('Parse JSON failed for material:', str, e);
        return [];
      }
    };

    const materials = materialFields.map(field => {
      // 同时尝试 camelCase 和 snake_case
      const value = item[field.key] || item[field.key.replace(/[A-Z]/g, letter => `_${letter.toLowerCase()}`)];
      return {
        label: field.label,
        urls: parseJson(value)
      };
    }).filter(m => m.urls.length > 0);

    this.setData({ materials });
  },

  previewImage(e) {
    const { current, urls } = e.currentTarget.dataset;
    wx.previewImage({
      current,
      urls
    });
  },

  onApprove() {
    this.setData({
      showModal: true,
      modalType: 'pass',
      auditComment: '',
      rejectReason: ''
    });
  },

  onReject() {
    this.setData({
      showModal: true,
      modalType: 'reject',
      auditComment: '',
      rejectReason: ''
    });
  },

  hideModal() {
    this.setData({ showModal: false });
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [field]: e.detail.value });
  },

  async submitAudit() {
    const { id, modalType, rejectReason, auditComment } = this.data;
    const reviewerId = wx.getStorageSync('userInfo')?.userId || 10001; // 从缓存获取民警ID

    if (modalType === 'reject' && !rejectReason.trim()) {
      wx.showToast({ title: '请输入驳回原因', icon: 'none' });
      return;
    }

    const status = modalType === 'pass' ? 2 : 3;
    const payload = {
      applicationId: id,
      reviewerId: reviewerId,
      status: status,
      auditComment: auditComment.trim(),
      rejectReason: modalType === 'pass' ? '' : rejectReason.trim()
    };

    wx.showLoading({ title: '提交中...' });
    try {
      const res = await request.put('/roadApplication/audit', payload);
      wx.hideLoading();

      if (res.code === 200) {
        wx.showToast({ title: '操作成功', icon: 'success' });
        this.setData({ showModal: false });
        setTimeout(() => {
          // 返回列表并刷新
          const pages = getCurrentPages();
          const prevPage = pages[pages.length - 2];
          if (prevPage && prevPage.fetchList) {
            prevPage.fetchList();
          }
          wx.navigateBack();
        }, 1500);
      } else {
        wx.showToast({ title: res.msg || '操作失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('Submit audit failed:', err);
      wx.showToast({ title: '提交失败，请重试', icon: 'none' });
    }
  }
});
