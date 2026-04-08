<template>
  <view class="assessment-container" :class="{ 'is-dark': darkPref }">
    <view class="status-bar-spacer"></view>

    <view class="page-header">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">返回</text>
      </view>
      <text class="page-title">职业测评</text>
      <view class="header-action"></view>
    </view>

    <view class="flow-bar">
      <view class="flow-pill">
        <text class="flow-step">Step 1</text>
        <text class="flow-desc">先完成职业测评，系统将基于结果为你推荐技能路径和岗位方向</text>
      </view>
    </view>

    <view class="status-card card-data">
      <view class="card-header">
        <text class="card-title">职业认知测评</text>
        <text class="card-subtitle">通过专业量表快速了解你的职业倾向与能力画像</text>
      </view>
      <view class="card-body">
        <view class="progress-info">
          <text class="progress-text">已完成 0 项</text>
          <text class="progress-label">共 2 项专业测评</text>
        </view>
        <view class="radar-placeholder">
          <text class="radar-icon">🧭</text>
        </view>
      </view>
    </view>

    <view class="section-title">精选测评</view>
    <view class="assessment-list">
      <view class="assessment-card" @click="startQuiz('mbti')">
        <view class="card-left">
          <view class="icon-box mbti-icon">🧠</view>
          <view class="card-info">
            <text class="a-title">MBTI 性格测试</text>
            <text class="a-desc">识别你的性格类型和岗位偏好</text>
            <view class="tags">
              <text class="tag">⏱ 5 分钟</text>
              <text class="tag tag-blue">93 题</text>
            </view>
          </view>
        </view>
        <view class="card-right">
          <button class="btn-start">开始</button>
        </view>
      </view>

      <view class="assessment-card" @click="startQuiz('holland')">
        <view class="card-left">
          <view class="icon-box holland-icon">🎯</view>
          <view class="card-info">
            <text class="a-title">霍兰德职业兴趣</text>
            <text class="a-desc">找到更适合你的职业环境与成长路径</text>
            <view class="tags">
              <text class="tag">⏱ 8 分钟</text>
              <text class="tag tag-blue">120 题</text>
            </view>
          </view>
        </view>
        <view class="card-right">
          <button class="btn-start">开始</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const darkPref = ref(false);

const startQuiz = (type: string) => {
  uni.navigateTo({
    url: `/pages/assessment/quiz?type=${type}`,
  });
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
});
</script>

<style scoped>
.status-bar-spacer { height: calc(var(--status-bar-height, 20px) + 8px); width: 100%; flex-shrink: 0; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 0 2px; }
.back-btn { display: inline-flex; align-items: center; gap: 2px; color: #2563eb; width: 64px; }
.back-icon { font-size: 22px; font-weight: 300; line-height: 1; }
.back-text { font-size: 16px; font-weight: 500; }
.page-title { font-size: 18px; font-weight: 700; color: #0f172a; letter-spacing: -0.3px; flex: 1; text-align: center; }
.header-action { width: 64px; }
.flow-bar { display: flex; align-items: flex-start; gap: 12px; margin-bottom: 16px; }
.flow-pill { flex: 1; background: #fff; border-radius: 16px; padding: 12px 14px; box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06); }
.flow-step { display: block; font-size: 11px; font-weight: 700; color: #2563eb; letter-spacing: 0.06em; margin-bottom: 4px; }
.flow-desc { font-size: 13px; color: #475569; line-height: 1.45; }
.assessment-container { min-height: 100vh; background-color: #f5f5f7; padding: 24px 20px; padding-bottom: 60px; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; box-sizing: border-box; }
.status-card { border-radius: 24px; padding: 28px 24px; color: #fff; margin-bottom: 32px; position: relative; overflow: hidden; }
.card-data { background: linear-gradient(135deg, #2563eb 0%, #60a5fa 100%); box-shadow: 0 16px 32px -8px rgba(37, 99, 235, 0.35); }
.card-header { margin-bottom: 24px; }
.card-title { font-size: 24px; font-weight: 700; letter-spacing: -0.5px; display: block; margin-bottom: 6px; }
.card-subtitle { font-size: 14px; font-weight: 400; opacity: 0.85; }
.card-body { display: flex; justify-content: space-between; align-items: center; }
.progress-info { display: flex; flex-direction: column; }
.progress-text { font-size: 20px; font-weight: 600; letter-spacing: -0.5px; margin-bottom: 4px; }
.progress-label { font-size: 13px; opacity: 0.8; }
.radar-placeholder { width: 64px; height: 64px; border-radius: 32px; background: rgba(255, 255, 255, 0.15); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); display: flex; justify-content: center; align-items: center; border: 1px solid rgba(255, 255, 255, 0.3); }
.radar-icon { font-size: 32px; }
.section-title { font-size: 20px; font-weight: 700; color: #000; letter-spacing: -0.5px; margin-top: 12px; margin-bottom: 16px; padding-left: 4px; }
.assessment-list { display: flex; flex-direction: column; gap: 16px; }
.assessment-card { background-color: #fff; border-radius: 20px; padding: 20px; display: flex; justify-content: space-between; align-items: center; transition: transform 0.2s ease; }
.assessment-card:active { transform: scale(0.98); }
.card-left { display: flex; align-items: center; flex: 1; }
.icon-box { width: 48px; height: 48px; border-radius: 24px; display: flex; justify-content: center; align-items: center; font-size: 24px; margin-right: 16px; flex-shrink: 0; }
.mbti-icon { background-color: #f3e8ff; }
.holland-icon { background-color: #e0e7ff; }
.card-info { display: flex; flex-direction: column; }
.a-title { font-size: 16px; font-weight: 600; color: #1c1c1e; margin-bottom: 6px; letter-spacing: -0.3px; }
.a-desc { font-size: 13px; color: #8e8e93; margin-bottom: 8px; }
.tags { display: flex; gap: 8px; }
.tag { font-size: 11px; font-weight: 500; color: #636366; background-color: #f2f2f7; padding: 4px 8px; border-radius: 8px; }
.tag-blue { color: #2563eb; background-color: #eff6ff; }
.card-right { margin-left: 12px; }
.btn-start { background-color: #f2f2f7; color: #2563eb; font-size: 14px; font-weight: 600; border-radius: 16px; padding: 0 16px; height: 32px; line-height: 32px; border: none; margin: 0; }
.is-dark { background-color: #0f172a; }
.is-dark .page-title,
.is-dark .section-title,
.is-dark .a-title { color: #f8fafc; }
.is-dark .flow-pill,
.is-dark .assessment-card { background-color: #1e293b; }
.is-dark .a-desc,
.is-dark .flow-desc,
.is-dark .tag { color: #94a3b8; }
</style>
