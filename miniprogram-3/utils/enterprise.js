/**
 * 企业端业务常量与工具
 */

const QUALIFICATION_STATUS = {
  NONE: -1,     // 未申请 (自定义)
  PENDING: 0,   // 待审核
  APPROVED: 1,  // 已通过
  REJECTED: 2   // 已驳回
};

const QUALIFICATION_STATUS_MAP = {
  [QUALIFICATION_STATUS.NONE]: { label: '未申请', color: '#64748b', bg: '#f1f5f9' },
  [QUALIFICATION_STATUS.PENDING]: { label: '审核中', color: '#d97706', bg: '#fef3c7' },
  [QUALIFICATION_STATUS.APPROVED]: { label: '已通过', color: '#16a34a', bg: '#dcfce7' },
  [QUALIFICATION_STATUS.REJECTED]: { label: '已驳回', color: '#dc2626', bg: '#fee2e2' }
};

const LICENSE_STATUS = {
  DRAFT: 0,
  REVIEWING: 1,
  INSPECTING: 2,
  ISSUED: 3,
  REJECTED: 4
};

const LICENSE_STATUS_MAP = {
  [LICENSE_STATUS.DRAFT]: { label: '待提交', color: '#64748b', bg: '#f1f5f9' },
  [LICENSE_STATUS.REVIEWING]: { label: '审核中', color: '#d97706', bg: '#fef3c7' },
  [LICENSE_STATUS.INSPECTING]: { label: '待查验', color: '#2563eb', bg: '#dbeafe' },
  [LICENSE_STATUS.ISSUED]: { label: '已发牌', color: '#16a34a', bg: '#dcfce7' },
  [LICENSE_STATUS.REJECTED]: { label: '驳回', color: '#dc2626', bg: '#fee2e2' }
};

const APPLY_TYPE_OPTIONS = [
  { value: 1, label: '道路测试' },
  { value: 2, label: '示范应用' },
  { value: 3, label: '应用试点' }
];

const UPLOAD_FIELDS = [
  { key: 'certificateImg', label: '合格证' },
  { key: 'idCardImg', label: '身份证' },
  { key: 'testReportImg', label: '检测报告' },
  { key: 'insuranceImg', label: '保险单' },
  { key: 'authorizationImg', label: '委托书' },
  { key: 'applicationFormImg', label: '申请书' }
];

function getApplyTypeLabel(value) {
  const item = APPLY_TYPE_OPTIONS.find(o => o.value === value);
  return item ? item.label : '未知类型';
}

function normalizeUserInfo(raw) {
  const u = raw || {};
  const authEnterpriseId = u.authEnterpriseId || u.auth_enterprise_id || '';
  
  // 资质状态优先从 qualificationStatus 或 auditStatus 获取
  let qStatus = u.qualificationStatus !== undefined ? u.qualificationStatus : u.auditStatus;
  if (qStatus === undefined || qStatus === null) {
    qStatus = u.qualification_status !== undefined ? u.qualification_status : u.audit_status;
  }
  
  // 如果是 null 或 undefined，映射为 NONE (-1)
  if (qStatus === undefined || qStatus === null) {
    qStatus = QUALIFICATION_STATUS.NONE;
  }

  return {
    ...u,
    userId: u.userId || u.user_id,
    authEnterpriseId,
    auth_enterprise_id: authEnterpriseId,
    roleType: u.roleType || u.role_type,
    enterpriseName: u.enterpriseName || u.enterprise_name || '',
    creditCode: u.creditCode || u.credit_code || '',
    legalPerson: u.legalPerson || u.legal_person || '',
    contactPhone: u.contactPhone || u.contact_phone || u.phone || '',
    licenseImg: u.licenseImg || u.license_img || u.businessLicenseUrl || '',
    qualificationStatus: Number(qStatus)
  };
}

function isQualificationApproved(userInfo) {
  const u = normalizeUserInfo(userInfo);
  // 规则：必须有 authEnterpriseId 且 qualificationStatus 为 1 (已通过)
  return !!u.authEnterpriseId && u.qualificationStatus === QUALIFICATION_STATUS.APPROVED;
}

function getQualificationMeta(status) {
  return QUALIFICATION_STATUS_MAP[status] || QUALIFICATION_STATUS_MAP[QUALIFICATION_STATUS.NONE];
}

function getLicenseStatusMeta(status) {
  return LICENSE_STATUS_MAP[status] || LICENSE_STATUS_MAP[LICENSE_STATUS.DRAFT];
}

function checkLicenseApplyPermission(userInfo) {
  if (isQualificationApproved(userInfo)) {
    return { allowed: true };
  }
  return {
    allowed: false,
    message: '请先完成企业资质认证'
  };
}

function guideToQualification() {
  wx.showModal({
    title: '提示',
    content: '您尚未完成企业资质认证，是否前往申请？',
    confirmText: '去申请',
    success: (res) => {
      if (res.confirm) {
        wx.navigateTo({ url: '/pages/enterprise/qualification/index' });
      }
    }
  });
}

/** Mock：我的上牌申请列表 */
function getMockLicenseList() {
  return [
    {
      id: 10001,
      vin: 'LFPH3ACC0M1A12345',
      applyType: 1,
      status: LICENSE_STATUS.REVIEWING,
      statusDesc: '审核中',
      createTime: '2026-05-25 12:30:00',
      rejectReason: ''
    },
    {
      id: 10002,
      vin: 'LSVAU2186N2B56789',
      applyType: 2,
      status: LICENSE_STATUS.ISSUED,
      statusDesc: '已发牌',
      createTime: '2026-05-20 09:15:00',
      rejectReason: ''
    },
    {
      id: 10003,
      vin: 'LFV2A21K0N3C78901',
      applyType: 3,
      status: LICENSE_STATUS.REJECTED,
      statusDesc: '驳回',
      createTime: '2026-05-18 16:40:00',
      rejectReason: '检测报告有效期不足，请重新上传'
    }
  ];
}

function getMockLicenseDetail(id) {
  const list = getMockLicenseList();
  const base = list.find(item => String(item.id) === String(id)) || list[0];
  return {
    ...base,
    certificateImg: '',
    idCardImg: '',
    testReportImg: '',
    insuranceImg: '',
    authorizationImg: '',
    applicationFormImg: '',
    approvalRecords: [
      { nodeName: '提交申请', actionType: 1, comment: '企业提交上牌申请', createTime: '2026-05-25 12:30:00' },
      { nodeName: '初审', actionType: 3, comment: '材料审核中', createTime: '2026-05-25 14:00:00' }
    ]
  };
}

module.exports = {
  QUALIFICATION_STATUS,
  QUALIFICATION_STATUS_MAP,
  LICENSE_STATUS,
  LICENSE_STATUS_MAP,
  APPLY_TYPE_OPTIONS,
  UPLOAD_FIELDS,
  getApplyTypeLabel,
  normalizeUserInfo,
  isQualificationApproved,
  getQualificationMeta,
  getLicenseStatusMeta,
  checkLicenseApplyPermission,
  guideToQualification,
  getMockLicenseList,
  getMockLicenseDetail
};
