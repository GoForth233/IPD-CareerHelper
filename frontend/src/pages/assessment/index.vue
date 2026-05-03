<template>
  <view class="assessment-container" :class="{ 'is-dark': darkPref }">
    <view class="status-bar-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="page-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">Back</text>
      </view>
      <text class="page-title">Assessment</text>
      <view class="header-action"></view>
    </view>

    <view class="page-summary">
      <text class="summary-title">Build your profile first</text>
      <text class="summary-text">These tests feed your Skill Map and AI advisor, so the first step should feel clear and low-friction.</text>
    </view>

    <view class="flow-bar">
      <view class="flow-pill">
        <text class="flow-step">Step 1</text>
        <text class="flow-desc">Complete assessments first — Skill Map and AI Advisor will personalize based on your profile</text>
      </view>
    </view>

    <view class="status-card card-data">
      <view class="card-header">
        <text class="card-title">Career Aptitude Test</text>
        <text class="card-subtitle">Discover your core strengths through professional assessments</text>
      </view>
      <view class="card-body">
        <view class="progress-info">
          <text class="progress-text">{{ completedCount }} completed</text>
          <text class="progress-label">{{ totalCount }} assessments available</text>
        </view>
        <view class="radar-placeholder">
          <text class="radar-icon">🧭</text>
        </view>
      </view>
    </view>

    <view class="section-title">Featured Assessments</view>

    <!-- Skeleton while scales load -->
    <view class="skeleton-list" v-if="loading">
      <view class="skel-card" v-for="i in 2" :key="i">
        <view class="skel-square"></view>
        <view class="skel-lines">
          <view class="skel-line skel-w70"></view>
          <view class="skel-line skel-w40"></view>
        </view>
      </view>
    </view>

    <view class="assessment-list" v-else-if="scales.length > 0">
      <view
        class="assessment-card"
        v-for="(s, idx) in scales"
        :key="s.scaleId"
        @click="startQuiz(s)"
      >
        <view class="card-left">
          <view class="icon-box" :class="idx === 0 ? 'mbti-icon' : 'holland-icon'">{{ iconFor(s.title) }}</view>
          <view class="card-info">
            <text class="a-title">{{ s.title }}</text>
            <text class="a-desc">{{ s.description }}</text>
            <view class="tags">
              <text class="tag">⏱ ~{{ estimateMinutes(s.questionCount) }} min</text>
              <text class="tag tag-blue">{{ s.questionCount }} Qs</text>
              <text class="tag tag-done" v-if="completedScales.has(s.scaleId)">✓ Done</text>
            </view>
          </view>
        </view>
        <view class="card-right">
          <view class="btn-start">{{ completedScales.has(s.scaleId) ? 'Retake' : 'Start' }}</view>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else>
      <text class="empty-icon">📝</text>
      <text class="empty-text">No assessments available yet</text>
      <text class="empty-desc">The administrator hasn't published any quiz banks. Check back later.</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getTopSafeHeight } from '@/utils/safeArea';
import {
  getAssessmentScalesApi,
  getMyAssessmentRecordsApi,
  type AssessmentScale,
} from '@/api/assessment';

const darkPref = ref(false);
const topSafeHeight = ref(52);
const loading = ref(true);
const scales = ref<AssessmentScale[]>([]);
const completedScales = ref<Set<number>>(new Set());

const totalCount = computed(() => scales.value.length);
const completedCount = computed(() => completedScales.value.size);

const iconFor = (title: string) => {
  const t = title.toLowerCase();
  if (t.includes('mbti')) return '🧠';
  if (t.includes('holland')) return '🎯';
  return '📋';
};

const estimateMinutes = (questionCount?: number) => {
  // ~12 sec / question, rounded to nearest minute, floor at 1.
  const n = questionCount ?? 0;
  return Math.max(1, Math.round((n * 12) / 60));
};

const startQuiz = (s: AssessmentScale) => {
  uni.navigateTo({
    url: `/pages/assessment/quiz?scaleId=${s.scaleId}&title=${encodeURIComponent(s.title)}`,
  });
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const loadAll = async () => {
  loading.value = true;
  try {
    const [scaleList, records] = await Promise.all([
      getAssessmentScalesApi(),
      // The records call is best-effort -- a guest who somehow lands here
      // shouldn't see a hard error, just an empty completed-set.
      getMyAssessmentRecordsApi().catch(() => []),
    ]);
    scales.value = scaleList || [];
    completedScales.value = new Set((records || []).map((r) => r.scaleId));
  } catch (e: any) {
    uni.showToast({ title: e?.message || 'Failed to load', icon: 'none' });
    scales.value = [];
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  topSafeHeight.value = getTopSafeHeight();
});

// Re-fetch on focus so a freshly completed quiz shows its "Done" pill.
onShow(() => {
  loadAll();
});
</script>

<style scoped>
.status-bar-spacer {
  width: 100%;
  flex-shrink: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
  padding: 0 2px;
}

.page-summary {
  margin-bottom: 16px;
}

.summary-title {
  display: block;
  font-size: 28px;
  line-height: 1.12;
  font-weight: 800;
  color: #0f172a;
}

.summary-text {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.5;
  color: #475569;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: #2563eb;
  width: var(--nav-back-width);
}

.back-icon {
  font-size: 22px;
  font-weight: 300;
  line-height: 1;
}

.back-text {
  font-size: 16px;
  font-weight: 500;
}

.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.3px;
  flex: 1;
  text-align: center;
}

.header-action { width: 64px; }

