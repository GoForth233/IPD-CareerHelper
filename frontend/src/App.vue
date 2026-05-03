<script setup lang="ts">
import { onLaunch } from "@dcloudio/uni-app";
import { isLoggedIn, LOGIN_PAGE } from "@/utils/auth";

/** Must match AGREEMENT_VERSION in pages/consent/index.vue */
const AGREEMENT_VERSION = '1.0';
const CONSENT_KEY = `consent_v${AGREEMENT_VERSION}`;
const CONSENT_PAGE = '/pages/consent/index';

onLaunch(() => {
  // F2: First-launch consent gate.
  // If the user has never accepted the Privacy Policy + age check, always
  // show the consent page regardless of login state. The consent page
  // handles the subsequent routing (login page or home).
  const hasConsent = uni.getStorageSync(CONSENT_KEY);
  if (!hasConsent) {
    uni.reLaunch({ url: CONSENT_PAGE });
    return;
  }

  // Cold-start gate: real users *and* guests both keep their session.
  // Previously a guest's lack of `userId` would bounce them back to the
  // login page on every relaunch; the auth helper now treats the guest
  // sentinel id as "logged in for navigation purposes."
  if (!isLoggedIn()) {
    uni.reLaunch({ url: LOGIN_PAGE });
  }
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

  --page-gutter: 20px;
  --page-gutter-tight: 16px;
  --content-max-width: 560px;

  /* Custom tab bar list height (uni-app default ~50px); pad scroll areas above it */
  --tab-bar-height: 50px;
}

/* 跟随系统的深色变量（业务页可另加 .theme-dark 覆盖） */
/* #ifndef H5 */
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
/* #endif */

/* 全局基础样式 */
page {
  height: 100%;
  background-color: var(--bg-color);
  color: var(--text-primary);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.app-shell {
  min-height: 100vh;
  background: var(--page-ios-gray);
  padding: 0 20px;
  box-sizing: border-box;
}

.app-page-header {
  padding: 8px 0 18px;
}

.app-page-kicker {
  display: block;
  font-size: 11px;
  line-height: 1.3;
  font-weight: 700;
  color: var(--primary-color);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  margin-bottom: 6px;
}

.app-page-title {
  display: block;
  font-size: var(--font-hero);
  line-height: 1.15;
  font-weight: 800;
  color: var(--text-primary);
}

.app-page-subtitle {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.55;
  color: var(--text-secondary);
}

.app-section-title {
  display: block;
  font-size: var(--font-section);
  line-height: 1.3;
  font-weight: 700;
  color: var(--text-primary);
}

.app-section-meta {
  font-size: var(--font-caption);
  color: var(--text-tertiary);
}

.app-surface {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.app-list-stack {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.app-empty {
  padding: 56px 20px 28px;
  text-align: center;
}

.app-empty__title {
  display: block;
  margin-top: 10px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.app-empty__desc {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.45;
  color: var(--text-secondary);
}

/* #ifdef H5 */
html,
body,
#app {
  height: 100%;
  overflow-x: hidden;
}
/* #endif */

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

/* #ifndef H5 */
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
/* #endif */

/* ============================================================== *
 *  WeChat Mini-Program parity layer
 *  -----------------------------------------------------------------
 *  WeChat's webview renders box-shadows ~50% lighter than Chrome,
 *  backdrop-filter is unsupported, <button> ships its own ::after
 *  decoration, and uniapp wrappers default to overflow:hidden which
 *  clips shadows entirely. The H5 build doesn't suffer any of this.
 *  This block patches all of those gaps for mp-weixin.
 * ============================================================== */
/* #ifdef MP-WEIXIN */

/* 1) Significantly boost shadow values for the dimmer MP renderer.
      The MP WebView renders RGBA opacity roughly 50% lighter than
      Chrome, so we need ~2x the alpha to look equivalent.
      Re-declare on both :root and page for full cascade coverage. */
:root,
page {
  --shadow-xs:   0 1px 3px rgba(0,0,0,0.10),  0 1px 8px  rgba(0,0,0,0.06);
  --shadow-sm:   0 2px 8px  rgba(0,0,0,0.14),  0 1px 3px  rgba(0,0,0,0.08);
  --shadow-card: 0 4px 16px rgba(0,0,0,0.16),  0 2px 6px  rgba(0,0,0,0.10);
  --shadow-lg:   0 8px 24px rgba(0,0,0,0.20),  0 4px 10px rgba(0,0,0,0.12);

  /* Stronger borders so cards read well at any brightness level */
  --border-color:  #b8c8da;
  --border-strong: #9eb0c4;
}

/* 2) Kill WeChat's native <button> decoration so our own
      border / radius / shadow / background actually show up. */
button {
  background: transparent;
  padding: 0;
  margin: 0;
  line-height: inherit;
  border: none;
}
button::after {
  border: none !important;
  border-radius: 0 !important;
  content: none !important;
}

/* 3) Any container that owns a box-shadow MUST be overflow:visible.
      border-radius alone does NOT clip shadows — only overflow:hidden
      does. uni-app wrappers sometimes inherit hidden, making shadows
      vanish entirely on device. */
.ui-card,
.card,
.panel,
.section-card,
.app-surface,
.ui-card-strong,
.feature-item,
.msg-card,
.video-card,
.article-card,
.consultation-card,
.career-card,
.assessment-card,
.check-in-card,
.checkin-card,
.stat-box,
.resume-card,
.profile-card,
.memory-card,
.feedback-card {
  overflow: visible;
}

/* 4) Compensate for the MP WebView's lighter gradient rendering:
      make gradients deeper / more saturated. */
page {
  --primary-soft:  #dbeafe;
  --accent-soft:   #ffedd5;
  --surface-2:     #f1f5f9;
  --surface-3:     #e8eef4;
}

/* 5) Inputs and textareas don't inherit font-size / color cleanly.
      Pin them so they match H5. */
.ui-input,
.input,
textarea.ui-input,
input,
textarea {
  font-size: 14px;
  color: var(--text-primary);
}

/* 6) Boost icon badge / chip shadow (these use inline box-shadow
      values not tied to variables — override them globally). */
.icon-assess  { box-shadow: 0 4px 14px rgba(37, 99, 235, 0.22) !important; }
.icon-map     { box-shadow: 0 4px 14px rgba(99, 102, 241, 0.22) !important; }
.icon-ai      { box-shadow: 0 4px 14px rgba(168, 85, 247, 0.22) !important; }
.icon-interview { box-shadow: 0 4px 14px rgba(249, 115, 22, 0.22) !important; }

/* 7) Segment control / tab pills — ensure active tab pops on device */
.seg-active,
.tab-active {
  box-shadow: 0 2px 8px rgba(0,0,0,0.12) !important;
}

/* 8) backdrop-filter is entirely unsupported in WeChat mini program.
      Elements that rely on it for frosted glass look fine at high
      opacity (≥ 85%), but translucent-background elements (≤ 50%)
      become invisible. Override the most common patterns:
      - Frosted nav bars / sticky footers → opaque white/gray
      - Glassmorphism cards inside gradient backgrounds → solid white 80% */
.chat-nav,
.sticky-cta,
.bottom-bar,
.toolbar,
.nav-bar {
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
}
.chat-nav        { background: #ffffff; }
.sticky-cta,
.bottom-bar,
.toolbar         { background: rgba(245, 245, 247, 1); }

/* #endif */
</style>
