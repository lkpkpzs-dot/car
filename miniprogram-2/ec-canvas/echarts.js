import * as zrender from './zrender.js';

var echarts = {
  version: '5.4.3'
};

function createCanvas() {
  return null;
}

function init(canvas, theme, opts) {
  opts = opts || {};
  var zr = zrender.init(canvas, {
    renderer: opts.renderer || 'canvas',
    devicePixelRatio: opts.devicePixelRatio
  });

  var chart = {
    _zr: zr,
    _charts: [],
    _components: [],
    _chartsMap: {},
    _componentsMap: {},
    _api: null,
    option: null,
    id: 0
  };

  chart.setOption = function(option) {
    chart.option = option;
    zr.clear();
    return chart;
  };

  chart.resize = function(opts) {
    if (opts) {
      if (opts.width) {
        zr.resize({ width: opts.width });
      }
      if (opts.height) {
        zr.resize({ height: opts.height });
      }
    }
    return chart;
  };

  chart.clear = function() {
    zr.clear();
    return chart;
  };

  chart.dispose = function() {
    zr.dispose();
    return chart;
  };

  chart.getZr = function() {
    return zr;
  };

  return chart;
}

function extendSeriesType(type) {}

echarts.extendSeriesType = extendSeriesType;

echarts.createCanvas = createCanvas;

echarts.init = init;

echarts.version = '5.4.3';

export default echarts;
