<template>
  <view class="msg-page" :class="[themeClass, fontClass]">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="page-header">
      <view class="header-row">
        <text class="page-title">{{ t('messages.title') }}</text>
        <view
          class="clear-btn"
          v-if="filteredMessages.length > 0 && unreadCount > 0"
          @click="markAllReadHandler"
        ><text class="clear-btn-text">{{ t('messages.markAllRead') }}</text></view>
      </view>
    </view>

    <!-- F9: Category segment tabs -->
    <view class="segment-wrap">
      <view class="segment-bar">
        <view
          v-for="tab in TABS"
          :key="tab.key"
          class="seg-item"
          :class="{ 'seg-active': activeTab === tab.key }"
          @click="activeTab = tab.key as TabKey"
        >
          <text class="seg-text">{{ tab.label }}</text>
          <view class="seg-badge" v-if="unreadByTab[tab.key] > 0">
            <text class="seg-badge-text">{{ unreadByTab[tab.key] }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Message list -->
    <scroll-view class="msg-list" scroll-y>
      <view class="list-wrap">
        <view
          class="msg-card"
          :class="{ 'msg-unread': item.unread }"
          v-for="(item, idx) in filteredMessages"
          :key="idx"
          @click="handleSystemClick(item)"
        >
          <view class="avatar-wrap">
            <view class="msg-avatar sys-av" :class="'sys-' + (idx % 3)">
              <text class="av-emoji">{{ item.icon }}</text>
            </view>
            <view class="unread-dot" v-if="item.unread"></view>
          </view>
          <view class="msg-body">
            <view class="msg-top-row">
              <text class="msg-name">{{ item.name }}</text>
              <text class="msg-time">{{ item.time }}</text>
            </view>
            <text class="msg-preview">{{ item.preview }}</text>
          </view>
        </view>
      </view>

      <!-- Empty state -->
      <view class="empty-state" v-if="!systemLoading && filteredMessages.length === 0">
        <text class="empty-icon">🔕</text>
        <text class="empty-text">{{ t('messages.empty') }}</text>
        <text class="empty-sub">{{ t('messages.emptySub') }}</text>
      </view>

      <!-- Reserve space above tab bar to prevent last row from being covered. -->
      <view class="bottom-safe"></view>
    </scroll-view>

  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useI18n } from '@/locales';
import { onShow } from '@dcloudio/uni-app';
import { getTopSafeHeight } from '@/utils/safeArea';
import {
  listNotificationsApi,
  markReadApi,
  markAllReadApi,
  type Notification,
} from '@/api/notification';
import { useTheme } from '@/utils/theme';

const { t } = useI18n();
const topSafeHeight = ref(88);
const { themeClass, fontClass, refresh: refreshTheme } = useTheme();

// ─── F9: Category tabs ───────────────────────────────────────────────────────
const TABS = computed(() => [
  { key: 'ALL',       label: t('messages.tabAll') },
  { key: 'CAREER',    label: t('messages.tabCareer') },
  { key: 'SYSTEM',    label: t('messages.tabSystem') },
  { key: 'AI',        label: t('messages.tabAI') },
]);

type TabKey = 'ALL' | 'CAREER' | 'SYSTEM' | 'AI';

const TAB_TYPES: Record<TabKey, string[]> = {
  ALL:    [],
  CAREER: ['INTERVIEW_REPORT', 'ASSESSMENT_RESULT', 'RESUME_DIAGNOSIS', 'WEEKLY_REPORT', 'STREAK_WARNING', 'MARKET_LIKE'],
  SYSTEM: ['SYSTEM', 'ADMIN_BROADCAST'],
  AI:     ['AI_PROACTIVE'],
};

const activeTab = ref<TabKey>('ALL');