.flow-bar {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.flow-pill {
  flex: 1;
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 16px;
  padding: 12px 14px;
  box-shadow: var(--shadow-xs);
}

.flow-step {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 0.06em;
  margin-bottom: 4px;
}

.flow-desc { font-size: 13px; color: #475569; line-height: 1.45; }

.assessment-container {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 24px 20px;
  padding-bottom: 60px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.status-card {
  border-radius: 24px;
  padding: 28px 24px;
  color: white;
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
}

.card-data {
  background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%);
  box-shadow: 0 16px 32px -8px rgba(37, 99, 235, 0.35);
}

.card-header { margin-bottom: 24px; }

.card-title {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.5px;
  display: block;
  margin-bottom: 6px;
}

.card-subtitle { font-size: 14px; font-weight: 400; opacity: 0.85; }

.card-body { display: flex; justify-content: space-between; align-items: center; }

.progress-info { display: flex; flex-direction: column; }

.progress-text { font-size: 20px; font-weight: 600; letter-spacing: -0.5px; margin-bottom: 4px; }

.progress-label { font-size: 13px; opacity: 0.8; }

.radar-placeholder {
  width: 64px;
  height: 64px;
  border-radius: 32px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.radar-icon { font-size: 32px; }

.section-title {
  font-size: 20px;
  font-weight: 700;
  color: #000000;
  letter-spacing: -0.5px;
  margin-top: 12px;
  margin-bottom: 16px;
  padding-left: 4px;
}

.assessment-list { display: flex; flex-direction: column; gap: 16px; }

.assessment-card {
  background-color: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease;
}

.assessment-card:active { transform: scale(0.98); }

.card-left { display: flex; align-items: center; flex: 1; }

.icon-box {
  width: 48px;
  height: 48px;
  border-radius: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  margin-right: 16px;
  flex-shrink: 0;
}

.mbti-icon { background-color: #f3e8ff; }
.holland-icon { background-color: #e0e7ff; }

.card-info { display: flex; flex-direction: column; }

.a-title {
  font-size: 16px;
  font-weight: 600;
  color: #1c1c1e;
  margin-bottom: 6px;
  letter-spacing: -0.3px;
}

.a-desc { font-size: 13px; color: #8e8e93; margin-bottom: 8px; }

.tags { display: flex; gap: 8px; }

.tag {
  font-size: 11px;
  font-weight: 500;
  color: #636366;
  background-color: #f2f2f7;
  padding: 4px 8px;
  border-radius: 8px;
}

.tag-blue { color: #2563eb; background-color: #eff6ff; }

.card-right { margin-left: 12px; }

.btn-start {
  background-color: #eff6ff;
  color: #2563eb;
  font-size: 13px;
  font-weight: 700;
  border-radius: 999px;
  padding: 0 16px;
  height: 32px;
  line-height: 32px;
  border: 1px solid #dbeafe;
  display: flex; align-items: center; justify-content: center;
  letter-spacing: 0.02em;
}

.assessment-card:active .btn-start { background-color: #dbeafe; }

.tag-done { color: #16a34a; background: #dcfce7; }

/* Skeleton placeholders shown during initial load. */
.skeleton-list { display: flex; flex-direction: column; gap: 16px; }
.skel-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 20px;
  padding: 20px;
  display: flex; align-items: center; gap: 16px;
}
.skel-square {
  width: 48px; height: 48px; border-radius: 24px;
  background: linear-gradient(90deg, #eef2f7 0%, #f7fafc 50%, #eef2f7 100%);
  background-size: 200% 100%;
  animation: skel-shimmer 1.4s infinite;
  flex-shrink: 0;
}
.skel-lines { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.skel-line {
  height: 12px; border-radius: 6px;
  background: linear-gradient(90deg, #eef2f7 0%, #f7fafc 50%, #eef2f7 100%);
  background-size: 200% 100%;
  animation: skel-shimmer 1.4s infinite;
}
.skel-w40 { width: 40%; }
.skel-w70 { width: 70%; }
@keyframes skel-shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 20px;
}
.empty-icon { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-text { font-size: 16px; font-weight: 700; color: #475569; display: block; margin-bottom: 8px; }
.empty-desc { font-size: 13px; color: #94a3b8; line-height: 1.5; }

.is-dark { background-color: #0f172a; }

.is-dark .page-title,
.is-dark .summary-title,
.is-dark .section-title,
.is-dark .a-title { color: #f8fafc; }

.is-dark .flow-pill,
.is-dark .assessment-card { background-color: #1e293b; }

.is-dark .a-desc,
.is-dark .summary-text,
.is-dark .flow-desc,
.is-dark .tag { color: #94a3b8; }

/* ================================================================
 *  MP-WEIXIN parity overrides — HARDCODED values, no CSS vars.
 * ================================================================ */
/* #ifdef MP-WEIXIN */

.assessment-page,
.page-wrap {
  background-color: #eaeff5;
}

.status-card {
  overflow: hidden;
  box-shadow: none;
  filter: drop-shadow(0 12px 28px rgba(37,99,235,0.38));
}

.assessment-card {
  overflow: visible;
  border: 1.5px solid #b0bfd0;
  box-shadow: 0 4px 16px rgba(0,0,0,0.22),
              0 2px 6px  rgba(0,0,0,0.12);
}

.radar-placeholder {
  backdrop-filter: none;
  -webkit-backdrop-filter: none;
  background: rgba(255,255,255,0.30);
}

/* #endif */
</style>
