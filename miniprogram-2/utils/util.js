const formatTime = date => {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hour = date.getHours()
  const minute = date.getMinutes()
  const second = date.getSeconds()

  return `${[year, month, day].map(formatNumber).join('/')} ${[hour, minute, second].map(formatNumber).join(':')}`
}

const formatNumber = n => {
  n = n.toString()
  return n[1] ? n : `0${n}`
}

/**
 * 创建兼容 iOS 的 Date 对象
 * iOS 只支持 "yyyy/MM/dd"、"yyyy/MM/dd HH:mm:ss"、"yyyy-MM-dd"、"yyyy-MM-ddTHH:mm:ss" 等格式
 * 不支持 "yyyy-MM-dd HH:mm:ss" 格式
 */
const createCompatibleDate = timeStr => {
  if (!timeStr) return new Date(NaN)
  
  let compatibleTimeStr = timeStr
  // 如果是 "yyyy-MM-dd HH:mm:ss" 格式，转换为 "yyyy/MM/dd HH:mm:ss"
  if (timeStr.includes('-') && timeStr.includes(':')) {
    compatibleTimeStr = timeStr.replace(/-/g, '/')
  }
  
  const date = new Date(compatibleTimeStr)
  
  // 如果转换失败，尝试手动解析
  if (isNaN(date.getTime())) {
    const reg = /(\d{4})[-/](\d{1,2})[-/](\d{1,2})(?:\s+(\d{1,2}):(\d{1,2}):?(\d{0,2})?)?/
    const match = timeStr.match(reg)
    if (match) {
      const year = parseInt(match[1], 10)
      const month = parseInt(match[2], 10) - 1
      const day = parseInt(match[3], 10)
      const hour = parseInt(match[4] || 0, 10)
      const minute = parseInt(match[5] || 0, 10)
      const second = parseInt(match[6] || 0, 10)
      return new Date(year, month, day, hour, minute, second)
    }
  }
  
  return date
}

/**
 * 格式化相对时间，兼容 iOS
 */
const formatRelativeTime = timeStr => {
  const date = createCompatibleDate(timeStr)
  if (isNaN(date.getTime())) return ''
  
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hour}:${minute}`
}

module.exports = {
  formatTime,
  createCompatibleDate,
  formatRelativeTime
}
