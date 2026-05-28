Component({
  data: {
    selected: 0,
    color: "#7A7E83",
    selectedColor: "#1a365d",
    list: []
  },
  attached() {
    this.updateTabList();
  },
  methods: {
    updateTabList() {
      const role = wx.getStorageSync('role');
      const enterpriseUtil = require('../utils/enterprise.js');
      const userInfo = enterpriseUtil.normalizeUserInfo(wx.getStorageSync('userInfo'));
      
      let homePath = '/pages/citizen/index';
      
      // 1. 民警端
      if (role === 'admin') {
        homePath = '/pages/admin/index';
      } 
      // 2. 企业端 (必须认证通过)
      else if (userInfo.authEnterpriseId && userInfo.qualificationStatus === 1) {
        homePath = '/pages/enterprise/index';
      }
      // 3. 普通市民端 (默认)
      else {
        homePath = '/pages/citizen/index';
      }
      
      this.setData({
        list: [{
          pagePath: homePath,
          text: "首页",
          iconPath: "/assets/images/home.png",
          selectedIconPath: "/assets/images/home-active.png"
        }, {
          pagePath: "/pages/profile/index",
          text: "我的",
          iconPath: "/assets/images/about-icon.png",
          selectedIconPath: "/assets/images/about-icon.png"
        }]
      });
    },
    switchTab(e) {
      const data = e.currentTarget.dataset;
      const url = data.path;
      wx.switchTab({ url });
      this.setData({
        selected: data.index
      });
    }
  }
});
