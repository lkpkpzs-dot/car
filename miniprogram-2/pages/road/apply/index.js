const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');

Page({
  data: {
    id: null,
    status: 0,
    readonly: false,
    rejectReason: '',
    
    typeOptions: [
      { value: 1, label: '道路测试' },
      { value: 2, label: '示范应用' },
      { value: 3, label: '应用试点' }
    ],
    typeIndex: 0,
    
    vehicleBrand: '',
    vehicleModel: '',
    vin: '',
    testArea: '',
    startDate: '',
    endDate: '',
    testProjects: '',
    supportPlan: '',
    
    // 材料字段（数组存储）
    docVehicleCert: [],
    docOwnerId: [],
    docSafetyInspection: [],
    docInsurance: [],
    docOwnerProxy: [],
    docAgentId: [],
    docSafetyDeclaration: [],
    docApplicationDoc: []
  },

  onLoad(options) {
    if (options.data) {
      try {
        const item = JSON.parse(decodeURIComponent(options.data));
        this.initFormData(item);
      } catch (e) {
        console.error('Parse data failed', e);
      }
    }
  },

  initFormData(item) {
    const typeIndex = this.data.typeOptions.findIndex(o => o.value === item.type);
    
    // 解析后端返回的 JSON 字符串材料
    const parseMaterial = (str) => {
      try {
        return typeof str === 'string' ? JSON.parse(str) : (Array.isArray(str) ? str : []);
      } catch (e) {
        return [];
      }
    };

    const status = item.status;
    const readonly = status === 1 || status === 2;

    this.setData({
      id: item.id,
      status: status,
      readonly: readonly,
      rejectReason: item.rejectReason || '',
      typeIndex: typeIndex > -1 ? typeIndex : 0,
      vehicleBrand: item.vehicleBrand || '',
      vehicleModel: item.vehicleModel || '',
      vin: item.vin || '',
      testArea: item.testArea || '',
      startDate: item.startDate || '',
      endDate: item.endDate || '',
      testProjects: item.testProjects || item.testProject || '',
      supportPlan: item.supportPlan || item.safetyPlan || '',
      docVehicleCert: parseMaterial(item.docVehicleCert),
      docOwnerId: parseMaterial(item.docOwnerId),
      docSafetyInspection: parseMaterial(item.docSafetyInspection),
      docInsurance: parseMaterial(item.docInsurance),
      docOwnerProxy: parseMaterial(item.docOwnerProxy),
      docAgentId: parseMaterial(item.docAgentId),
      docSafetyDeclaration: parseMaterial(item.docSafetyDeclaration),
      docApplicationDoc: parseMaterial(item.docApplicationDoc)
    });
  },

  onInput(e) {
    if (this.data.readonly) return;
    const { field } = e.currentTarget.dataset;
    this.setData({ [field]: e.detail.value });
  },

  onTypeChange(e) {
    this.setData({ typeIndex: e.detail.value });
  },

  onDateChange(e) {
    const { field } = e.currentTarget.dataset;
    this.setData({ [field]: e.detail.value });
  },

  async chooseImage(e) {
    const { field } = e.currentTarget.dataset;
    try {
      const res = await wx.chooseImage({
        count: 9,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera']
      });
      
      const tempPaths = res.tempFilePaths;
      wx.showLoading({ title: '上传中...' });
      
      const uploadTasks = tempPaths.map(path => request.uploadFile(path));
      const urls = await Promise.all(uploadTasks);
      
      wx.hideLoading();
      this.setData({
        [field]: [...this.data[field], ...urls]
      });
    } catch (err) {
      wx.hideLoading();
      console.error('Upload failed', err);
    }
  },

  removeImage(e) {
    const { field, index } = e.currentTarget.dataset;
    const list = [...this.data[field]];
    list.splice(index, 1);
    this.setData({ [field]: list });
  },

  previewImage(e) {
    const { urls, current } = e.currentTarget.dataset;
    wx.previewImage({
      current,
      urls
    });
  },

  onPreviewPermit() {
    wx.showModal({
      title: '提示',
      content: '您的申请已通过审核，测试许可证明正在生成中，您可以联系管理员获取纸质凭证。',
      showCancel: false,
      confirmText: '知道了'
    });
  },

  validate() {
    const d = this.data;
    if (!d.vehicleBrand || !d.vehicleModel || !d.vin || !d.testArea || !d.startDate || !d.endDate || !d.testProjects || !d.supportPlan) {
      wx.showToast({ title: '请填写完整信息', icon: 'none' });
      return false;
    }
    if (d.docVehicleCert.length === 0 || d.docOwnerId.length === 0 || d.docSafetyInspection.length === 0 || d.docInsurance.length === 0 || d.docSafetyDeclaration.length === 0 || d.docApplicationDoc.length === 0) {
      wx.showToast({ title: '请上传必要证明材料', icon: 'none' });
      return false;
    }
    return true;
  },

  async onSubmit() {
    if (!this.validate()) return;
    
    const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
    const applicantId = userInfo.userId;
    const enterpriseId = userInfo.authEnterpriseId;

    if (!applicantId || !enterpriseId) {
      wx.showToast({ title: '用户信息不完整，请重新登录', icon: 'none' });
      return;
    }

    wx.showLoading({ title: '提交中...' });
    const d = this.data;
    const payload = {
      id: d.id, // 如果是重新提交，带上 ID
      applicantId,
      enterpriseId,
      type: d.typeOptions[d.typeIndex].value,
      vehicleBrand: d.vehicleBrand,
      vehicleModel: d.vehicleModel,
      vin: d.vin,
      testArea: d.testArea,
      startDate: d.startDate,
      endDate: d.endDate,
      testProjects: d.testProjects,
      supportPlan: d.supportPlan,
      // 提交时直接发送数组，后端 DTO 配置 List<String>
      docVehicleCert: d.docVehicleCert,
      docOwnerId: d.docOwnerId,
      docSafetyInspection: d.docSafetyInspection,
      docInsurance: d.docInsurance,
      docOwnerProxy: d.docOwnerProxy,
      docAgentId: d.docAgentId,
      docSafetyDeclaration: d.docSafetyDeclaration,
      docApplicationDoc: d.docApplicationDoc
    };

    try {
      const res = await request.post('/roadApplication/apply', payload);
      wx.hideLoading();
      if (res.code === 200) {
        wx.showToast({ title: '提交成功', icon: 'success' });
        setTimeout(() => wx.navigateBack(), 1500);
      } else {
        wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      wx.showToast({ title: '网络异常', icon: 'none' });
    }
  }
});
