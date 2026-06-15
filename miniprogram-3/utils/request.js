const API_BASE_URL = 'https://wbd726ad.natappfree.cc';

const auth = require('./auth');
const cacheUtil = require('./cache');

// 刷新 token 相关状态
let isRefreshingToken = false;
let refreshTokenQueue = [];

/** GET 参数序列化：避免 isProcessed=false 被省略导致后端收不到 */
function normalizeQueryData(data) {
  const result = {};
  Object.keys(data || {}).forEach((key) => {
    const val = data[key];
    if (val === undefined || val === null) return;
    if (typeof val === 'boolean') {
      result[key] = String(val);
    } else {
      result[key] = val;
    }
  });
  return result;
}

// 不带 token 的请求（用于重新登录时）
function requestWithoutToken(options) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: options.url.startsWith('http') ? options.url : API_BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || 10000,
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(res);
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}

// 使用 refresh token 刷新
async function refreshTokenByRefreshToken() {
  const refreshToken = wx.getStorageSync('refreshToken');
  if (!refreshToken) {
    throw new Error('无刷新令牌');
  }

  const res = await requestWithoutToken({
    url: '/auth/refresh',
    method: 'POST',
    data: { refreshToken }
  });

  if (res.code === 200 && res.data && res.data.token && res.data.refreshToken) {
    wx.setStorageSync('token', res.data.token);
    wx.setStorageSync('refreshToken', res.data.refreshToken);
    
    if (res.data.user) {
      const enterpriseUtil = require('./enterprise.js');
      const fullUserInfo = {
        ...res.data.user,
        enterpriseName: res.data.enterpriseName,
        qualificationStatus: res.data.qualificationStatus
      };
      wx.setStorageSync('userInfo', enterpriseUtil.normalizeUserInfo(fullUserInfo));
    }
    
    if (res.data.user && (res.data.user.roleType !== undefined || res.data.user.role_type !== undefined)) {
      const roleType = res.data.user.roleType !== undefined ? res.data.user.roleType : res.data.user.role_type;
      const role = auth.mapRoleType(roleType);
      auth.setRole(role);
    }
    
    return;
  } else {
    throw new Error(res.msg || '刷新 token 失败');
  }
}

// 重新登录
async function reLogin() {
  wx.removeStorageSync('token');
  wx.removeStorageSync('refreshToken');

  const loginRes = await new Promise((resolve, reject) => {
    wx.login({
      success: resolve,
      fail: reject
    });
  });

  if (!loginRes.code) {
    throw new Error('wx.login 获取 code 失败');
  }

  const res = await requestWithoutToken({
    url: '/auth/login',
    method: 'POST',
    data: { code: loginRes.code }
  });

  if (res.code === 200 && res.data && res.data.token && res.data.refreshToken) {
    wx.setStorageSync('token', res.data.token);
    wx.setStorageSync('refreshToken', res.data.refreshToken);
    
    if (res.data.user) {
      const enterpriseUtil = require('./enterprise.js');
      const fullUserInfo = {
        ...res.data.user,
        enterpriseName: res.data.enterpriseName,
        qualificationStatus: res.data.qualificationStatus
      };
      wx.setStorageSync('userInfo', enterpriseUtil.normalizeUserInfo(fullUserInfo));
    }
    
    if (res.data.user && (res.data.user.roleType !== undefined || res.data.user.role_type !== undefined)) {
      const roleType = res.data.user.roleType !== undefined ? res.data.user.roleType : res.data.user.role_type;
      const role = auth.mapRoleType(roleType);
      auth.setRole(role);
    }
    
    return;
  } else {
    throw new Error(res.msg || '登录失败');
  }
}

// 刷新 token 主函数
async function refreshToken() {
  if (isRefreshingToken) {
    return new Promise((resolve, reject) => {
      refreshTokenQueue.push({ resolve, reject });
    });
  }

  isRefreshingToken = true;

  try {
    try {
      await refreshTokenByRefreshToken();
    } catch (error) {
      console.log('[RefreshToken] 使用 refreshToken 失败，尝试重新登录:', error);
      await reLogin();
    }
    
    refreshTokenQueue.forEach(({ resolve }) => resolve());
    refreshTokenQueue = [];
    return;
  } catch (error) {
    refreshTokenQueue.forEach(({ reject }) => reject(error));
    refreshTokenQueue = [];
    throw error;
  } finally {
    isRefreshingToken = false;
  }
}

