const API_BASE_URL = 'http://10.10.4.146:8080';
const auth = require('./auth');

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

const request = (options) => {
  return new Promise((resolve, reject) => {
    const queryData = options.method === 'GET' || !options.method
      ? normalizeQueryData(options.data)
      : (options.data || {});

    const defaultOptions = {
      url: options.url.startsWith('http') ? options.url : API_BASE_URL + options.url,
      method: options.method || 'GET',
      data: queryData,
      header: {
        'Content-Type': 'application/json',
        'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : '',
        ...options.header
      },
      success: (res) => {
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
          // Token 过期或无效，跳转回启动页重连
          auth.clearRole();
          wx.reLaunch({ url: '/pages/index/index' });
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

const uploadFile = (filePath) => {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: API_BASE_URL + '/file/upload',
      filePath: filePath,
      name: 'file',
      header: {
        'Authorization': wx.getStorageSync('token') ? `Bearer ${wx.getStorageSync('token')}` : '',
      },
      success: (res) => {
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
  uploadFile,
  parseListData,
  API_BASE_URL
};
