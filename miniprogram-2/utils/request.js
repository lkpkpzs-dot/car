const API_BASE_URL = 'http://10.10.4.192:8080';
const auth = require('./auth');

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

// 重新登录
async function refreshToken() {
  if (isRefreshingToken) {
    // 如果正在刷新，加入队列等待
    return new Promise((resolve, reject) => {
      refreshTokenQueue.push({ resolve, reject });
    });
  }

  isRefreshingToken = true;
  console.log('[Auth] 开始重新登录...');

  try {
    // 清除旧的 token
    wx.removeStorageSync('token');

    // 调用 wx.login 获取 code
    const loginRes = await new Promise((resolve, reject) => {
      wx.login({
        success: resolve,
        fail: reject
      });
    });

    if (!loginRes.code) {
      throw new Error('wx.login 获取 code 失败');
    }

    // 调用后端登录接口
    const res = await requestWithoutToken({
      url: '/auth/login',
      method: 'POST',
      data: { code: loginRes.code }
    });

    if (res.code === 200 && res.data && res.data.token) {
      wx.setStorageSync('token', res.data.token);
      
      // 保存用户信息
      if (res.data.user) {
        wx.setStorageSync('userInfo', res.data.user);
      }
      
      // 保存角色
      if (res.data.roleType !== undefined || res.data.role_type !== undefined) {
        const roleType = res.data.roleType !== undefined ? res.data.roleType : res.data.role_type;
        const role = auth.mapRoleType(roleType);
        auth.setRole(role);
      }
      
      console.log('[Auth] 重新登录成功');
      
      // 执行队列中的所有请求
      refreshTokenQueue.forEach(({ resolve }) => resolve());
      refreshTokenQueue = [];
      return;
    } else {
      throw new Error(res.msg || '登录失败');
    }
  } catch (error) {
    console.error('[Auth] 重新登录失败:', error);
    // 拒绝队列中的所有请求
    refreshTokenQueue.forEach(({ reject }) => reject(error));
    refreshTokenQueue = [];
    throw error;
  } finally {
    isRefreshingToken = false;
  }
}

const request = (options) => {
  return new Promise((resolve, reject) => {
    const queryData = options.method === 'GET' || !options.method
      ? normalizeQueryData(options.data)
      : (options.data || {});

    const defaultOptions = {
      url: options.url.startsWith('http') ? options.url : API_BASE_URL + options.url,
      method: options.method || 'GET',
      data: queryData,
      timeout: options.timeout || 10000,
      header: {
        'Content-Type': 'application/json',
        'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : '',
        ...options.header
      },
      success: async (res) => {
        // 自动检查响应中的角色标识（仅对象型 data，跳过列表数组）
        if (res.data && res.data.data && !Array.isArray(res.data.data)) {
          const data = res.data.data;
          const user = data.user;
          
          // 尝试从多个可能的位置获取角色标识
          let roleType = data.roleType !== undefined ? data.roleType : data.role_type;
          if (roleType === undefined && user) {
            roleType = user.roleType !== undefined ? user.roleType : user.role_type;
          }
          
          if (roleType !== undefined) {
            const currentRole = auth.getRole();
            const serverRole = auth.mapRoleType(roleType);
            
            if (serverRole !== currentRole) {
              console.log(`检测到角色变更: ${currentRole} -> ${serverRole}`);
              auth.setRole(serverRole);
              auth.navigateByRole(serverRole);
            }
          }
        }

        if (res.statusCode === 200) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          try {
            console.log('[Auth] 收到 401，重新登录...');
            await refreshToken();
            // 重新登录成功后，重试当前请求
            console.log('[Auth] 重新登录成功，重试请求...');
            // 用新的 token 重新发起请求
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
            console.error('[Auth] 重新登录失败，拒绝请求:', error);
            reject(error);
          }
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
};

const get = (url, data) => {
  return request({ url, method: 'GET', data });
};

const post = (url, data) => {
  return request({ url, method: 'POST', data });
};

const put = (url, data) => {
  return request({ url, method: 'PUT', data });
};

const del = (url, data) => {
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
          // 检查 401 状态码
          if (res.statusCode === 401) {
            try {
              console.log('[Auth] 上传收到 401，重新登录...');
              await refreshToken();
              // 重新登录成功后，重试上传
              console.log('[Auth] 重新登录成功，重试上传...');
              const retryRes = await uploadFile(filePath);
              resolve(retryRes);
              return;
            } catch (error) {
              console.error('[Auth] 重新登录失败，拒绝上传:', error);
              reject(error);
              return;
            }
          }

          try {
            const data = JSON.parse(res.data);
            if (data.code === 200) {
              resolve(data.data); // 返回 URL 字符串
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
  API_BASE_URL
};
