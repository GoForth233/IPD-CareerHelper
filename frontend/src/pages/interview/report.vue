<template>
  <view class="report-container" :class="{ 'is-dark': darkPref }">
    <view class="report-header">
      <text class="r-title">Interview Report</text>
      <text class="r-sub">Role: Frontend Developer | Duration: 12m 45s</text>
    </view>

    <!-- Overall score -->
    <view class="score-card">
      <view class="score-ring-box">
        <view class="score-ring">
          <text class="score-num">85</text>
        </view>
        <text class="score-label">Overall Score</text>
      </view>
      <view class="score-summary">
        <text class="score-eval">Excellent</text>
        <text class="score-desc">Solid technical foundation with clear and logical expression. Keep practicing under-pressure scenarios.</text>
      </view>
    </view>

    <!-- Dimensions breakdown -->
    <view class="dims-section">
      <text class="section-title">Dimension Breakdown</text>
      <view class="dims-card">
        <view class="dim-row" v-for="(d, i) in dimensions" :key="i">
          <text class="dim-name">{{ d.name }}</text>
          <view class="dim-bar-bg">
            <view class="dim-bar-fill" :class="'bar-' + i" :style="{ width: d.score + '%' }"></view>
          </view>
          <text class="dim-score">{{ d.score }}</text>
        </view>
      </view>
    </view>

    <!-- AI advice -->
    <view class="advice-section">
      <text class="section-title">AI Interviewer Feedback</text>
      <view class="advice-card" v-for="(a, i) in adviceList" :key="i">
        <text class="advice-icon">{{ a.icon }}</text>
        <view class="advice-body">
          <text class="advice-title">{{ a.title }}</text>
          <text class="advice-detail">{{ a.detail }}</text>
        </view>
      </view>
    </view>

    <!-- Bottom action -->
    <view class="bottom-bar">
      <button class="btn-back" @click="backToLobby">Back to Interview Lobby</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const darkPref = ref(false);

const dimensions = ref([
  { name: 'Technical', score: 90 },
  { name: 'Communication', score: 82 },
  { name: 'Logic', score: 85 },
  { name: 'Resilience', score: 75 },
]);

const adviceList = ref([
  {
    icon: '🎯',
    title: 'Structure your project showcases',
    detail: 'When presenting project experience, use the STAR method (Situation → Task → Action → Result) to clearly convey your role and impact. Quantify results wherever possible, e.g. "reduced load time by 40%."',
  },
  {
    icon: '💡',
    title: 'Improve stress responses',
    detail: 'Under pressure questions, take a 2-second pause to structure your thoughts before answering. Practice answering "What is your biggest weakness?" with genuine self-awareness instead of a disguised strength.',
  },
]);

const backToLobby = () => {
  uni.navigateBack({ delta: 2 });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
});
</script>

<style scoped>
.report-container {
  min-height: 100vh;
  background: #f5f5f7;
  padding: 24px 20px;
  padding-bottom: calc(100px + env(safe-area-inset-bottom));
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.report-header { margin-bottom: 24px; }

.r-title {
  font-size: 28px; font-weight: 800; color: #0f172a;
  letter-spacing: -0.5px; display: block; margin-bottom: 6px;
}

.r-sub { font-size: 13px; color: #94a3b8; }

/* Score card */
.score-card {
  background: #ffffff; border-radius: 24px; padding: 28px 24px;
  display: flex; gap: 20px; align-items: center; margin-bottom: 28px;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.05);
}

.score-ring-box {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  flex-shrink: 0;
}

.score-ring {
  width: 80px; height: 80px; border-radius: 40px;
  border: 5px solid transparent;
  background-image: linear-gradient(#ffffff, #ffffff), linear-gradient(135deg, #22c55e, #16a34a);
  background-origin: border-box;
  background-clip: padding-box, border-box;
  display: flex; justify-content: center; align-items: center;
}

.score-num { font-size: 28px; font-weight: 800; color: #15803d; }

.score-label { font-size: 12px; color: #94a3b8; font-weight: 500; }

.score-summary { flex: 1; }

.score-eval {
  font-size: 20px; font-weight: 700; color: #15803d;
  display: block; margin-bottom: 6px;
}

.score-desc { font-size: 13px; color: #64748b; line-height: 1.5; }

/* Dimensions */
.dims-section { margin-bottom: 28px; }

.section-title {
  font-size: 18px; font-weight: 700; color: #0f172a;
  margin-bottom: 14px; display: block; letter-spacing: -0.3px;
}

.dims-card {
  background: #ffffff; border-radius: 20px; padding: 20px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
}

.dim-row {
  display: flex; align-items: center; gap: 12px;
  margin-bottom: 18px;
}

.dim-row:last-child { margin-bottom: 0; }

.dim-name { width: 90px; font-size: 14px; font-weight: 600; color: #334155; }

.dim-bar-bg {
  flex: 1; height: 8px; background: #f1f5f9; border-radius: 4px; overflow: hidden;
}

.dim-bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }

.bar-0 { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.bar-1 { background: linear-gradient(90deg, #10b981, #34d399); }
.bar-2 { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }
.bar-3 { background: linear-gradient(90deg, #f59e0b, #fbbf24); }

.dim-score { width: 28px; font-size: 14px; font-weight: 700; color: #0f172a; text-align: right; }

/* Advice */
.advice-section { margin-bottom: 24px; }

.advice-card {
  display: flex; gap: 14px; background: #ffffff;
  border-radius: 16px; padding: 18px; margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}

.advice-icon { font-size: 24px; flex-shrink: 0; margin-top: 2px; }

.advice-body { flex: 1; }

.advice-title {
  font-size: 15px; font-weight: 600; color: #1e293b;
  display: block; margin-bottom: 6px;
}

.advice-detail { font-size: 13px; color: #64748b; line-height: 1.55; }

/* Bottom bar */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  padding: 16px 20px calc(20px + env(safe-area-inset-bottom));
  background: rgba(245, 245, 247, 0.88);
  backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1); z-index: 100;
}

.btn-back {
  width: 100%; background: #2563eb; color: #ffffff;
  font-size: 16px; font-weight: 600; border-radius: 16px;
  height: 52px; line-height: 52px; border: none;
}

.btn-back:active { background: #1d4ed8; }

/* Dark mode */
.is-dark { background: #0f172a; }

.is-dark .r-title,
.is-dark .section-title,
.is-dark .dim-name,
.is-dark .dim-score,
.is-dark .advice-title { color: #f8fafc; }

.is-dark .score-card,
.is-dark .dims-card,
.is-dark .advice-card { background: #1e293b; box-shadow: none; }

.is-dark .score-desc,
.is-dark .advice-detail { color: #94a3b8; }

.is-dark .bottom-bar { background: rgba(15, 23, 42, 0.88); border-color: #334155; }
</style>