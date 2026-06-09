const ROLES = {
  ADMIN: 'admin',
  ENTERPRISE: 'enterprise',
  CITIZEN: 'citizen'
};

// 后端数值与前端字符串角色的映射关系
const ROLE_MAP = {
  1: ROLES.ADMIN,      // 1 -> 车管所管理端
  2: ROLES.ENTERPRISE, // 2 -> 企业运营端
  3: ROLES.CITIZEN     // 3 -> 市民监督端
};

function mapRoleType(roleType) {
  // 如果后端返回的是数字，进行转换；如果是字符串，直接返回或按需映射
  // 增加对数值和字符串数值的兼容
  const roleValue = parseInt(roleType);
  if (!isNaN(roleValue)) {
    return ROLE_MAP[roleValue] || ROLES.CITIZEN;
  }
  
  return ROLE_MAP[roleType] || roleType || ROLES.CITIZEN;
}

const ROLE_PAGES = {
  [ROLES.ADMIN]: ['/pages/admin/index', '/pages/admin/audit/index', '/pages/scan/scan'],
  [ROLES.ENTERPRISE]: ['/pages/enterprise/index'],
  [ROLES.CITIZEN]: ['/pages/citizen/index']
};

function checkRole(requiredRole) {
  const app = getApp();
  const currentRole = app.globalData.role || wx.getStorageSync('role');
  
  if (!currentRole) {
    return false;
  }
  
  if (requiredRole && currentRole !== requiredRole) {
    return false;
  }
  
  return true;
}

function requireRole(requiredRole) {
  const app = getApp();
  const currentRole = app.globalData.role || wx.getStorageSync('role');
  
  if (!currentRole) {
    wx.reLaunch({
      url: '/pages/splash/index'
    });
    return false;
  }
  
  if (requiredRole && currentRole !== requiredRole) {
    wx.reLaunch({
      url: '/pages/splash/index'
    });
    return false;
  }
  
  return true;
}

function setRole(role) {
  const app = getApp();
  const mappedRole = mapRoleType(role);
  if (app) {
    app.globalData.role = mappedRole;
  }
  wx.setStorageSync('role', mappedRole);
}

function getRole() {
  const app = getApp();
  return (app && app.globalData.role) || wx.getStorageSync('role');
}

/** 当前登录用户 ID，用于已处理列表 reviewerId 参数 */
function getReviewerId() {
  const userInfo = wx.getStorageSync('userInfo') || {};
  return userInfo.userId || userInfo.id || userInfo.reviewerId || null;
}

function clearRole() {
  const app = getApp();
  if (app) {
    app.globalData.role = null;
  }
  wx.removeStorageSync('role');
}

function navigateByRole(roleTypeRaw) {
  const enterpriseUtil = require('./enterprise.js');
  const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
  
  // 1 = 民警（优先级最高）
  if (roleTypeRaw === 1 || roleTypeRaw === '1') {
    wx.reLaunch({ url: '/pages/admin/index' });
    return;
  }
  
  // 2 = 企业端
  if (roleTypeRaw === 2 || roleTypeRaw === '2') {
    wx.reLaunch({ url: '/pages/enterprise/index' });
    return;
  }
  
  // 3 = 普通用户/企业用户
  // 通过企业资质字段判断跳转目标
  if (userInfo.authEnterpriseId && userInfo.qualificationStatus === 1) {
    wx.reLaunch({ url: '/pages/enterprise/index' });
  } else {
    wx.reLaunch({ url: '/pages/citizen/index' });
  }
}

function getRoleName(role) {
  const roleNames = {
    [ROLES.ADMIN]: '车管所管理端',
    [ROLES.ENTERPRISE]: '企业运营端',
    [ROLES.CITIZEN]: '市民监督端'
  };
  return roleNames[role] || '';
}

function getRoleColor(role) {
  const colors = {
    [ROLES.ADMIN]: '#1a365d',
    [ROLES.ENTERPRISE]: '#2f855a',
    [ROLES.CITIZEN]: '#ed8936'
  };
  return colors[role] || '#333';
}

function login() {
  return new Promise((resolve, reject) => {
    wx.login({
      success: (res) => {
        if (res.code) {
          wx.request({
            url: 'https://q32d54e8.natappfree.cc/auth/login',
            method: 'POST',
            timeout: 10000,
            data: { code: res.code },
            success: (loginRes) => {
              console.log('[Auth] Login response:', loginRes.data);
              if (loginRes.data && loginRes.data.data) {
                const data = loginRes.data.data;
                const { token, user, enterpriseName, qualificationStatus } = data;
                
                // 1. 保存 token
                wx.setStorageSync('token', token);
                
                // 2. 整合用户信息并保存
                const enterpriseUtil = require('./enterprise.js');
                const fullUserInfo = {
                  ...user,
                  enterpriseName,
                  qualificationStatus
                };
                wx.setStorageSync('userInfo', enterpriseUtil.normalizeUserInfo(fullUserInfo));
                
                // 3. 处理角色跳转
                const rawRoleType = user ? user.roleType : 3;
                const mappedRole = mapRoleType(rawRoleType);
                setRole(mappedRole);
                
                resolve({
                  token,
                  user,
                  enterpriseName,
                  qualificationStatus,
                  roleType: rawRoleType, // 返回原始数值角色
                  mappedRole: mappedRole
                });
              } else {
                reject(new Error('登录失败：数据格式不正确'));
              }
            },
            fail: (err) => {
              reject(err);
            }
          })
        } else {
          reject(new Error('微信登录失败：' + res.errMsg));
        }
      },
      fail: (err) => {
        reject(err);
      }
    })
  });
}

module.exports = {
  ROLES,
  ROLE_PAGES,
  checkRole,
  requireRole,
  setRole,
  getRole,
  getReviewerId,
  clearRole,
  getRoleName,
  getRoleColor,
  login,
  navigateByRole,
  mapRoleType
};