// ─── Icon & label maps (all 9 types) ────────────────────────────────────────
const iconForType = (type: string): string => {
  switch (type) {
    case 'INTERVIEW_REPORT':  return '🎤';
    case 'INTERVIEW_COMPLETED': return '🎤';
    case 'ASSESSMENT_RESULT': return '🧠';
    case 'ASSESSMENT_DONE':   return '🧠';
    case 'RESUME_DIAGNOSIS':  return '📄';
    case 'RESUME_REVIEWED':   return '📄';
    case 'WEEKLY_REPORT':     return '📊';
    case 'STREAK_WARNING':    return '🔥';
    case 'MARKET_LIKE':       return '❤️';
    case 'AI_PROACTIVE':      return '🤖';
    case 'ADMIN_BROADCAST':   return '📢';
    default:                  return '🔔';
  }
};
const nameForType = (type: string): string => {
  switch (type) {
    case 'INTERVIEW_REPORT':
    case 'INTERVIEW_COMPLETED': return t('messages.typeInterview');
    case 'ASSESSMENT_RESULT':
    case 'ASSESSMENT_DONE':     return t('messages.typeAssessment');
    case 'RESUME_DIAGNOSIS':
    case 'RESUME_REVIEWED':     return t('messages.typeResumeAI');
    case 'WEEKLY_REPORT':       return t('messages.typeWeeklyReport');
    case 'STREAK_WARNING':      return t('messages.typeCheckin');
    case 'MARKET_LIKE':         return t('messages.typeMarket');
    case 'AI_PROACTIVE':        return t('messages.typeAI');
    case 'ADMIN_BROADCAST':     return t('messages.typeAnnouncement');
    default:                    return t('messages.typeSystem');
  }
};

// System notifications come from the backend (/api/notifications).
// Each row is a Notification + a derived UI shape used by the existing template.
interface SystemMessageView {
  notificationId: number;
  type: string;
  icon: string;
  name: string;
  time: string;
  preview: string;
  unread: boolean;
  link?: string;
}
const systemMessages = ref<SystemMessageView[]>([]);
const systemLoading = ref(false);
const unreadCount = computed(() => systemMessages.value.filter((m) => m.unread).length);

const filteredMessages = computed(() => {
  if (activeTab.value === 'ALL') return systemMessages.value;
  const allowed = TAB_TYPES[activeTab.value];
  return systemMessages.value.filter((m) => allowed.includes(m.type));
});

const unreadByTab = computed<Record<string, number>>(() => {
  const out: Record<string, number> = { ALL: 0, CAREER: 0, SYSTEM: 0, AI: 0 };
  systemMessages.value.forEach((m) => {
    if (!m.unread) return;
    out['ALL']++;
    for (const key of Object.keys(TAB_TYPES) as TabKey[]) {
      if (TAB_TYPES[key].includes(m.type)) { out[key]++; break; }
    }
  });
  return out;
});

