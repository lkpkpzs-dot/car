const validator = {
  isCreditCode: (code) => {
    if (!code) return false;
    if (code.length !== 18) return false;
    const reg = /^[0-9A-HJ-NPQRTUWXY]{2}\d{6}[0-9A-HJ-NPQRTUWXY]{10}/;
    return reg.test(code);
  },

  isPhone: (phone) => {
    if (!phone) return false;
    const reg = /^1[3-9]\d{9}$/;
    return reg.test(phone);
  },

  isIdCard: (id) => {
    if (!id) return false;
    const reg = /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/;
    return reg.test(id);
  },

  isVin: (vin) => {
    if (!vin) return false;
    if (vin.length !== 17) return false;
    const reg = /^[A-HJ-NPR-Z0-9]{17}$/;
    return reg.test(vin);
  },

  hasSpecialChars: (str) => {
    if (!str) return false;
    const reg = /[`~!@#$%^&*()+=|{}':;',\[\].<>/?~！@#￥%……&*（）——+|{}【】'；：""'。，、？]/;
    return reg.test(str);
  },

  isValidNumber: (value, min = 0, max = Infinity) => {
    const num = parseFloat(value);
    if (isNaN(num)) return false;
    return num >= min && num <= max;
  },

  isValidPositiveInteger: (value) => {
    const num = parseInt(value);
    if (isNaN(num)) return false;
    return Number.isInteger(num) && num > 0;
  },

  filterNumber: (value) => {
    if (!value) return '';
    return value.replace(/[^\d.]/g, '');
  },

  filterPositiveInteger: (value) => {
    if (!value) return '';
    return value.replace(/[^\d]/g, '');
  }
};

module.exports = validator;
