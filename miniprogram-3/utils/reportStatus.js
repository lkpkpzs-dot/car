/**
 * 举报处理状态管理
 * 统一管理前后端状态映射，确保一致性
 */

// 举报处理状态枚举
const REPORT_PROCESS_STATUS = {
  PENDING_VERIFICATION: 0,      // 待核实
  ENTERPRISE_PROCESSING: 1,     // 企业处理中
  PROCESSED: 2,                 // 已处理
  INVALID: 3,                   // 无效举报
  POLICE_REVIEW: 4              // 待民警审核
};

// 状态映射：状态值 -> 显示文本和样式
const REPORT_STATUS_MAP = {
  [REPORT_PROCESS_STATUS.PENDING_VERIFICATION]: {
    label: '待核实',
    className: 'pending',
    color: '#d97706',
    bg: '#fef3c7'
  },
  [REPORT_PROCESS_STATUS.ENTERPRISE_PROCESSING]: {
    label: '企业处理中',
    className: 'processing',
    color: '#2563eb',
    bg: '#dbeafe'
  },
  [REPORT_PROCESS_STATUS.PROCESSED]: {
    label: '已处理',
    className: 'approved',
    color: '#16a34a',
    bg: '#dcfce7'
  },
  [REPORT_PROCESS_STATUS.INVALID]: {
    label: '无效举报',
    className: 'rejected',
    color: '#dc2626',
    bg: '#fee2e2'
  },
  [REPORT_PROCESS_STATUS.POLICE_REVIEW]: {
    label: '待民警审核',
    className: 'escalated',
    color: '#7c3aed',
    bg: '#f3e8ff'
  }
};

/**
 * 获取状态元数据
 * @param {number} status - 状态值
 * @returns {Object} 状态元数据
 */
function getReportStatusMeta(status) {
  const statusNum = Number(status);
  return REPORT_STATUS_MAP[statusNum] || REPORT_STATUS_MAP[REPORT_PROCESS_STATUS.PENDING_VERIFICATION];
}

/**
 * 获取状态显示文本
 * @param {number} status - 状态值
 * @returns {string} 状态显示文本
 */
function getReportStatusLabel(status) {
  return getReportStatusMeta(status).label;
}

/**
 * 获取状态样式类名
 * @param {number} status - 状态值
 * @returns {string} 样式类名
 */
function getReportStatusClass(status) {
  return getReportStatusMeta(status).className;
}

/**
 * 验证状态值是否有效
 * @param {number} status - 状态值
 * @returns {boolean} 是否有效
 */
function isValidReportStatus(status) {
  const statusNum = Number(status);
  return Object.values(REPORT_PROCESS_STATUS).includes(statusNum);
}

/**
 * 获取状态映射表（仅包含标签，用于页面显示）
 * @returns {Object} 状态映射表
 */
function getReportStatusLabelMap() {
  const labelMap = {};
  Object.keys(REPORT_STATUS_MAP).forEach(key => {
    labelMap[key] = REPORT_STATUS_MAP[key].label;
  });
  return labelMap;
}

module.exports = {
  REPORT_PROCESS_STATUS,
  REPORT_STATUS_MAP,
  getReportStatusMeta,
  getReportStatusLabel,
  getReportStatusClass,
  isValidReportStatus,
  getReportStatusLabelMap
};
