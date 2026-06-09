const request = require('../../../utils/request.js');
const enterpriseUtil = require('../../../utils/enterprise.js');
const validator = require('../../../utils/validator.js');
const safetyOfficerUtil = require('../../../utils/safetyOfficer.js');

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
    
    // 安全员相关
    safetyOfficers: [],
    safetyOfficerIndex: 0,
    officerId: null,
    
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
    this.pendingItem = null;
    if (options.data) {
      try {
        const item = JSON.parse(decodeURIComponent(options.data));
        if (item.id) {
          this.fetchDetail(item.id);
        } else {
          this.pendingItem = item;
        }
      } catch (e) {
        console.error('Parse data failed', e);
      }
    }
    this.loadSafetyOfficers();
  },

  async fetchDetail(id) {
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await request.get(`/roadApplication/${id}`);
      wx.hideLoading();
      if (res.code === 200 && res.data) {
        this.pendingItem = res.data;
        this.initFormDataIfReady();
      }
    } catch (err) {
      wx.hideLoading();
      console.error('Fetch detail failed', err);
      wx.showToast({ title: '加载详情失败', icon: 'none' });
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
    
    // 计算安全员索引
    let safetyOfficerIndex = 0;
    if (item.officerId && this.data.safetyOfficers.length > 0) {
      const index = this.data.safetyOfficers.findIndex(o => o.value === item.officerId);
      if (index > -1) {
        safetyOfficerIndex = index;
      }
    }

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
      officerId: item.officerId || null,
      safetyOfficerIndex,
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

  initFormDataIfReady() {
    if (this.pendingItem && this.data.safetyOfficers.length > 0) {
      this.initFormData(this.pendingItem);
      this.pendingItem = null;
    }
  },

  async loadSafetyOfficers() {
    try {
      const res = await safetyOfficerUtil.getEnterpriseValidSafetyOfficers();
      if (res.code === 200 && res.data) {
        const officers = res.data.map(item => ({
          value: item.officerId,
          label: `${item.officerName} (${item.licenseType} - 已关联 ${item.totalVehicleCount || 0} 辆车)`
        }));
        // 添加默认选项
        const safetyOfficers = [{ value: null, label: '请选择安全员' }, ...officers];
        this.setData({ safetyOfficers });
        // 检查是否有待初始化的表单数据
        this.initFormDataIfReady();
      }
    } catch (error) {
      console.error('Load safety officers failed', error);
    }
  },

  onSafetyOfficerChange(e) {
    const index = e.detail.value;
    const officer = this.data.safetyOfficers[index];
    this.setData({
      safetyOfficerIndex: index,
      officerId: officer ? officer.value : null
    });
  },

  onInput(e) {
    if (this.data.readonly) return;
    const { field } = e.currentTarget.dataset;
    let value = e.detail.value;

    if (field === 'vin') {
      value = value.toUpperCase();
      if (value.length > 17) value = value.slice(0, 17);
    }
    if (field === 'vehicleBrand' || field === 'vehicleModel' || field === 'testArea') {
      if (validator.hasSpecialChars(value)) {
        wx.showToast({ title: '不能输入特殊符号', icon: 'none', duration: 1000 });
        return;
      }
    }

    this.setData({ [field]: value });
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
    
    if (!d.vehicleBrand) {
      wx.showToast({ title: '请填写车辆品牌', icon: 'none' });
      return false;
    }
    if (d.vehicleBrand.length < 1 || d.vehicleBrand.length > 50) {
      wx.showToast({ title: '车辆品牌长度需在1-50字之间', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(d.vehicleBrand)) {
      wx.showToast({ title: '车辆品牌不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!d.vehicleModel) {
      wx.showToast({ title: '请填写车辆型号', icon: 'none' });
      return false;
    }
    if (d.vehicleModel.length < 1 || d.vehicleModel.length > 50) {
      wx.showToast({ title: '车辆型号长度需在1-50字之间', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(d.vehicleModel)) {
      wx.showToast({ title: '车辆型号不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!d.vin) {
      wx.showToast({ title: '请填写VIN码', icon: 'none' });
      return false;
    }
    if (!validator.isVin(d.vin)) {
      wx.showToast({ title: 'VIN码应为17位字母数字', icon: 'none' });
      return false;
    }

    if (!d.testArea) {
      wx.showToast({ title: '请填写测试区域', icon: 'none' });
      return false;
    }
    if (d.testArea.length < 2 || d.testArea.length > 200) {
      wx.showToast({ title: '测试区域长度需在2-200字之间', icon: 'none' });
      return false;
    }
    if (validator.hasSpecialChars(d.testArea)) {
      wx.showToast({ title: '测试区域不能包含特殊符号', icon: 'none' });
      return false;
    }

    if (!d.startDate || !d.endDate) {
      wx.showToast({ title: '请选择日期', icon: 'none' });
      return false;
    }

    if (new Date(d.startDate) > new Date(d.endDate)) {
      wx.showToast({ title: '结束日期不能早于开始日期', icon: 'none' });
      return false;
    }

    if (!d.testProjects) {
      wx.showToast({ title: '请填写测试项目', icon: 'none' });
      return false;
    }
    if (d.testProjects.length < 10 || d.testProjects.length > 1000) {
      wx.showToast({ title: '测试项目长度需在10-1000字之间', icon: 'none' });
      return false;
    }

    if (!d.supportPlan) {
      wx.showToast({ title: '请填写安全保障计划', icon: 'none' });
      return false;
    }
    if (d.supportPlan.length < 10 || d.supportPlan.length > 1000) {
      wx.showToast({ title: '安全保障计划长度需在10-1000字之间', icon: 'none' });
      return false;
    }

    if (!d.officerId) {
      wx.showToast({ title: '请选择安全员', icon: 'none' });
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
      officerId: d.officerId,
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
