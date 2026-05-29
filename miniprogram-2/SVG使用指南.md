# 微信小程序SVG图片使用指南

## 方法1：使用 `<image>` 标签直接引用（推荐）

### 步骤
1. 将 SVG 文件放入 `assets/images/` 目录
2. 在 wxml 中使用 `<image>` 标签引用

### 代码示例
```html
<image src="/assets/images/icon.svg" mode="aspectFit" />
```

### 优点
- ✅ 简单直接，与普通图片用法一致
- ✅ 支持所有小程序特性（懒加载、缓存等）
- ✅ 维护方便

---

## 方法2：转换为Base64使用

### 步骤
1. 将SVG转换为Base64编码
2. 在wxss中作为背景图片使用

### 代码示例
```css
.custom-icon {
  width: 64rpx;
  height: 64rpx;
  background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmci...');
  background-size: contain;
}
```

---

## 方法3：使用iconfont（批量图标推荐）

### 步骤
1. 将SVG上传到iconfont.cn
2. 生成字体文件
3. 在项目中引入使用

### 优点
- ✅ 可批量管理图标
- ✅ 文件体积小
- ✅ 支持样式修改（颜色、大小）

---

## SVG文件放置位置
```
assets/
└── images/
    ├── user-icon.svg       ✅ 已添加
    ├── settings-icon.svg    ✅ 已添加
    ├── about-icon.svg       ✅ 已添加
    └── ...其他svg文件
```

---

## 实际应用示例

### 1. 首页头像（已实现）
- 文件：`pages/admin/index.wxml`
- 使用方式：`<image src="/assets/images/user-icon.svg" />`

### 2. 我的页面菜单（已实现）
- 文件：`pages/profile/index.wxml`
- 使用方式：`<image class="menu-icon" src="/assets/images/settings-icon.svg" />`

---

## SVG设计建议

### 尺寸规范
- 图标建议尺寸：64x64px 或 128x128px
- 使用 viewBox 属性：`<svg viewBox="0 0 64 64">`

### 颜色方案
- 管理端：蓝紫色渐变 #3b82f6 → #8b5cf6
- 企业端：蓝绿色渐变 #059669 → #10b981  
- 市民端：橙红色渐变 #ea580c → #f59e0b

### 导出要求
- 导出为纯SVG格式
- 避免复杂滤镜（小程序支持有限）
- 保留渐变效果

---

## 注意事项

### ⚠️ 兼容性
- 微信小程序 2.13.0+ 完全支持SVG
- 建议同时提供PNG备份（可选）

### ⚠️ 性能优化
- SVG文件不宜过大（建议 < 20KB）
- 复杂SVG考虑转PNG

---

## 工具推荐

- **SVG编辑**：Figma / Sketch / Adobe Illustrator
- **SVG压缩**：https://jakearchibald.github.io/svgomg/
- **在线转换**：https://convertio.co/zh/
