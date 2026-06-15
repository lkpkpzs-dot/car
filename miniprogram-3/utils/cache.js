/**
 * 轻量 GET 响应缓存 + in-flight 请求合并
 */
const DEFAULT_TTL = 30000;

const responseCache = new Map();
const inflightRequests = new Map();

const CACHE_TTL_RULES = [
  { prefix: '/enterprise/dashboard', ttl: 45000 },
  { prefix: '/sysMessage/unreadCount', ttl: 30000 },
  { prefix: '/sysMessage/myMessages', ttl: 30000 },
  { prefix: '/citizenReport/enterpriseList', ttl: 30000 },
  { prefix: '/enterprise/admin/dashboard', ttl: 45000 },
  { prefix: '/enterprise/citizen/dashboard', ttl: 45000 },
  { prefix: '/common/reverseGeocode', ttl: 86400000 }
];

function normalizeQueryData(data) {
  const result = {};
  Object.keys(data || {}).forEach((key) => {
    const val = data[key];
    if (val === undefined || val === null) return;
    result[key] = typeof val === 'boolean' ? String(val) : val;
  });
  return result;
}

function buildCacheKey(method, url, data) {
  const upperMethod = (method || 'GET').toUpperCase();
  if (upperMethod !== 'GET') {
    return null;
  }
  let key = upperMethod + ':' + url;
  if (data && Object.keys(data).length > 0) {
    key += '?' + JSON.stringify(normalizeQueryData(data));
  }
  return key;
}

function resolveTtl(url) {
  for (let i = 0; i < CACHE_TTL_RULES.length; i++) {
    if (url.indexOf(CACHE_TTL_RULES[i].prefix) !== -1) {
      return CACHE_TTL_RULES[i].ttl;
    }
  }
  return DEFAULT_TTL;
}

function getCached(key) {
  const entry = responseCache.get(key);
  if (!entry) return null;
  if (Date.now() > entry.expireAt) {
    responseCache.delete(key);
    return null;
  }
  return entry.data;
}

function setCached(key, data, ttl) {
  responseCache.set(key, {
    data,
    expireAt: Date.now() + (ttl || DEFAULT_TTL)
  });
}

function invalidateByPrefix(prefix) {
  responseCache.forEach((_, key) => {
    if (key.indexOf(prefix) !== -1) {
      responseCache.delete(key);
    }
  });
}

function invalidateAll() {
  responseCache.clear();
}

function invalidateMutation(url) {
  if (!url) return;
  if (url.indexOf('/sysMessage') !== -1) {
    invalidateByPrefix('GET:/sysMessage');
  }
  if (url.indexOf('/citizenReport') !== -1) {
    invalidateByPrefix('GET:/citizenReport');
  }
  if (url.indexOf('/enterprise') !== -1 || url.indexOf('/roadApplication') !== -1) {
    invalidateByPrefix('GET:/enterprise/dashboard');
    invalidateByPrefix('GET:/enterprise/admin/dashboard');
    invalidateByPrefix('GET:/enterprise/citizen/dashboard');
  }
}

module.exports = {
  buildCacheKey,
  getCached,
  setCached,
  resolveTtl,
  inflightRequests,
  invalidateMutation,
  invalidateAll,
  invalidateByPrefix
};
