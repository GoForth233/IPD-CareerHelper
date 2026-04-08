<template>
  <view class="result-container" :class="{ 'is-dark': darkPref }">
    <view class="result-header">
      <text class="result-subtitle">你的性格类型为</text>
      <text class="result-title">INTJ · 建筑师</text>
      <view class="tags-container">
        <text class="tag">独立思考</text>
        <text class="tag">逻辑分析</text>
        <text class="tag">战略规划</text>
      </view>
    </view>

    <view class="section-title">能力雷达</view>
    <view class="radar-card">
      <view class="skill-bars">
        <view class="skill-row">
          <text class="skill-name">逻辑</text>
          <view class="bar-bg"><view class="bar-fill fill-logic" style="width: 95%;"></view></view>
          <text class="skill-score">95</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">创造力</text>
          <view class="bar-bg"><view class="bar-fill fill-creative" style="width: 88%;"></view></view>
          <text class="skill-score">88</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">执行力</text>
          <view class="bar-bg"><view class="bar-fill fill-exec" style="width: 75%;"></view></view>
          <text class="skill-score">75</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">协作</text>
          <view class="bar-bg"><view class="bar-fill fill-team" style="width: 60%;"></view></view>
          <text class="skill-score">60</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">抗压</text>
          <view class="bar-bg"><view class="bar-fill fill-pressure" style="width: 82%;"></view></view>
          <text class="skill-score">82</text>
        </view>
      </view>
    </view>

    <view class="section-title">AI 深度分析</view>
    <view class="analysis-card">
      <view class="paragraph">
        <text class="p-title">职业优势</text>
        <text class="p-content">你擅长处理复杂系统与抽象概念，能够快速识别核心问题并进行结构化拆解。非常适合系统设计、数据分析和技术研发等方向。</text>
      </view>
      <view class="divider"></view>
      <view class="paragraph">
        <text class="p-title">建议提升</text>
        <text class="p-content">你可能更重视逻辑严谨，有时会忽略团队协作中的情绪与沟通细节。建议在项目中有意识地练习表达与反馈，提升跨团队协同能力。</text>
      </view>
    </view>

    <view class="bottom-action">
      <button class="btn-primary" @click="goMap">查看推荐技能地图</button>
      <button class="btn-secondary" @click="goBack">返回测评页</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const darkPref = ref(false);

const goMap = () => {
  uni.setStorageSync('assessment_recommended_role', '前端开发工程师');
  uni.navigateTo({ url: '/pages/map/index?from=assessment' });
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
});
</script>

<style scoped>
.result-container { min-height: 100vh; background-color: #f5f5f7; padding: 24px 20px; padding-bottom: calc(140px + env(safe-area-inset-bottom)); font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; box-sizing: border-box; }
.result-header { display: flex; flex-direction: column; align-items: center; padding: 40px 20px; background: linear-gradient(135deg, #1e1b4b 0%, #4338ca 100%); border-radius: 24px; color: #fff; margin-bottom: 32px; box-shadow: 0 16px 32px -8px rgba(67, 56, 202, 0.4); }
.result-subtitle { font-size: 14px; font-weight: 500; opacity: 0.8; margin-bottom: 8px; letter-spacing: 1px; }
.result-title { font-size: 32px; font-weight: 800; letter-spacing: -1px; margin-bottom: 20px; }
.tags-container { display: flex; gap: 12px; flex-wrap: wrap; justify-content: center; }
.tag { background: rgba(255, 255, 255, 0.15); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); padding: 6px 14px; border-radius: 16px; font-size: 13px; font-weight: 600; border: 1px solid rgba(255, 255, 255, 0.2); }
.section-title { font-size: 20px; font-weight: 700; color: #000; letter-spacing: -0.5px; margin-top: 12px; margin-bottom: 16px; padding-left: 4px; }
.radar-card { background-color: #fff; border-radius: 24px; padding: 24px; margin-bottom: 32px; }
.skill-bars { display: flex; flex-direction: column; gap: 20px; }
.skill-row { display: flex; align-items: center; gap: 12px; }
.skill-name { width: 75px; font-size: 14px; font-weight: 600; color: #1c1c1e; }
.bar-bg { flex: 1; height: 8px; background-color: #f2f2f7; border-radius: 4px; overflow: hidden; }
.bar-fill { height: 100%; border-radius: 4px; }
.fill-logic { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.fill-creative { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }
.fill-exec { background: linear-gradient(90deg, #10b981, #34d399); }
.fill-team { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.fill-pressure { background: linear-gradient(90deg, #ef4444, #f87171); }
.skill-score { width: 24px; font-size: 14px; font-weight: 700; color: #000; text-align: right; }
.analysis-card { background-color: #fff; border-radius: 24px; padding: 24px; margin-bottom: 24px; }
.paragraph { display: flex; flex-direction: column; gap: 8px; }
.p-title { font-size: 16px; font-weight: 600; color: #1c1c1e; letter-spacing: -0.3px; }
.p-content { font-size: 15px; color: #636366; line-height: 1.6; }
.divider { height: 1px; background-color: #f2f2f7; margin: 20px 0; }
.bottom-action { position: fixed; bottom: 0; left: 0; right: 0; padding: 16px 20px calc(20px + env(safe-area-inset-bottom)) 20px; background: rgba(245, 245, 247, 0.92); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border-top: 0.5px solid rgba(60, 60, 67, 0.1); z-index: 300; display: flex; flex-direction: column; gap: 12px; }
.btn-primary { width: 100%; background-color: #007aff; color: #fff; font-size: 17px; font-weight: 600; border-radius: 16px; height: 52px; line-height: 52px; border: none; }
.btn-secondary { width: 100%; background-color: transparent; color: #007aff; font-size: 16px; font-weight: 600; height: 44px; line-height: 44px; border: none; }
.is-dark { background-color: #0f172a; }
.is-dark .section-title,
.is-dark .p-title,
.is-dark .skill-name,
.is-dark .skill-score { color: #f8fafc; }
.is-dark .radar-card,
.is-dark .analysis-card,
.is-dark .bottom-action { background-color: #1e293b; border-color: #334155; }
.is-dark .p-content,
.is-dark .result-subtitle { color: #94a3b8; }
</style>
