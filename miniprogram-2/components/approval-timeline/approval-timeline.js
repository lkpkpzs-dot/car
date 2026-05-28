const auditUtil = require('../../utils/audit.js');

Component({
  properties: {
    records: { type: Array, value: [] }
  },

  observers: {
    records(list) {
      const items = (list || []).map(item => ({
        ...item,
        actionLabel: auditUtil.getActionTypeLabel(item.actionType),
        actionColor: auditUtil.getActionTypeColor(item.actionType)
      }));
      this.setData({ items });
    }
  },

  data: {
    items: []
  }
});
