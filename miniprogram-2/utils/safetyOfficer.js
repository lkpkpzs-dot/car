const request = require('./request.js');

const STATUS_META = {
  0: { label: '待审核', color: '#d97706', bg: '#fef3c7' },
  1: { label: '有效', color: '#16a34a', bg: '#dcfce7' },
  2: { label: '驳回', color: '#dc2626', bg: '#fee2e2' },
  3: { label: '暂停', color: '#2563eb', bg: '#dbeafe' },
  4: { label: '取消', color: '#64748b', bg: '#f1f5f9' }
};

const LICENSE_TYPES = ['A1', 'A2', 'A3', 'B1', 'B2', 'C1', 'C2'];

const CONDITION_FIELDS = [
  { key: 'noFullScoreRecord', label: '最近连续三个记分周期无记满12分记录' },
  { key: 'noMajorAccidentRecord', label: '无致人死亡或重伤交通责任事故记录' },
  { key: 'noDuiRecord', label: '无酒后或醉酒驾驶记录' },
  { key: 'noCrimeRecord', label: '无犯罪记录' },
  { key: 'healthy', label: '身心健康' },
  { key: 'noAlcoholDrugRecord', label: '无酗酒、吸毒记录' }
];

const MATERIAL_FIELDS = [
  { key: 'idCardUrl', label: '身份证' },
  { key: 'driverLicenseUrl', label: '机动车驾驶证' },
  { key: 'healthCertificateUrl', label: '机动车驾驶人身体条件证明' },
  { key: 'noCrimeCertificateUrl', label: '无犯罪记录证明' },
  { key: 'noViolationAccidentCertificateUrl', label: '无相应交通违法及事故证明' },
  { key: 'noAlcoholDrugCertificateUrl', label: '无酗酒、吸毒记录证明' }
];

const LIABILITY_OPTIONS = [
  { value: 1, label: '无责' },
  { value: 2, label: '次责' },
  { value: 3, label: '同等责任' },
  { value: 4, label: '主责' },
  { value: 5, label: '全责' }
];

const CASUALTY_OPTIONS = [
  { value: 0, label: '无伤亡' },
  { value: 1, label: '受伤' },
  { value: 2, label: '死亡' }
];

function applySafetyOfficer(officer) {
  return request.post('/safetyOfficer/apply', { officer });
}

function getMySafetyOfficers() {
  return request.get('/safetyOfficer/myList');
}

function getSafetyOfficerList(params = {}) {
  return request.get('/safetyOfficer/list', params);
}

function getSafetyOfficerDetail(id) {
  return request.get(`/safetyOfficer/${id}`);
}

function auditSafetyOfficer(data) {
  return request.put('/safetyOfficer/audit', data);
}

function handleSafetyOfficerAccident(data) {
  return request.post('/safetyOfficer/accident/handle', data);
}

function getSafetyOfficerPenalties(id) {
  return request.get(`/safetyOfficer/${id}/penalties`);
}

function deleteSafetyOfficer(id) {
  return request.del(`/safetyOfficer/${id}`);
}

function getEnterpriseValidSafetyOfficers() {
  return request.get('/safetyOfficer/enterpriseValidList');
}

function assignSafetyOfficerToCar(carId, officerId) {
  return request.post('/carArchive/assignSafetyOfficer', { carId, officerId });
}

function getCarsBySafetyOfficer(officerId) {
  return request.get(`/carArchive/byOfficer/${officerId}`);
}

function getStatusMeta(status) {
  return STATUS_META[Number(status)] || { label: '未知', color: '#64748b', bg: '#f1f5f9' };
}

function parseData(res, fallback) {
  if (!res) return fallback;
  if (res.code !== undefined && res.code !== 200) return fallback;
  if (res.code === 200 && res.data !== undefined) return res.data;
  if (res.data !== undefined) return res.data;
  return res;
}

function parseList(res) {
  const data = parseData(res, []);
  return Array.isArray(data) ? data : [];
}

function getOfficerId(item) {
  return item && (item.officerId || item.id);
}

function enrichOfficer(item) {
  if (!item) return {};
  const status = item.status === undefined || item.status === null ? item.status : Number(item.status);
  const meta = getStatusMeta(item.status);
  const suspendStart = item.suspendStartDate || item.pauseStartDate || item.suspendStartTime || item.pauseStartTime || '';
  const suspendEnd = item.suspendEndDate || item.pauseEndDate || item.suspendEndTime || item.pauseEndTime || '';
  return {
    ...item,
    status,
    officerId: getOfficerId(item),
    statusLabel: item.statusDesc || meta.label,
    statusColor: meta.color,
    statusBg: meta.bg,
    auditCommentText: item.auditComment || item.reviewComment || item.rejectReason || item.comment || '-',
    suspendRangeText: suspendStart || suspendEnd ? `${suspendStart || '-'} 至 ${suspendEnd || '-'}` : '-',
    penaltyReasonText: item.penaltyReason || item.punishReason || item.reason || '-',
    materialList: MATERIAL_FIELDS.map(field => ({
      ...field,
      url: item[field.key] || ''
    })).filter(field => field.url)
  };
}

function getLiabilityLabel(value) {
  const item = LIABILITY_OPTIONS.find(option => option.value === Number(value));
  return item ? item.label : '';
}

function getCasualtyLabel(value) {
  const item = CASUALTY_OPTIONS.find(option => option.value === Number(value));
  return item ? item.label : '';
}

module.exports = {
  STATUS_META,
  LICENSE_TYPES,
  CONDITION_FIELDS,
  MATERIAL_FIELDS,
  LIABILITY_OPTIONS,
  CASUALTY_OPTIONS,
  applySafetyOfficer,
  getMySafetyOfficers,
  getSafetyOfficerList,
  getSafetyOfficerDetail,
  auditSafetyOfficer,
  handleSafetyOfficerAccident,
  getSafetyOfficerPenalties,
  deleteSafetyOfficer,
  getEnterpriseValidSafetyOfficers,
  assignSafetyOfficerToCar,
  getCarsBySafetyOfficer,
  getStatusMeta,
  parseData,
  parseList,
  getOfficerId,
  enrichOfficer,
  getLiabilityLabel,
  getCasualtyLabel
};
