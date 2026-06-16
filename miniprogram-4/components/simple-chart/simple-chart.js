Component({
  properties: {
    canvasId: {
      type: String,
      value: 'chart'
    },
    chartData: {
      type: Object,
      value: {}
    },
    chartType: {
      type: String,
      value: 'line'
    }
  },

  data: {
    chartWidth: 0,
    chartHeight: 0
  },

  lifetimes: {
    ready() {
      this.initChart();
    }
  },

  methods: {
    initChart() {
      const query = wx.createSelectorQuery().in(this);
      query.select(`#${this.data.canvasId}`).boundingClientRect((res) => {
        if (!res) return;
        
        this.setData({
          chartWidth: res.width,
          chartHeight: res.height
        });

        this.drawChart();
      }).exec();
    },

    drawChart() {
      const ctx = wx.createCanvasContext(this.data.canvasId, this);
      const { chartWidth: width, chartHeight: height, chartType, chartData } = this.data;
      
      if (!width || !height) return;

      const padding = {
        top: 50,
        right: 20,
        bottom: 40,
        left: 50
      };

      const chartWidth = width - padding.left - padding.right;
      const chartHeight = height - padding.top - padding.bottom;

      ctx.clearRect(0, 0, width, height);

      if (chartType === 'line') {
        this.drawLineChart(ctx, chartData, padding, chartWidth, chartHeight);
      } else if (chartType === 'bar') {
        this.drawBarChart(ctx, chartData, padding, chartWidth, chartHeight);
      }

      ctx.draw();
    },

    drawLineChart(ctx, data, padding, chartWidth, chartHeight) {
      const { dates = [], data: values = [] } = data;
      if (values.length === 0) return;

      const maxValue = Math.max(...values);
      const minValue = Math.min(...values);
      const range = maxValue - minValue || 1;

      const xStep = chartWidth / (dates.length - 1 || 1);
      const yScale = chartHeight / range;

      ctx.setFontSize(10);
      ctx.setFillStyle('#666');
      ctx.setTextAlign('center');

      dates.forEach((date, i) => {
        const x = padding.left + i * xStep;
        ctx.fillText(date, x, padding.top + chartHeight + 20);
      });

      ctx.setTextAlign('right');
      const ySteps = 5;
      for (let i = 0; i <= ySteps; i++) {
        const value = minValue + (range / ySteps) * (ySteps - i);
        const y = padding.top + (chartHeight / ySteps) * i;
        
        ctx.fillText(Math.round(value), padding.left - 5, y + 3);
        
        ctx.beginPath();
        ctx.setStrokeStyle('#f0f0f0');
        ctx.moveTo(padding.left, y);
        ctx.lineTo(padding.left + chartWidth, y);
        ctx.stroke();
      }

      ctx.beginPath();
      ctx.setStrokeStyle('#3182ce');
      ctx.setLineWidth(2);

      values.forEach((value, i) => {
        const x = padding.left + i * xStep;
        const y = padding.top + chartHeight - (value - minValue) * yScale;

        if (i === 0) {
          ctx.moveTo(x, y);
        } else {
          ctx.lineTo(x, y);
        }
      });
      ctx.stroke();

      const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartHeight);
      gradient.addColorStop(0, 'rgba(49, 130, 206, 0.5)');
      gradient.addColorStop(1, 'rgba(49, 130, 206, 0.1)');

      ctx.lineTo(padding.left + (values.length - 1) * xStep, padding.top + chartHeight);
      ctx.lineTo(padding.left, padding.top + chartHeight);
      ctx.closePath();
      ctx.setFillStyle(gradient);
      ctx.fill();

      ctx.setFillStyle('#3182ce');
      values.forEach((value, i) => {
        const x = padding.left + i * xStep;
        const y = padding.top + chartHeight - (value - minValue) * yScale;
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, 2 * Math.PI);
        ctx.fill();
      });
    },

    drawBarChart(ctx, data, padding, chartWidth, chartHeight) {
      const { dates = [], data: values = [] } = data;
      if (values.length === 0) return;

      const maxValue = Math.max(...values, 1);

      ctx.setFontSize(10);
      ctx.setFillStyle('#666');
      ctx.setTextAlign('center');

      const barWidth = chartWidth / dates.length * 0.6;
      const gap = chartWidth / dates.length * 0.4;

      dates.forEach((date, i) => {
        const x = padding.left + i * (barWidth + gap) + gap / 2;
        ctx.fillText(date, x + barWidth / 2, padding.top + chartHeight + 20);
      });

      ctx.setTextAlign('right');
      const ySteps = 5;
      for (let i = 0; i <= ySteps; i++) {
        const value = (maxValue / ySteps) * (ySteps - i);
        const y = padding.top + (chartHeight / ySteps) * i;
        
        ctx.fillText(Math.round(value), padding.left - 5, y + 3);
        
        ctx.beginPath();
        ctx.setStrokeStyle('#f0f0f0');
        ctx.moveTo(padding.left, y);
        ctx.lineTo(padding.left + chartWidth, y);
        ctx.stroke();
      }

      values.forEach((value, i) => {
        const x = padding.left + i * (barWidth + gap) + gap / 2;
        const barHeight = (value / maxValue) * chartHeight;
        const y = padding.top + chartHeight - barHeight;

        const gradient = ctx.createLinearGradient(0, y, 0, y + barHeight);
        gradient.addColorStop(0, '#e53e3e');
        gradient.addColorStop(1, '#fc8181');

        ctx.beginPath();
        ctx.setFillStyle(gradient);
        ctx.roundRect(x, y, barWidth, barHeight, [4, 4, 0, 0]);
        ctx.fill();
      });
    },

    setChartData(data) {
      this.setData({ chartData: data });
      this.drawChart();
    }
  }
});
