<template>
  <view class="history-page" :class="[themeClass, fontClass]">
    <view class="nav" :style="{ paddingTop: topSafe + 'px' }">
      <view class="nav-row">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
          <text class="back-text">Back</text>
        </view>
        <text class="nav-title">AI Chat History</text>
        <view class="nav-placeholder"></view>
      </view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="retention-card">
        <text class="retention-title">Cloud history retention</text>
        <text class="retention-desc">Your assistant conversations are saved in the cloud so the AI can continue context and build long-term memory. They are kept until you delete a session or remove your account.</text>
        <text class="retention-desc">Deleting a session removes its messages. AI-extracted facts can be managed separately on the AI Memory page.</text>
      </view>

      <view v-if="loading" class="loading-card">
        <text class="loading-text">Loading history...</text>
      </view>

      <view v-else-if="sessions.length === 0" class="empty-state">
        <text class="empty-icon">💬</text>
        <text class="empty-title">No saved conversations yet</text>
        <text class="empty-desc">Start chatting with 小职, 小严, or 小面. Your cloud sessions will appear here after the first saved reply.</text>
      </view>

      <view v-else class="session-list">
        <view
          v-for="session in sessions"
          :key="session.sessionId"
          class="session-card"
          @click="openSession(session)"
        >
          <view class="session-top">
            <view class="persona-pill">
              <text class="persona-emoji">{{ personaMeta(session.persona).emoji }}</text>
              <text class="persona-name">{{ personaMeta(session.persona).label }}</text>
            </view>
            <text class="session-time">{{ formatTime(session.updatedAt || session.createdAt) }}</text>
          </view>
          <text class="session-title">{{ session.title || 'New Conversation' }}</text>
          <view class="session-bottom">
            <text class="session-hint">Tap to continue this conversation</text>
            <view class="delete-btn" @click.stop="confirmDelete(session)">
              <text class="delete-text">Delete</text>
            </view>
          </view>
        </view>
      </view>

      <view class="bottom-safe"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getTopSafeHeight } from '@/utils/safeArea';
import request from '@/utils/request';
import { useTheme } from '@/utils/theme';

interface AssistantSession {
  sessionId: number;
  userId: number;
  title?: string;
  persona?: 'MENTOR' | 'CHALLENGER' | 'INTERVIEWER' | string;
  createdAt?: string;
  updatedAt?: string;
}

const { themeClass, fontClass, refresh: refreshTheme } = useTheme();
const topSafe = ref(44);
const loading = ref(true);
const sessions = ref<AssistantSession[]>([]);

const personaMeta = (persona?: string) => {
  const map: Record<string, { emoji: string; label: string }> = {
    MENTOR: { emoji: '🧭', label: '小职' },
    CHALLENGER: { emoji: '💪', label: '小严' },
    INTERVIEWER: { emoji: '🎙️', label: '小面' },
  };
  return map[persona || 'MENTOR'] || map.MENTOR;
};

