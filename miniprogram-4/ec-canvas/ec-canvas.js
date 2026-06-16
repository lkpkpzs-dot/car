import * as echarts from './echarts.js';

const COMPONENT_NAME = 'ec-canvas';

function wrapEvent(e) {
  if (!e) {
    return;
  }
  if (!e.target) {
    e.target = {};
  }
  return e;
}

Component({
  properties: {
    canvasId: {
      type: String,
      value: 'ec-canvas'
    },
    ec: {
      type: Object
    },
    forceUseOldDataFormat: {
      type: Boolean,
      value: false
    }
  },

  data: {
    isUseNewDataFormat: 1
  },

  contexts: [],

  lifetimes: {
    ready() {
      if (!this.data.ec) {
        console.warn('组件需绑定 ec 变量，例：<ec-canvas id="mychart" ec="{{ ec }}"></ec-canvas>');
        return;
      }

      if (!this.data.ec.lazyLoad) {
        this.init();
      }
    },

    detached() {
      this.dispose();
    }
  },

  methods: {
    init(callback) {
      const version = wx.version.version.split('.').map(n => parseInt(n, 10));
      const isHighVersion = version[0] > 6 || (version[0] === 6 && version[1] >= 6);
      const needUseNewDataFormat = !this.data.forceUseOldDataFormat && isHighVersion;

      this.setData({
        isUseNewDataFormat: needUseNewDataFormat ? 1 : 0
      });

      if (!this.data.ec) {
        return;
      }

      const ctx = wx.createCanvasContext(this.data.canvasId, this);

      const query = wx.createSelectorQuery().in(this);
      this.chart = echarts.init(ctx, undefined, {
        width: 0,
        height: 0,
        devicePixelRatio: wx.getSystemInfoSync().pixelRatio
      });

      if (typeof callback === 'function') {
        callback(this.chart, this.data.ec);
      } else {
        this.triggerEvent('init', {
          chart: this.chart,
          ec: this.data.ec
        });
      }

      const that = this;
      const timer = setTimeout(() => {
        that.setChartQuery(query);
        clearTimeout(timer);
      }, 100);

      if (this.data.ec.onInit) {
        this.data.ec.onInit(this.chart, this.data.ec);
      }
    },

    setChartQuery(query) {
      query.select(`#${this.data.canvasId}`).boundingClientRect((res) => {
        if (!res) {
          return;
        }
        if (this.chart) {
          this.chart.resize({
            width: res.width,
            height: res.height
          });
        }
      }).exec();
    },

    setOption(option) {
      if (this.chart) {
        this.chart.setOption(option);
      }
    },

    onTouchStart(e) {
      if (this.chart && e.touches.length > 0) {
        const touch = e.touches[0];
        const handler = this.chart.getZr().handler;
        handler.dispatch('mousedown', {
          x: touch.x,
          y: touch.y,
          zrX: touch.x,
          zrY: touch.y,
          cancelBubble: false,
          target: null,
          topTarget: null,
          offsetX: touch.x,
          offsetY: touch.y
        });
      }
    },

    onTouchMove(e) {
      if (this.chart && e.touches.length > 0) {
        const touch = e.touches[0];
        const handler = this.chart.getZr().handler;
        handler.dispatch('mousemove', {
          x: touch.x,
          y: touch.y,
          zrX: touch.x,
          zrY: touch.y,
          cancelBubble: false,
          target: null,
          topTarget: null,
          offsetX: touch.x,
          offsetY: touch.y
        });
      }
    },

    onTouchEnd(e) {
      if (this.chart) {
        const handler = this.chart.getZr().handler;
        handler.dispatch('mouseup', {
          x: 0,
          y: 0,
          zrX: 0,
          zrY: 0,
          cancelBubble: false,
          target: null,
          topTarget: null
        });
      }
    },

    canvasIdError(e) {
      console.error('canvas-id error: ' + e.detail.errMsg);
    },

    dispose() {
      if (this.chart) {
        this.chart.dispose();
      }
    }
  }
});
