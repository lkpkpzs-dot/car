const request = require('../../../../utils/request.js');

Page({
  data: {
    userList: [],
    loading: false,
    currentTab: 0,
    tabs: ['全部用户', '已封禁用户']
  },

  onLoad() {
    this.loadUserList();
  },

  onPullDownRefresh() {
    this.loadUserList().then(() => {
      wx.stopPullDownRefresh();
    });
  },

  onShow() {
    this.loadUserList();
  },

  onTabChange(e) {
    const currentTab = parseInt(e.currentTarget.dataset.index);
    this.setData({ currentTab });
    this.filterUserList();
  },

  async loadUserList() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    try {
      const res = await request.get('/citizenReport/admin/users');
      console.log('loadUserList - 接口返回数据:', res);
      
      if (res.code === 200) {
        const rawList = request.parseListData(res);
        console.log('loadUserList - 原始用户列表:', rawList);
        
        const userList = this.formatUserList(rawList);
        console.log('loadUserList - 格式化后的用户列表:', userList);
        
        this.setData({ userList });
        this.filterUserList();
      } else {
        wx.showToast({
          title: res.msg || '加载失败',
          icon: 'none'
        });
      }
    } catch (error) {
      console.error('加载用户列表失败:', error);
      wx.showToast({
        title: '加载失败，请重试',
        icon: 'none'
      });
    } finally {
      this.setData({ loading: false });
    }
  },

  formatUserList(list) {
    if (!list || !Array.isArray(list)) return [];
    
    return list
      .filter(user => {
        const isCitizenRole = user.roleType === 3;
        const hasNoEnterprise = !user.authEnterpriseId && !user.auth_enterprise_id;
        return isCitizenRole && hasNoEnterprise;
      })
      .map(user => ({
        ...user,
        totalReportCount: user.totalReportCount || 0,
        invalidReportCount: user.invalidReportCount || 0,
        isReportBanned: !!user.isReportBanned,
        banEndTime: this.formatBanEndTime(user.banEndTime),
        banStatusClass: user.isReportBanned ? 'banned' : 'normal'
      }));
  },

  formatBanEndTime(time) {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    
    if (date <= now) {
      return '已过期';
    }
    
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hour = String(date.getHours()).padStart(2, '0');
    const minute = String(date.getMinutes()).padStart(2, '0');
    
    return year + '-' + month + '-' + day + ' ' + hour + ':' + minute;
  },

  filterUserList() {
    const { currentTab, userList } = this.data;
    let filteredList = userList;
    
    if (currentTab === 1) {
      filteredList = userList.filter(user => user.isReportBanned);
    }
    
    this.setData({ filteredList });
  },

  showBanConfirm(e) {
    const user = e.currentTarget.dataset.user;
    console.log('showBanConfirm - 接收到的用户数据:', user);
    
    wx.navigateTo({
      url: '/pages/admin/report/ban-user/index?user=' + encodeURIComponent(JSON.stringify(user))
    });
  },

  showUnbanConfirm(e) {
    const user = e.currentTarget.dataset.user;
    console.log('showUnbanConfirm - 接收到的用户数据:', user);
    
    const displayName = user.realName || '用户' + user.userId;
    wx.showModal({
      title: '解封用户',
      content: '确定要解封用户 ' + displayName + ' 的举报权限吗？',
      confirmText: '确认解封',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.unbanUser(user.userId);
        }
      }
    });
  },

  async unbanUser(userId) {
    console.log('unbanUser - 接收到的 userId:', userId);
    wx.showLoading({ title: '处理中...' });
    
    try {
      const url = '/citizenReport/admin/unban-user?userId=' + userId;
      console.log('unbanUser - 请求 URL:', url);
      const res = await request.post(url, {});
      console.log('unbanUser - 接口返回:', res);
      
      wx.hideLoading();
      
      if (res.code === 200) {
        wx.showModal({
          title: '解封成功',
          content: '用户已解封，系统已发送通知用户',
          showCancel: false,
          confirmText: '确定',
          success: () => {
            this.loadUserList();
          }
        });
      } else {
        wx.showToast({
          title: res.msg || '解封失败',
          icon: 'none'
        });
      }
    } catch (error) {
      wx.hideLoading();
      console.error('解封用户失败:', error);
      wx.showToast({
        title: '解封失败，请重试',
        icon: 'none'
      });
    }
  }
});
