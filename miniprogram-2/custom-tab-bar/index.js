Component({
  data: {
    selected: 0,
    color: "#64748B",
    selectedColor: "#3B82F6",
    bgColor: "#FFFFFF",
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
      let color = "#64748B";
      let selectedColor = "#3B82F6";
      let tabConfig = {};
      
      // 三端共用同一套TabBar图标
      tabConfig = {
        home: {
          icon: "/assets/images/tab-home.png",
          selectedIcon: "/assets/images/tab-home-active.png"
        },
        profile: {
          icon: "/assets/images/tab-my.png",
          selectedIcon: "/assets/images/tab-my-active.png"
        }
      };
      
      // 1. 民警端
      if (role === 'admin') {
        homePath = '/pages/admin/index';
        selectedColor = "#3B82F6";
      } 
      // 2. 企业端 (必须认证通过)
      else if (userInfo.authEnterpriseId && userInfo.qualificationStatus === 1) {
        homePath = '/pages/enterprise/index';
        selectedColor = "#10B981";
      }
      // 3. 普通市民端 (默认)
      else {
        homePath = '/pages/citizen/index';
        selectedColor = "#F59E0B";
      }
      
      this.setData({
        color,
        selectedColor,
        list: [{
          pagePath: homePath,
          text: "首页",
          iconPath: tabConfig.home.icon,
          selectedIconPath: tabConfig.home.selectedIcon
        }, {
          pagePath: "/pages/profile/index",
          text: "我的",
          iconPath: tabConfig.profile.icon,
          selectedIconPath: tabConfig.profile.selectedIcon
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
