<template>
  <view class="history-page" :class="[themeClass, fontClass]">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>
    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">Back</text>
      </view>
      <text class="nav-title">Interviews</text>
      <view class="nav-new-btn" @click="startNew">
        <text class="nav-new-text">+ New</text>
      </view>
    </view>

    <view class="page-hero">
      <text class="hero-title">Interview History</text>
      <text class="hero-subtitle">Review previous sessions or start a fresh practice run.</text>
    </view>

    <!-- Loading skeleton -->
    <view class="skeleton-list" v-if="loading">
      <view class="skel-card" v-for="i in 3" :key="i">
        <view class="skel-line skel-w60"></view>
        <view class="skel-line skel-w40"></view>
        <view class="skel-line skel-w80"></view>
      </view>
    </view>

    <view class="list" v-else-if="groupedInterviews.length > 0">
      <template v-for="group in groupedInterviews" :key="group.label">
        <text class="group-label">{{ group.label }}</text>
        <view
          v-for="item in group.items"
          :key="item.interviewId"
          class="interview-card"
          @click="viewDetail(item)"
        >
          <view class="card-top">
            <text class="position">{{ item.positionName }}</text>
            <view :class="['status-pill', (item.status ?? '').toLowerCase()]">
              <text class="pill-text">{{ item.status === 'COMPLETED' ? 'Completed' : 'Ongoing' }}</text>
            </view>
          </view>
          <view class="card-bottom">
            <view class="info-item">
              <text class="info-label">Difficulty</text>
              <text class="info-val">{{ item.difficulty }}</text>
            </view>
            <view class="info-item" v-if="item.finalScore != null">
              <text class="info-label">Score</text>
              <text class="info-val score-val">{{ item.finalScore }}</text>
            </view>
            <view class="info-item info-item-time">
              <text class="info-label">Time</text>
              <text class="info-val">{{ formatTime(item.startedAt) }}</text>
            </view>
          </view>
        </view>
      </template>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">💼</text>
      <text class="empty-text">No interview history yet</text>
      <text class="empty-desc">Start your first mock interview to generate a session record and score history.</text>
      <button class="btn-primary" @click="startNew">Start your first interview</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getTopSafeHeight } from '@/utils/safeArea';
import { getUserInterviewsApi, type Interview } from '@/api/interview';
import { useTheme } from '@/utils/theme';

const interviews = ref<Interview[]>([]);
const loading = ref(true);
const { themeClass, fontClass, refresh: refreshTheme } = useTheme();
const topSafeHeight = ref(52);

const goBack = () => uni.navigateBack({ delta: 1 });

