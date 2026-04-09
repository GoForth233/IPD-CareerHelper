<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
import { LOGIN_PAGE } from "@/utils/auth";

onLaunch(() => {
  console.log("App Launch");
  const userId = uni.getStorageSync('userId');
  if (!userId) {
    uni.reLaunch({ url: LOGIN_PAGE });
  }
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});
</script>

<style>
/* 全局 CSS 变量：科技蓝 + 活力橙，深浅色与业务页面对齐 */
:root {
  --primary-color: #2563eb;
  --primary-hover: #1d4ed8;
  --primary-mid: #3b82f6;
  --primary-soft: #eff6ff;

  --accent-color: #f97316;
  --accent-hover: #ea580c;
  --accent-soft: #fff7ed;

  /* 与 Tab 内页统一的浅灰底 */
  --page-ios-gray: #f5f5f7;
  --bg-color: #f8fafc;
  --card-bg: #ffffff;
  --text-primary: #0f172a;
  --text-secondary: #64748b;
  --text-tertiary: #8e8e93;
  --border-color: #cbd5e1;
  --border-strong: #b6c4d8;
  --interactive-label: var(--primary-color);

  --surface-1: #ffffff;
  --surface-2: #f8fafc;
  --surface-3: #f1f5f9;

  --radius-sm: 12px;
  --radius-md: 16px;
  --radius-lg: 20px;
  --radius-xl: 24px;

  --shadow-xs: 0 1px 4px rgba(15, 23, 42, 0.04);
  --shadow-sm: 0 2px 8px rgba(15, 23, 42, 0.05);
  --shadow-card: 0 4px 16px rgba(15, 23, 42, 0.06);
  --shadow-lg: 0 8px 24px rgba(15, 23, 42, 0.08);

  --nav-back-width: 64px;

  --btn-height-md: 48px;
  --btn-height-lg: 52px;
  --btn-radius: 14px;

  --font-hero: 28px;
  --font-title: 18px;
  --font-section: 17px;
  --font-body: 15px;
  --font-caption: 13px;
  --font-micro: 11px;

  --line-height-body: 1.5;
  --line-height-caption: 1.45;

  --space-xs: 4px;
  --space-sm: 8px;
  --space-md: 12px;
  --space-lg: 16px;
  --space-xl: 20px;
  --space-2xl: 24px;

  /* Custom tab bar list height (uni-app default ~50px); pad scroll areas above it */
  --tab-bar-height: 50px;
}

/* 跟随系统的深色变量（业务页可另加 .theme-dark 覆盖） */
@media (prefers-color-scheme: dark) {
  :root {
    --bg-color: #0f172a;
    --page-ios-gray: #0f172a;
    --card-bg: #1e293b;
    --text-primary: #f8fafc;
    --text-secondary: #94a3b8;
    --text-tertiary: #94a3b8;
    --border-color: #334155;
    --primary-soft: rgba(37, 99, 235, 0.2);
    --accent-soft: rgba(249, 115, 22, 0.15);
  }
}

/* 全局基础样式 */
page {
  height: 100%;
  background-color: var(--bg-color);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

/* 全局视觉系统：统一卡片、输入框、按钮、列表项 */
.ui-card,
.card,
.panel,
.section-card {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.ui-card-strong {
  border-color: var(--border-strong);
  box-shadow: var(--shadow-card);
}

.ui-input,
.input,
textarea.ui-input {
  width: 100%;
  box-sizing: border-box;
  background: var(--surface-1);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  min-height: 44px;
  padding: 0 12px;
  color: var(--text-primary);
  font-size: 14px;
}

.ui-list-item,
.list-item,
.item {
  background: var(--surface-1);
  border: 1px solid var(--border-color);
  border-radius: 14px;
}

button {
  box-sizing: border-box;
  font-family: inherit;
}

.ui-btn,
.btn-primary,
.btn-secondary,
.btn-send,
.btn-choose,
.btn-upload-cloud {
  border-radius: var(--btn-radius);
  min-height: var(--btn-height-md);
  line-height: var(--btn-height-md);
  font-weight: 600;
  border: 1px solid transparent;
}

.ui-btn-primary,
.btn-primary,
.btn-send {
  background: var(--primary-color);
  color: #fff;
  box-shadow: var(--shadow-card);
}

.ui-btn-secondary,
.btn-secondary,
.btn-choose,
.btn-upload-cloud {
  background: var(--surface-2);
  color: var(--text-primary);
  border-color: var(--border-color);
}

@media (prefers-color-scheme: dark) {
  .ui-card,
  .card,
  .panel,
  .section-card,
  .ui-list-item,
  .list-item,
  .item {
    box-shadow: none;
    border-color: var(--border-color);
  }

  .ui-input,
  .input,
  textarea.ui-input {
    background: #0f172a;
  }

  .ui-btn-secondary,
  .btn-secondary,
  .btn-choose,
  .btn-upload-cloud {
    background: #1e293b;
    color: #e2e8f0;
  }
}
</style>