const loadSessions = async () => {
  loading.value = true;
  try {
    const userId = Number(uni.getStorageSync('userId'));
    if (!userId) {
      sessions.value = [];
      return;
    }
    const res = await request<AssistantSession[]>({ url: `/api/chat/history/${userId}`, method: 'GET' });
    sessions.value = Array.isArray(res) ? res : [];
  } catch (e: any) {
    uni.showToast({ title: e?.message || 'Failed to load history', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const formatTime = (value?: string) => {
  if (!value) return '';
  const d = new Date(value.replace(' ', 'T'));
  if (Number.isNaN(d.getTime())) return value;
  const now = new Date();
  const sameDay = d.toDateString() === now.toDateString();
  const time = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  if (sameDay) return `Today ${time}`;
  return `${d.getMonth() + 1}/${d.getDate()} ${time}`;
};

const openSession = (session: AssistantSession) => {
  uni.setStorageSync('assistantOpenSession', {
    sessionId: session.sessionId,
    persona: session.persona || 'MENTOR',
  });
  uni.switchTab({ url: '/pages/assistant/index' });
};

const confirmDelete = (session: AssistantSession) => {
  uni.showModal({
    title: 'Delete Conversation',
    content: `Delete "${session.title || 'New Conversation'}" and all messages in it?`,
    confirmText: 'Delete',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await request({ url: `/api/chat/history/session/${session.sessionId}`, method: 'DELETE' });
        sessions.value = sessions.value.filter((s) => s.sessionId !== session.sessionId);
        uni.showToast({ title: 'Deleted', icon: 'success' });
      } catch (e: any) {
        uni.showToast({ title: e?.message || 'Failed to delete', icon: 'none' });
      }
    },
  });
};

const goBack = () => uni.navigateBack();

onMounted(() => {
  refreshTheme();
  topSafe.value = getTopSafeHeight();
  loadSessions();
});
</script>

<style scoped>
.history-page { min-height: 100vh; background: #e8eef5; font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif; }
.nav { background: rgba(255,255,255,0.9); backdrop-filter: blur(24px); border-bottom: 1px solid rgba(148,163,184,0.22); }
.nav-row { height: 52px; display: flex; align-items: center; justify-content: space-between; padding: 0 16px; box-sizing: border-box; }
.back-btn { min-width: 68px; display: flex; align-items: center; gap: 2px; }
.back-icon { font-size: 26px; color: #2563eb; line-height: 1; }
.back-text { font-size: 15px; color: #2563eb; font-weight: 600; }
.nav-title { font-size: 17px; font-weight: 800; color: #0f172a; }
.nav-placeholder { width: 68px; }
.content { height: calc(100vh - 96px); padding: 16px; box-sizing: border-box; }
.retention-card, .loading-card, .session-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 18px; box-shadow: 0 4px 14px rgba(15,23,42,0.06); }
.retention-card { padding: 16px; margin-bottom: 14px; }
.retention-title { display: block; font-size: 16px; font-weight: 800; color: #0f172a; margin-bottom: 8px; }
.retention-desc { display: block; font-size: 13px; line-height: 1.55; color: #64748b; margin-top: 6px; }
.loading-card { padding: 18px; text-align: center; }
.loading-text { font-size: 13px; color: #64748b; }
.empty-state { padding: 56px 18px; text-align: center; }
.empty-icon { display: block; font-size: 48px; margin-bottom: 10px; }
.empty-title { display: block; font-size: 17px; font-weight: 800; color: #1e293b; margin-bottom: 8px; }
.empty-desc { display: block; font-size: 13px; line-height: 1.55; color: #64748b; }
.session-list { display: flex; flex-direction: column; gap: 12px; }
.session-card { padding: 14px; }
.session-card:active { transform: scale(0.99); background: #f8fafc; }
.session-top, .session-bottom { display: flex; align-items: center; justify-content: space-between; gap: 10px; }
.persona-pill { display: flex; align-items: center; gap: 5px; background: #eff6ff; border-radius: 999px; padding: 5px 9px; }
.persona-emoji { font-size: 14px; }
.persona-name { font-size: 12px; font-weight: 800; color: #2563eb; }
.session-time { font-size: 12px; color: #94a3b8; }
.session-title { display: block; font-size: 15px; font-weight: 800; line-height: 1.45; color: #0f172a; margin: 12px 0; }
.session-hint { font-size: 12px; color: #64748b; }
.delete-btn { border: 1px solid #fecaca; border-radius: 999px; padding: 5px 10px; }
.delete-text { font-size: 12px; font-weight: 700; color: #ef4444; }
.bottom-safe { height: 48px; }
.is-dark.history-page { background: #0f172a; }
.is-dark .nav { background: rgba(15,23,42,0.9); border-color: #334155; }
.is-dark .nav-title, .is-dark .retention-title, .is-dark .session-title, .is-dark .empty-title { color: #f8fafc; }
.is-dark .retention-card, .is-dark .loading-card, .is-dark .session-card { background: #1e293b; border-color: #334155; box-shadow: none; }
.is-dark .retention-desc, .is-dark .loading-text, .is-dark .empty-desc, .is-dark .session-hint, .is-dark .session-time { color: #94a3b8; }
.is-dark .persona-pill { background: rgba(37,99,235,0.16); }
.is-dark .session-card:active { background: #334155; }
</style>