const loadInterviews = async () => {
  const userId = uni.getStorageSync('userId');
  const numericId = Number(userId);
  if (!userId || isNaN(numericId) || numericId <= 0) {
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    interviews.value = await getUserInterviewsApi(numericId);
  } catch (error) {
    console.error('Failed to load interviews:', error);
    uni.showToast({ title: 'Failed to load interviews', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  refreshTheme();
  topSafeHeight.value = getTopSafeHeight();
});

// Refresh every time the page becomes visible — in particular when the
// user finishes an interview and pops back to history, the new entry
// should appear without a manual reload.
onShow(() => {
  refreshTheme();
  loadInterviews();
});

const startNew = () => {
  uni.navigateTo({ url: '/pages/interview/start' });
};

/**
 * Tapping a card always leads somewhere useful:
 *   ONGOING   → resume the same modality the candidate originally chose
 *               (VOICE → room.vue, TEXT/unknown → chat.vue) so we never
 *               silently downgrade a digital-human session into a text chat.
 *   COMPLETED → open the AI evaluation report
 *   CANCELLED → toast
 */
const viewDetail = (item: Interview) => {
  if (item.status === 'ONGOING') {
    const isVoice = (item.mode || '').toUpperCase() === 'VOICE';
    const target = isVoice
      ? `/pages/interview/room?interviewId=${item.interviewId}`
      : `/pages/interview/chat?interviewId=${item.interviewId}`;
    uni.navigateTo({ url: target });
  } else if (item.status === 'COMPLETED') {
    uni.navigateTo({ url: `/pages/interview/report?interviewId=${item.interviewId}` });
  } else {
    uni.showToast({ title: 'This interview was cancelled', icon: 'none' });
  }
};

const formatTime = (dateStr?: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr.replace(' ', 'T'));
  return `${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};

const startOfDay = (d: Date) => new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();

const groupedInterviews = computed(() => {
  if (!interviews.value.length) return [] as Array<{ label: string; items: Interview[] }>;
  const today = startOfDay(new Date());
  const yesterday = today - 86400000;
  const sevenAgo = today - 6 * 86400000;

  const buckets: Record<string, Interview[]> = { Today: [], Yesterday: [], 'This Week': [], Older: [] };
  for (const it of interviews.value) {
    if (!it.startedAt) { buckets['Older'].push(it); continue; }
    const day = startOfDay(new Date(it.startedAt.replace(' ', 'T')));
    if (day === today) buckets['Today'].push(it);
    else if (day === yesterday) buckets['Yesterday'].push(it);
    else if (day >= sevenAgo) buckets['This Week'].push(it);
    else buckets['Older'].push(it);
  }
  return Object.entries(buckets)
    .filter(([, items]) => items.length > 0)
    .map(([label, items]) => ({ label, items }));
});
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  padding: 0 20px 24px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.status-spacer { width: 100%; }

.nav-row {
  display: flex; align-items: center;
  height: 44px; padding: 0 2px; margin-bottom: 4px;
}

.back-btn {
  display: inline-flex; align-items: center; gap: 2px;
  color: #2563eb; width: 64px;
}

.back-icon { font-size: 24px; font-weight: 300; line-height: 1; }
.back-text { font-size: 16px; font-weight: 500; }

.nav-title {
  flex: 1; text-align: center;
  font-size: 17px; font-weight: 600;
  color: #0f172a; letter-spacing: -0.3px;
}

/* "+ New" target raised to a comfortable 44pt thumb zone (HCI: Apple HIG / WCAG 2.5.5) */
.nav-new-btn {
  min-width: 64px;
  min-height: 44px;
  display: flex; justify-content: flex-end; align-items: center;
  padding: 0 6px;
}

.nav-new-text {
  font-size: 15px; font-weight: 600; color: #2563eb;
}

/* Section labels between date buckets */
.group-label {
  display: block;
  margin: 6px 4px 4px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--text-tertiary);
  text-transform: uppercase;
}

/* Skeleton loader */
.skeleton-list { display: flex; flex-direction: column; gap: 12px; }
.skel-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 20px 18px;
  display: flex; flex-direction: column; gap: 10px;
}
.skel-line {
  height: 12px;
  border-radius: 6px;
  background: linear-gradient(90deg, #eef2f7 0%, #f7fafc 50%, #eef2f7 100%);
  background-size: 200% 100%;
  animation: skel-shimmer 1.4s infinite;
}
.skel-w40 { width: 40%; }
.skel-w60 { width: 60%; }
.skel-w80 { width: 80%; }
@keyframes skel-shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.page-hero { margin-bottom: 20px; padding: 0 2px; }

.hero-title {
  font-size: var(--font-hero); font-weight: 800;
  color: var(--text-primary); letter-spacing: -0.5px;
  display: block; margin-bottom: 6px;
}

.hero-subtitle {
  display: block; font-size: 14px; line-height: 1.5;
  color: var(--text-secondary);
}

.list { display: flex; flex-direction: column; gap: 12px; }

.interview-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 18px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.15s;
}

.interview-card:active { transform: scale(0.98); }

.card-top {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 14px;
}

.position { font-size: 17px; font-weight: 600; color: #1e293b; }

.status-pill { padding: 4px 12px; border-radius: 10px; }

.pill-text { font-size: 12px; font-weight: 600; }

.completed { background: #dcfce7; }
.completed .pill-text { color: #16a34a; }

.ongoing { background: #fef3c7; }
.ongoing .pill-text { color: #d97706; }

.card-bottom { display: flex; gap: 20px; }

.info-item { display: flex; flex-direction: column; gap: 2px; }

.info-label { font-size: 11px; color: #94a3b8; font-weight: 500; text-transform: uppercase; letter-spacing: 0.3px; }

.info-val { font-size: 14px; color: #334155; font-weight: 500; }

.score-val { color: #2563eb; font-weight: 700; }

.empty { text-align: center; padding: 80px 20px; }

.empty-icon { font-size: 56px; display: block; margin-bottom: 16px; }

.empty-text {
  font-size: 16px; color: #94a3b8; font-weight: 500;
  display: block; margin-bottom: 24px;
}

.empty-desc {
  display: block;
  margin: -14px 0 24px;
  font-size: 13px;
  line-height: 1.45;
  color: var(--text-secondary);
}

.btn-primary {
  background: #2563eb; color: #fff; font-size: 16px; font-weight: 600;
  border-radius: 14px; height: 48px; line-height: 48px; border: none;
}

.btn-primary:active { background: #1d4ed8; }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .nav-title,
.is-dark .hero-title { color: #f8fafc; }

.is-dark .hero-subtitle { color: #94a3b8; }

.is-dark .title,
.is-dark .position { color: #f8fafc; }

.is-dark .subtitle,
.is-dark .empty-desc { color: #94a3b8; }

.is-dark .interview-card { background: #1e293b; box-shadow: none; }

.is-dark .info-val { color: #e2e8f0; }

/* Status pills: light-mode pastels + saturated text; dark mode needs its own pair */
.is-dark .status-pill.completed {
  background: rgba(16, 185, 129, 0.22);
}

.is-dark .status-pill.completed .pill-text {
  color: #34d399;
}

.is-dark .status-pill.ongoing {
  background: rgba(245, 158, 11, 0.22);
}

.is-dark .status-pill.ongoing .pill-text {
  color: #fbbf24;
}

.is-dark .skel-card {
  background: #1e293b;
  border-color: #334155;
}

.is-dark .skel-line {
  background: linear-gradient(90deg, #1e293b 0%, #334155 50%, #1e293b 100%);
  background-size: 200% 100%;
}

.is-dark .group-label {
  color: #64748b;
}

.is-dark .nav-title {
  color: #f8fafc;
}

.is-dark .back-btn {
  color: #93c5fd;
}

.is-dark .nav-new-text {
  color: #93c5fd;
}
</style>