const request = (options) => {
  const method = options.method || 'GET';
  const skipCache = options.skipCache === true;
  const cacheKey = cacheUtil.buildCacheKey(method, options.url, options.data);

  if (!skipCache && cacheKey) {
    const cached = cacheUtil.getCached(cacheKey);
    if (cached !== null) {
      return Promise.resolve(cached);
    }
    if (cacheUtil.inflightRequests.has(cacheKey)) {
      return cacheUtil.inflightRequests.get(cacheKey);
    }
  }

  const promise = new Promise((resolve, reject) => {
    let url = options.url.startsWith('http') ? options.url : API_BASE_URL + options.url;
    let data = options.data || {};
    
    console.log(`[Request] ${options.method || 'GET'} ${options.url}`, { data });
    
    if ((options.method === 'GET' || !options.method) && data) {
      const queryData = normalizeQueryData(data);
      const queryString = Object.keys(queryData)
        .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(queryData[key])}`)
        .join('&');
      
      if (queryString) {
        url += (url.includes('?') ? '&' : '?') + queryString;
      }
      data = {};
    }

    const defaultOptions = {
      url,
      method: options.method || 'GET',
      data,
      timeout: options.timeout || 10000,
      header: {
        'Content-Type': 'application/json',
        'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : '',
        ...options.header
      },
      success: async (res) => {
        if (res.data && res.data.data && !Array.isArray(res.data.data)) {
          const data = res.data.data;
          const user = data.user;
          
          let roleType = data.roleType !== undefined ? data.roleType : data.role_type;
          if (roleType === undefined && user) {
            roleType = user.roleType !== undefined ? user.roleType : user.role_type;
          }
          
          if (roleType !== undefined) {
            const currentRole = auth.getRole();
            const serverRole = auth.mapRoleType(roleType);
            
            if (serverRole !== currentRole) {
              auth.setRole(serverRole);
              auth.navigateByRole(serverRole);
            }
          }
        }

        console.log(`[Response] ${options.url}`, { statusCode: res.statusCode, data: res.data });
        
        if (res.statusCode === 200) {
          if (cacheKey && !skipCache) {
            cacheUtil.setCached(cacheKey, res.data, cacheUtil.resolveTtl(options.url));
          }
          resolve(res.data);
        } else if (res.statusCode === 401) {
          try {
            await refreshToken();
            const retryOptions = {
              ...options,
              header: {
                ...options.header,
                'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : ''
              }
            };
            const retryRes = await request(retryOptions);
            resolve(retryRes);
          } catch (error) {
            reject(error);
          }
        } else if (res.statusCode === 403) {
          // 403 权限错误处理
          const errorMsg = res.data && res.data.msg ? res.data.msg : '无权限访问';
          wx.showToast({
            title: errorMsg,
            icon: 'none',
            duration: 2000
          });
          reject(res);
        } else {
          reject(res);
        }
      },
      fail: (err) => {
        reject(err);
      }
    };

    wx.request(defaultOptions);
  });

  if (cacheKey && !skipCache) {
    cacheUtil.inflightRequests.set(cacheKey, promise);
    promise.finally(() => {
      cacheUtil.inflightRequests.delete(cacheKey);
    });
  }

  return promise;
};

const get = (url, data, options = {}) => {
  return request({ url, method: 'GET', data, ...options });
};

const post = (url, data) => {
  cacheUtil.invalidateMutation(url);
  return request({ url, method: 'POST', data });
};

const put = (url, data) => {
  cacheUtil.invalidateMutation(url);
  return request({ url, method: 'PUT', data });
};

const del = (url, data) => {
  cacheUtil.invalidateMutation(url);
  return request({ url, method: 'DELETE', data });
};

const uploadFile = (filePath) => {
  return new Promise((resolve, reject) => {
    const upload = () => {
      wx.uploadFile({
        url: API_BASE_URL + '/file/upload',
        filePath: filePath,
        name: 'file',
        timeout: 10000,
        header: {
          'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : '',
        },
        success: async (res) => {
          if (res.statusCode === 401) {
            try {
              await refreshToken();
              const retryRes = await uploadFile(filePath);
              resolve(retryRes);
              return;
            } catch (error) {
              reject(error);
              return;
            }
          }

          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              let url = data.data;
              url = url.replace(
                'http://10.10.4.185:8080',
                API_BASE_URL
              );
              resolve(url);
            } else {
              reject(new Error(data.msg || '上传失败'));
            }
          } catch (e) {
            reject(new Error('解析响应失败'));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    };

    upload();
  });
};

/** 解析 { code, msg, data } 响应体中的 data 列表 */
function parseListData(res) {
  if (!res) return [];
  if (Array.isArray(res)) return res;
  if (res.code === 200 && Array.isArray(res.data)) return res.data;
  if (Array.isArray(res.data)) return res.data;
  return [];
}

module.exports = {
  request,
  get,
  post,
  put,
  del,
  uploadFile,
  parseListData,
  API_BASE_URL,
  invalidateCache: cacheUtil.invalidateAll
};
