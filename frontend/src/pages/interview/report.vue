<template>
  <view class="report-container" :class="{ 'is-dark': darkPref }">
    <view class="report-header">
      <text class="r-title">面试复盘报告</text>
      <text class="r-sub">岗位：前端开发工程师 ｜ 耗时：12分45秒</text>
    </view>

    <view class="score-card">
      <view class="score-ring-box">
        <view class="score-ring">
          <text class="score-num">85</text>
        </view>
        <text class="score-label">综合得分</text>
      </view>
      <view class="score-summary">
        <text class="score-eval">表现优秀</text>
        <text class="score-desc">整体回答逻辑清晰，技术基础扎实。建议进一步加强高压场景下的表达节奏和临场应对。</text>
      </view>
    </view>

    <view class="dims-section">
      <text class="section-title">维度拆解</text>
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

    <view class="advice-section">
      <text class="section-title">AI 面试官建议</text>
      <view class="advice-card" v-for="(a, i) in adviceList" :key="i">
        <text class="advice-icon">{{ a.icon }}</text>
        <view class="advice-body">
          <text class="advice-title">{{ a.title }}</text>
          <text class="advice-detail">{{ a.detail }}</text>
        </view>
      </view>
    </view>

    <view class="bottom-bar">
      <button class="btn-back" @click="backToLobby">返回面试大厅</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const darkPref = ref(false);

const dimensions = ref([
  { name: '专业能力', score: 90 },
  { name: '表达能力', score: 82 },
  { name: '逻辑思维', score: 85 },
  { name: '抗压能力', score: 75 },
]);

const adviceList = ref([
  {
    icon: '🎯',
    title: '项目叙述建议结构化',
    detail:
      '建议使用 STAR 结构（背景-任务-行动-结果）描述项目经历，并补充量化结果，例如“首屏时间下降 40%”。',
  },
  {
    icon: '💡',
    title: '加强高压追问应对',
    detail:
      '面对追问时先停顿 1-2 秒组织答案，再分点输出。建议重点训练“缺点题”和“冲突题”的真实表达。',
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
.report-container { min-height: 100vh; background: #f5f5f7; padding: 24px 20px; padding-bottom: calc(100px + env(safe-area-inset-bottom)); font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; box-sizing: border-box; }
.report-header { margin-bottom: 24px; }
.r-title { font-size: 28px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; display: block; margin-bottom: 6px; }
.r-sub { font-size: 13px; color: #94a3b8; }
.score-card { background: #fff; border-radius: 24px; padding: 28px 24px; display: flex; gap: 20px; align-items: center; margin-bottom: 28px; box-shadow: 0 4px 16px rgba(15, 23, 42, 0.05); }
.score-ring-box { display: flex; flex-direction: column; align-items: center; gap: 8px; flex-shrink: 0; }
.score-ring { width: 80px; height: 80px; border-radius: 40px; border: 5px solid transparent; background-image: linear-gradient(#fff, #fff), linear-gradient(135deg, #22c55e, #16a34a); background-origin: border-box; background-clip: padding-box, border-box; display: flex; justify-content: center; align-items: center; }
.score-num { font-size: 28px; font-weight: 800; color: #15803d; }
.score-label { font-size: 12px; color: #94a3b8; font-weight: 500; }
.score-summary { flex: 1; }
.score-eval { font-size: 20px; font-weight: 700; color: #15803d; display: block; margin-bottom: 6px; }
.score-desc { font-size: 13px; color: #64748b; line-height: 1.5; }
.dims-section { margin-bottom: 28px; }
.section-title { font-size: 18px; font-weight: 700; color: #0f172a; margin-bottom: 14px; display: block; letter-spacing: -0.3px; }
.dims-card { background: #fff; border-radius: 20px; padding: 20px; box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04); }
.dim-row { display: flex; align-items: center; gap: 12px; margin-bottom: 18px; }
.dim-row:last-child { margin-bottom: 0; }
.dim-name { width: 90px; font-size: 14px; font-weight: 600; color: #334155; }
.dim-bar-bg { flex: 1; height: 8px; background: #f1f5f9; border-radius: 4px; overflow: hidden; }
.dim-bar-fill { height: 100%; border-radius: 4px; transition: width 0.5s ease; }
.bar-0 { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.bar-1 { background: linear-gradient(90deg, #10b981, #34d399); }
.bar-2 { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }
.bar-3 { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.dim-score { width: 28px; font-size: 14px; font-weight: 700; color: #0f172a; text-align: right; }
.advice-section { margin-bottom: 24px; }
.advice-card { display: flex; gap: 14px; background: #fff; border-radius: 16px; padding: 18px; margin-bottom: 12px; box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04); }
.advice-icon { font-size: 24px; flex-shrink: 0; margin-top: 2px; }
.advice-body { flex: 1; }
.advice-title { font-size: 15px; font-weight: 600; color: #1e293b; display: block; margin-bottom: 6px; }
.advice-detail { font-size: 13px; color: #64748b; line-height: 1.55; }
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; padding: 16px 20px calc(20px + env(safe-area-inset-bottom)); background: rgba(245, 245, 247, 0.88); backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px); border-top: 0.5px solid rgba(60, 60, 67, 0.1); z-index: 100; }
.btn-back { width: 100%; background: #2563eb; color: #fff; font-size: 16px; font-weight: 600; border-radius: 16px; height: 52px; line-height: 52px; border: none; }
.btn-back:active { background: #1d4ed8; }
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
