/** 业务类型 */
const BUSINESS_TYPE = {
  PLATE: 1,
  ENTERPRISE: 2
};

/** 审批动作（与后端 approvalRecord.actionType 一致） */
const ACTION_TYPE = {
  SUBMIT: 1,
  PASS: 2,
  REJECT: 3,
  TRANSFER: 4
};

const BUSINESS_TYPE_LABEL = {
  [BUSINESS_TYPE.PLATE]: '上牌申请',
  [BUSINESS_TYPE.ENTERPRISE]: '企业资质'
};

const ACTION_TYPE_LABEL = {
  [ACTION_TYPE.SUBMIT]: '提交',
  [ACTION_TYPE.PASS]: '通过',
  [ACTION_TYPE.REJECT]: '审核中',
  [ACTION_TYPE.TRANSFER]: '转审'
};

const POLICE_NODE_KEYWORD = '民警';

const ACTION_TYPE_COLOR = {
  [ACTION_TYPE.SUBMIT]: '#2563eb',
  [ACTION_TYPE.PASS]: '#16a34a',
  [ACTION_TYPE.REJECT]: '#dc2626',
  [ACTION_TYPE.TRANSFER]: '#7c3aed'
};

function getBusinessTypeLabel(businessType) {
  return BUSINESS_TYPE_LABEL[businessType] || '未知类型';
}

function getActionTypeLabel(actionType) {
  return ACTION_TYPE_LABEL[actionType] || '处理中';
}

function getActionTypeColor(actionType) {
  return ACTION_TYPE_COLOR[actionType] || '#64748b';
}

/** 列表 statusDesc 着色（已处理 Tab） */
function getStatusStyle(statusDesc) {
  const desc = (statusDesc || '').toLowerCase();
  if (desc.includes('驳回') || desc.includes('拒绝')) {
    return { bg: '#fee2e2', color: '#dc2626' };
  }
  if (desc.includes('通过') || desc.includes('发牌')) {
    return { bg: '#dcfce7', color: '#16a34a' };
  }
  return { bg: '#f1f5f9', color: '#64748b' };
}

function filterByBusinessType(list, filterType) {
  if (!filterType || filterType === 0) return list;
  return (list || []).filter(item => item.businessType === filterType);
}

function filterByKeyword(list, keyword) {
  const kw = (keyword || '').trim().toLowerCase();
  if (!kw) return list;
  return (list || []).filter(item => {
    const title = (item.title || '').toLowerCase();
    const id = String(item.id || '');
    return title.includes(kw) || id.includes(kw);
  });
}

function parseSnapshotJson(snapshotJson) {
  if (!snapshotJson || snapshotJson === 'null') return null;
  try {
    return typeof snapshotJson === 'string' ? JSON.parse(snapshotJson) : snapshotJson;
  } catch (e) {
    return null;
  }
}

function isPoliceNode(nodeName) {
  return nodeName && nodeName.indexOf(POLICE_NODE_KEYWORD) !== -1;
}

function getPoliceRecord(records) {
  return (records || []).find(r => isPoliceNode(r.nodeName));
}

/** 民警审核已完成（通过） */
function isProcessedTask(records) {
  const police = getPoliceRecord(records);
  return !!(police && police.actionType === ACTION_TYPE.PASS);
}

function buildTaskTitle(records, businessType) {
  const applyId = records[0].applyId;
  const snapshotRecord = (records || []).find(r => r.snapshotJson && r.snapshotJson !== 'null');
  const snap = parseSnapshotJson(snapshotRecord && snapshotRecord.snapshotJson);

  if (snap && snap.enterpriseName) return snap.enterpriseName;
  if (snap && snap.vin) return `上牌申请: ${snap.vin}`;
  if (businessType === BUSINESS_TYPE.PLATE) return `上牌申请 #${applyId}`;
  return `企业资质 #${applyId}`;
}

function buildStatusDesc(records, processed) {
  const police = getPoliceRecord(records);
  if (processed && police) {
    return getActionTypeLabel(police.actionType);
  }
  const latest = records[0];
  if (latest && latest.nodeName) {
    return latest.nodeName;
  }
  return '待审核';
}

/**
 * 将 /approvalRecord/list 扁平记录聚合为任务列表
 * @param {Array} records 审批记录
 * @param {boolean} isProcessed true=已处理, false=待审核
 */
function buildTaskListFromRecords(records, isProcessed) {
  const groups = {};

  (records || []).forEach(record => {
    const key = `${record.applyId}_${record.businessType}`;
    if (!groups[key]) groups[key] = [];
    groups[key].push(record);
  });

  const tasks = [];

  Object.keys(groups).forEach(key => {
    const group = groups[key].slice().sort((a, b) => {
      return new Date(b.createTime.replace(/-/g, '/')) - new Date(a.createTime.replace(/-/g, '/'));
    });

    const processed = isProcessedTask(group);
    if (isProcessed !== processed) return;

    const submitRecord = group.slice().reverse().find(r => r.actionType === ACTION_TYPE.SUBMIT) || group[group.length - 1];

    tasks.push({
      id: group[0].applyId,
      businessType: group[0].businessType,
      title: buildTaskTitle(group, group[0].businessType),
      statusDesc: buildStatusDesc(group, processed),
      status: group[0].actionType,
      createTime: (submitRecord && submitRecord.createTime) || group[0].createTime
    });
  });

  return tasks.sort((a, b) => {
    return new Date(b.createTime.replace(/-/g, '/')) - new Date(a.createTime.replace(/-/g, '/'));
  });
}

module.exports = {
  BUSINESS_TYPE,
  ACTION_TYPE,
  BUSINESS_TYPE_LABEL,
  ACTION_TYPE_LABEL,
  getBusinessTypeLabel,
  getActionTypeLabel,
  getActionTypeColor,
  getStatusStyle,
  filterByBusinessType,
  filterByKeyword,
  parseSnapshotJson,
  buildTaskListFromRecords,
  isProcessedTask
};
