<template>
  <view class="msg-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="page-header">
      <text class="page-title">Messages</text>
    </view>

    <!-- iOS segment tabs -->
    <view class="segment-wrap">
      <view class="segment-bar">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="seg-item"
          :class="{ 'seg-active': currentTab === tab.key }"
          @click="currentTab = tab.key"
        >
          <text>{{ tab.label }}</text>
          <view class="seg-badge" v-if="tab.count > 0">
            <text class="badge-num">{{ tab.count }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Message list -->
    <scroll-view class="msg-list" scroll-y>
      <!-- HR conversations -->
      <view v-if="currentTab === 'hr'" class="list-wrap">
        <view
          class="msg-card"
          :class="{ 'msg-unread': item.unread }"
          v-for="(item, idx) in hrMessages"
          :key="idx"
          @click="openHrModal(item)"
        >
          <view class="avatar-wrap">
            <view class="msg-avatar" :class="'av-' + (idx % 3)">
              <text class="av-text">{{ item.avatarChar }}</text>
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

      <!-- System notifications -->
      <view v-if="currentTab === 'system'" class="list-wrap">
        <view
          class="msg-card"
          :class="{ 'msg-unread': item.unread }"
          v-for="(item, idx) in systemMessages"
          :key="idx"
          @click="handleSystemClick(item)"
        >
          <view class="avatar-wrap">
            <view class="msg-avatar sys-av" :class="'sys-' + (idx % 2)">
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

      <!-- Application status -->
      <view v-if="currentTab === 'status'" class="list-wrap">
        <view
          class="msg-card"
          v-for="(item, idx) in statusMessages"
          :key="idx"
          @click="openStatusModal(item)"
        >
          <view class="avatar-wrap">
            <view class="msg-avatar status-av">
              <text class="av-emoji">{{ item.icon }}</text>
            </view>
          </view>
          <view class="msg-body">
            <view class="msg-top-row">
              <text class="msg-name">{{ item.name }}</text>
              <text class="msg-time">{{ item.time }}</text>
            </view>
            <text class="msg-preview">{{ item.preview }}</text>
            <view class="status-tag" :class="'tag-' + item.status">
              <text class="tag-text">{{ item.statusLabel }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Empty state -->
      <view class="empty-state" v-if="currentListEmpty">
        <text class="empty-icon">📭</text>
        <text class="empty-text">No messages yet</text>
        <text class="empty-sub">New messages will appear here</text>
      </view>
    </scroll-view>

    <!-- UI Modals -->
    <!-- HR Chat Modal -->
    <view class="modal-overlay" v-if="activeHrChat" @click="activeHrChat = null">
      <view class="modal-content bottom-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="modal-header">
          <view class="msg-avatar av-0 sheet-av"><text class="av-text">{{ activeHrChat.avatarChar }}</text></view>
          <text class="modal-title">{{ activeHrChat.name }}</text>
        </view>
        <view class="modal-body">
          <view class="chat-bubble received">
            <text>{{ activeHrChat.preview }}</text>
          </view>
        </view>
        <view class="modal-actions">
          <input class="reply-input" placeholder="Type a reply..." @confirm="replyHr" />
          <button class="btn-primary" @click="viewProfile">View Profile</button>
        </view>
      </view>
    </view>

    <!-- Status Timeline Modal -->
    <view class="modal-overlay" v-if="activeStatus" @click="activeStatus = null">
      <view class="modal-content bottom-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <view class="modal-header">
          <text class="modal-title">{{ activeStatus.name }}</text>
        </view>
        <view class="modal-body">
          <view class="timeline">
            <view class="tl-item completed">
              <view class="tl-dot"></view>
              <view class="tl-line"></view>
              <view class="tl-content">
                <text class="tl-title">Application Submitted</text>
                <text class="tl-time">2 days ago</text>
              </view>
            </view>
            <view class="tl-item" :class="{ 'completed': activeStatus.status === 'progress' || activeStatus.status === 'pass' }">
              <view class="tl-dot"></view>
              <view class="tl-line"></view>
              <view class="tl-content">
                <text class="tl-title">Resume Screening</text>
                <text class="tl-time" v-if="activeStatus.status !== 'sent'">Yesterday</text>
              </view>
            </view>
            <view class="tl-item" :class="{ 'completed': activeStatus.status === 'pass' }">
              <view class="tl-dot"></view>
              <view class="tl-content">
                <text class="tl-title">Interview Round 1</text>
                <text class="tl-time" v-if="activeStatus.status === 'pass'">Today</text>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

const currentTab = ref('hr');
const topSafeHeight = ref(88);
const darkPref = ref(false);

const activeHrChat = ref<any>(null);
const activeStatus = ref<any>(null);

const tabs = ref([
  { key: 'hr', label: 'Chats', count: 2 },
  { key: 'system', label: 'Alerts', count: 1 },
  { key: 'status', label: 'Applications', count: 0 },
]);

const hrMessages = ref([
  {
    avatarChar: 'B',
    name: 'ByteDance · Ms. Zhang',
    time: '10:30',
    preview: 'Your resume passed the initial screening. Are you free for an interview this Friday?',
    unread: true,
  },
  {
    avatarChar: 'T',
    name: 'Tencent · Mr. Li',
    time: 'Yesterday',
    preview: 'Hi, I reviewed your resume. Are you interested in our frontend position?',
    unread: true,
  },
  {
    avatarChar: 'A',
    name: 'Alibaba · Ms. Wang',
    time: 'Mon',
    preview: 'Congratulations on passing the written test! Please check your interview invitation.',
    unread: false,
  },
]);

const systemMessages = ref([
  {
    icon: '🔔',
    name: 'System',
    time: 'Just now',
    preview: 'Your interview review report is ready. Tap to view details.',
    unread: true,
  },
  {
    icon: '✨',
    name: 'AI Assistant',
    time: 'Yesterday',
    preview: 'Resume diagnosis complete — overall score 85. 3 items can be optimized.',
    unread: false,
  },
]);

const statusMessages = ref([
  {
    icon: '🚀',
    name: 'Frontend Engineer · ByteDance',
    time: '14:20',
    preview: 'Status update: Moved to secondary screening. Stay tuned.',
    status: 'progress',
    statusLabel: 'Screening',
  },
  {
    icon: '📝',
    name: 'Full Stack Engineer · Meituan',
    time: '2 days ago',
    preview: 'Application submitted: Your resume has been delivered to the recruiter.',
    status: 'sent',
    statusLabel: 'Submitted',
  },
  {
    icon: '✅',
    name: 'Backend Intern · Xiaohongshu',
    time: 'Last week',
    preview: 'Congratulations! You passed the final interview. Offer coming within 3 business days.',
    status: 'pass',
    statusLabel: 'Passed',
  },
]);

const currentListEmpty = computed(() => {
  if (currentTab.value === 'hr') return hrMessages.value.length === 0;
  if (currentTab.value === 'system') return systemMessages.value.length === 0;
  if (currentTab.value === 'status') return statusMessages.value.length === 0;
  return true;
});

const openHrModal = (item: any) => {
  item.unread = false; // Mark as read
  // Update badge count
  const unreadCount = hrMessages.value.filter(m => m.unread).length;
  tabs.value[0].count = unreadCount;
  activeHrChat.value = item;
};

const replyHr = () => {
  activeHrChat.value = null;
  uni.showToast({ title: 'Reply sent', icon: 'success' });
};

const viewProfile = () => {
  uni.showToast({ title: 'Coming in next release', icon: 'none' });
};

const handleSystemClick = (item: any) => {
  item.unread = false;
  const unreadCount = systemMessages.value.filter(m => m.unread).length;
  tabs.value[1].count = unreadCount;

  if (item.preview.includes('report')) {
    uni.navigateTo({ url: '/pages/interview/report' });
  } else if (item.preview.includes('diagnosis')) {
    uni.navigateTo({ url: '/pages/resume-ai/index' });
  }
};

const openStatusModal = (item: any) => {
  activeStatus.value = item;
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const systemInfo = uni.getSystemInfoSync();
  const menuButton = uni.getMenuButtonBoundingClientRect?.();
  if (menuButton && menuButton.top) {
    topSafeHeight.value = menuButton.top;
  } else {
    const statusBar = systemInfo.statusBarHeight || 44;
    topSafeHeight.value = statusBar + 8;
  }
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
  padding: 8px 20px 12px;
}

.page-title {
  font-size: var(--font-hero);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

/* ---- Segment tabs ---- */
.segment-wrap {
  padding: 0 20px 16px;
}

.segment-bar {
  display: flex;
  background: #e8ecf1;
  border-radius: 12px;
  padding: 3px;
  gap: 2px;
  border: 1px solid var(--border-color);
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
  background: #ffffff;
  color: #0f172a;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
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

.badge-num {
  font-size: 10px;
  font-weight: 700;
  color: #ffffff;
}

/* ---- Message list ---- */
.msg-list {
  flex: 1;
  height: 0;
  min-height: 0;
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
  box-sizing: border-box;
}

.list-wrap {
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.msg-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  padding: 16px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-strong);
  box-shadow: 0 5px 16px rgba(15, 23, 42, 0.08);
  transition: transform 0.1s;
}

.msg-card:active {
  transform: scale(0.98);
}

.msg-unread {
  border-left: 3px solid #2563eb;
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

/* ---- Modals ---- */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4); z-index: 1000;
  display: flex; align-items: flex-end;
}

.modal-content.bottom-sheet {
  width: 100%; background: #ffffff;
  border-radius: 24px 24px 0 0; padding: 16px 20px 32px;
  box-sizing: border-box; min-height: 250px;
  border-top: 1px solid var(--border-color);
}

.sheet-handle {
  width: 36px; height: 5px; border-radius: 3px; background: #e2e8f0;
  margin: 0 auto 16px;
}

.modal-header {
  display: flex; align-items: center; margin-bottom: 24px;
}

.sheet-av { width: 40px; height: 40px; margin-right: 12px; }

.modal-title { font-size: 18px; font-weight: 700; color: #0f172a; }

.chat-bubble {
  background: #f1f5f9; padding: 14px 16px; border-radius: 4px 16px 16px 16px;
  font-size: 15px; color: #1e293b; line-height: 1.5; margin-bottom: 24px;
}

.modal-actions {
  display: flex; flex-direction: column; gap: 12px;
}

.reply-input {
  width: 100%; height: 48px; background: #f8fafc; border: 1.5px solid #e2e8f0;
  border-radius: 12px; padding: 0 16px; font-size: 15px; box-sizing: border-box;
}

.btn-primary {
  width: 100%; height: 48px; background: #2563eb; color: #fff;
  border-radius: 12px; font-weight: 600; line-height: 48px; border: none;
}

.timeline { display: flex; flex-direction: column; gap: 0; padding-left: 8px; }

.tl-item { position: relative; padding-left: 28px; padding-bottom: 24px; }
.tl-item:last-child { padding-bottom: 8px; }

.tl-dot {
  position: absolute; left: 0; top: 4px; width: 12px; height: 12px;
  border-radius: 6px; background: #cbd5e1; z-index: 2;
}

.tl-line {
  position: absolute; left: 5.5px; top: 16px; bottom: -4px; width: 1px;
  background: #e2e8f0; z-index: 1;
}

.tl-item.completed .tl-dot { background: #2563eb; }
.tl-item.completed .tl-line { background: #2563eb; }

.tl-content { display: flex; flex-direction: column; gap: 4px; }
.tl-title { font-size: 15px; font-weight: 600; color: #1e293b; }
.tl-time { font-size: 12px; color: #94a3b8; }
.tl-item:not(.completed) .tl-title { color: #94a3b8; font-weight: 500; }

.is-dark {
  background: #0f172a;
}

.is-dark .page-title,
.is-dark .seg-active,
.is-dark .msg-name {
  color: #f8fafc;
}

.is-dark .segment-bar,
.is-dark .msg-card {
  background: #1e293b;
  box-shadow: none;
}

.is-dark .seg-item,
.is-dark .msg-preview,
.is-dark .msg-time {
  color: #94a3b8;
}

.is-dark .modal-content { background: #1e293b; }
.is-dark .sheet-handle { background: #334155; }
.is-dark .modal-title { color: #f8fafc; }
.is-dark .chat-bubble { background: #0f172a; color: #e2e8f0; }
.is-dark .reply-input { background: #0f172a; border-color: #334155; color: #f8fafc; }
.is-dark .tl-title { color: #f8fafc; }
.is-dark .tl-item:not(.completed) .tl-title { color: #64748b; }
.is-dark .tl-dot { background: #334155; }
.is-dark .tl-line { background: #334155; }
</style>