const formatRelativeTime = (ts?: string): string => {
  if (!ts) return '';
  const date = new Date(ts.replace(' ', 'T'));
  if (isNaN(date.getTime())) return '';
  const diff = Math.max(0, Math.floor((Date.now() - date.getTime()) / 1000));
  if (diff < 60) return t('messages.timeJustNow');
  const m = Math.floor(diff / 60);
  if (m < 60) return t('messages.timeMins', { m });
  const h = Math.floor(m / 60);
  if (h < 24) return t('messages.timeHours', { h });
  const d = Math.floor(h / 24);
  if (d === 1) return t('messages.timeYesterday');
  if (d < 7) return t('messages.timeDays', { d });
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const loadSystemNotifications = async () => {
  systemLoading.value = true;
  try {
    const list: Notification[] = (await listNotificationsApi()) || [];
    systemMessages.value = list.map((n) => ({
      notificationId: n.notificationId,
      type: n.type,
      icon: iconForType(n.type),
      name: nameForType(n.type) + (n.title ? ' · ' + n.title : ''),
      time: formatRelativeTime(n.createdAt),
      preview: n.content || '',
      unread: !n.readFlag,
      link: n.link,
    }));
  } catch {
    systemMessages.value = [];
  } finally {
    systemLoading.value = false;
  }
};

const markAllReadHandler = async () => {
  // Optimistic UI: flip every row, then call the backend.
  systemMessages.value.forEach((m) => { m.unread = false; });
  try {
    await markAllReadApi();
    uni.showToast({ title: 'All marked read', icon: 'success' });
  } catch (e: any) {
    uni.showToast({ title: e?.message || 'Failed', icon: 'none' });
  }
};

const handleSystemClick = async (item: SystemMessageView) => {
  if (item.unread) {
    item.unread = false;
    try { await markReadApi(item.notificationId); } catch { /* best-effort */ }
  }

  if (item.link) {
    if (item.link.startsWith('/pages/')) {
      // The interview report deep link needs an interviewId — without it the
      // page boots into a 404 state. Send those orphans to the history list
      // instead so the user can still recover the right report.
      if (item.link.startsWith('/pages/interview/report')
          && !/[?&]interviewId=\d+/.test(item.link)) {
        uni.showToast({ title: 'This notification is no longer linked to a report', icon: 'none' });
        uni.navigateTo({ url: '/pages/interview/history' });
        return;
      }
      uni.navigateTo({ url: item.link });
    } else {
      uni.showToast({ title: item.link, icon: 'none' });
    }
    return;
  }

  // Legacy fallback for older notifications without a link payload.
  if (item.preview.includes('report')) {
    uni.navigateTo({ url: '/pages/interview/history' });
  } else if (item.preview.includes('diagnosis')) {
    uni.navigateTo({ url: '/pages/resume-ai/index' });
  }
};

onMounted(() => {
  refreshTheme();
  topSafeHeight.value = getTopSafeHeight();
});

// Pull notifications every time the tab becomes visible -- new alerts can
// arrive while the user is on another page (e.g. they just finished an
// interview / quiz).
onShow(() => {
  refreshTheme();
  loadSystemNotifications();
});
</script>

<style scoped>
.msg-page {
  height: 100vh;
  background: var(--page-ios-gray);
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.status-spacer {
  width: 100%;
  flex-shrink: 0;
}

.page-header {
  padding: 8px 20px 14px;
}
.header-row {
  display: flex; align-items: center; justify-content: space-between;
}
.clear-btn {
  min-height: 32px;
  padding: 6px 12px;
  background: #eff6ff;
  border: 1px solid #dbeafe;
  border-radius: 999px;
  display: flex; align-items: center;
}
.clear-btn:active { background: #dbeafe; }
.clear-btn-text { color: #2563eb; font-size: 12px; font-weight: 700; }

.page-title {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.35px;
}

.page-subtitle {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-secondary);
}

/* ---- Segment tabs ---- */
.segment-wrap {
  padding: 0 20px 16px;
}

.segment-bar {
  display: flex;
  background: #ffffff;
  border: 1px solid #b8c8d8;
  border-radius: 14px;
  padding: 4px;
  gap: 2px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.11);
}

.seg-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 36px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
  transition: all 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
  position: relative;
  gap: 6px;
}

.seg-active {
  background: var(--surface-2);
  color: #0f172a;
  font-weight: 600;
  box-shadow: none;
}

.seg-badge {
  min-width: 18px;
  height: 18px;
  border-radius: 9px;
  background: #ef4444;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
}

.badge-num,
.seg-badge-text {
  font-size: 10px;
  font-weight: 700;
  color: #ffffff;
}

/* ---- Message list ---- */
.msg-list {
  flex: 1;
  height: 0;
  min-height: 0;
  padding-bottom: calc(16px + env(safe-area-inset-bottom, 0px));
  box-sizing: border-box;
}

.list-wrap {
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bottom-safe {
  height: calc(var(--tab-bar-height, 50px) + 20px);
}

.msg-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid #b8c8d8;
  box-shadow: 0 3px 12px rgba(0,0,0,0.13), 0 1px 4px rgba(0,0,0,0.07);
  transition: transform 0.1s;
}

.msg-card:active {
  transform: scale(0.98);
}

.msg-unread {
  border-color: rgba(37, 99, 235, 0.35);
  background: linear-gradient(0deg, rgba(239, 246, 255, 0.6), rgba(239, 246, 255, 0.6)), #ffffff;
}

/* ---- Avatar ---- */
.avatar-wrap {
  position: relative;
  margin-right: 14px;
  flex-shrink: 0;
}

.msg-avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.av-text {
  font-size: 18px;
  font-weight: 700;
  color: #ffffff;
}

.av-0 { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.av-1 { background: linear-gradient(135deg, #10b981, #059669); }
.av-2 { background: linear-gradient(135deg, #f97316, #ea580c); }

.sys-av {
  border-radius: 16px;
}
.sys-0 { background: linear-gradient(135deg, #fef08a, #fbbf24); }
.sys-1 { background: linear-gradient(135deg, #c4b5fd, #8b5cf6); }
.sys-2 { background: linear-gradient(135deg, #6ee7b7, #10b981); }

.status-av {
  background: linear-gradient(135deg, #dbeafe, #bfdbfe);
  border-radius: 16px;
}

.av-emoji {
  font-size: 22px;
}

.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  border-radius: 5px;
  background: #2563eb;
  border: 2px solid #ffffff;
}

/* ---- Message body ---- */
.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.msg-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  margin-right: 8px;
}

.msg-time {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.msg-preview {
  font-size: var(--font-caption);
  color: var(--text-secondary);
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: var(--line-height-caption);
}

/* ---- Status tags ---- */
.status-tag {
  display: inline-flex;
  margin-top: 8px;
  padding: 3px 10px;
  border-radius: 8px;
}

.tag-text {
  font-size: 11px;
  font-weight: 600;
}

.tag-progress {
  background: #eff6ff;
}
.tag-progress .tag-text { color: #2563eb; }

.tag-sent {
  background: #f0fdf4;
}
.tag-sent .tag-text { color: #16a34a; }

.tag-pass {
  background: #fef3c7;
}
.tag-pass .tag-text { color: #d97706; }

/* ---- Empty state ---- */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 20px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: #94a3b8;
  margin-bottom: 6px;
}

.empty-sub {
  font-size: 13px;
  color: #cbd5e1;
}

.is-dark {
  background: #0f172a;
}

.is-dark .page-title,
.is-dark .msg-name {
  color: #f8fafc;
}

.is-dark .msg-card {
  background: #1e293b;
  box-shadow: none;
}

.is-dark .msg-preview,
.is-dark .msg-time {
  color: #94a3b8;
}

.is-dark .segment-bar { background: #1e293b; border-color: #334155; }
.is-dark .seg-item { color: #94a3b8; }
.is-dark .seg-active { background: #334155; color: #f8fafc; }
.is-dark .clear-btn { background: rgba(37, 99, 235, 0.15); border-color: rgba(37, 99, 235, 0.3); }
.is-dark .clear-btn-text { color: #60a5fa; }
.is-dark .msg-unread { background: linear-gradient(0deg, rgba(37, 99, 235, 0.15), rgba(37, 99, 235, 0.15)), #1e293b; border-color: rgba(37, 99, 235, 0.35); }
.is-dark .tag-progress { background: rgba(37, 99, 235, 0.15); }
.is-dark .tag-progress .tag-text { color: #60a5fa; }
.is-dark .tag-sent { background: rgba(16, 185, 129, 0.15); }
.is-dark .tag-sent .tag-text { color: #34d399; }
.is-dark .tag-pass { background: rgba(245, 158, 11, 0.15); }
.is-dark .tag-pass .tag-text { color: #fbbf24; }
.is-dark .empty-text { color: #94a3b8; }
.is-dark .empty-sub { color: #64748b; }
.is-dark .status-av { background: linear-gradient(135deg, #1e3a5f, #1e40af); }
.is-dark .unread-dot { border-color: #1e293b; }

/* ================================================================
 *  MP-WEIXIN parity overrides — HARDCODED values, no CSS vars.
 * ================================================================ */
/* #ifdef MP-WEIXIN */

.msg-page {
  background-color: #eaeff5;
}

.msg-card {
  overflow: visible;
  border: 1.5px solid #b0bfd0;
  box-shadow: 0 3px 14px rgba(0,0,0,0.18),
              0 1px 5px  rgba(0,0,0,0.10);
}

.segment-bar {
  overflow: visible;
  border: 1.5px solid #b0bfd0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.14);
}

.seg-active {
  background: #dbeafe;
  color: #2563eb;
  box-shadow: 0 2px 8px rgba(37,99,235,0.22);
}

/* Dark theme on MP: the block above is light-first; pin dark styles with
   higher specificity so the segment bar never stays a white slab. */
.msg-page.is-dark {
  background-color: #0f172a;
}

.msg-page.is-dark .segment-bar {
  background: #1e293b;
  border-color: #334155;
  box-shadow: none;
}

.msg-page.is-dark .seg-item {
  color: #94a3b8;
}

.msg-page.is-dark .seg-active {
  background: #334155;
  color: #f8fafc;
  box-shadow: none;
}

.msg-page.is-dark .msg-card {
  background: #1e293b;
  border-color: #334155;
  box-shadow: none;
}

.msg-page.is-dark .msg-name {
  color: #f8fafc;
}

.msg-page.is-dark .msg-preview,
.msg-page.is-dark .msg-time {
  color: #94a3b8;
}

.msg-page.is-dark .clear-btn {
  background: rgba(37, 99, 235, 0.15);
  border-color: rgba(37, 99, 235, 0.3);
}

.msg-page.is-dark .clear-btn-text {
  color: #60a5fa;
}

/* #endif */
</style>
