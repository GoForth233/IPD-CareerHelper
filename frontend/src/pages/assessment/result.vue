<template>
  <view class="result-container" :class="{ 'is-dark': darkPref }">
    <view class="result-header">
      <text class="result-subtitle">Your personality type is</text>
      <text class="result-title">INTJ Architect</text>
      <view class="tags-container">
        <text class="tag">Independent Thinker</text>
        <text class="tag">Logical</text>
        <text class="tag">Strategic Planner</text>
      </view>
    </view>

    <view class="section-title">Ability Radar</view>
    <view class="radar-card">
      <view class="skill-bars">
        <view class="skill-row">
          <text class="skill-name">Logic</text>
          <view class="bar-bg"><view class="bar-fill fill-logic" style="width: 95%;"></view></view>
          <text class="skill-score">95</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">Creativity</text>
          <view class="bar-bg"><view class="bar-fill fill-creative" style="width: 88%;"></view></view>
          <text class="skill-score">88</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">Execution</text>
          <view class="bar-bg"><view class="bar-fill fill-exec" style="width: 75%;"></view></view>
          <text class="skill-score">75</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">Teamwork</text>
          <view class="bar-bg"><view class="bar-fill fill-team" style="width: 60%;"></view></view>
          <text class="skill-score">60</text>
        </view>
        <view class="skill-row">
          <text class="skill-name">Resilience</text>
          <view class="bar-bg"><view class="bar-fill fill-pressure" style="width: 82%;"></view></view>
          <text class="skill-score">82</text>
        </view>
      </view>
    </view>

    <view class="section-title">AI Deep Analysis</view>
    <view class="analysis-card">
      <view class="paragraph">
        <text class="p-title">Career Strengths</text>
        <text class="p-content">As an INTJ personality, you have a natural advantage in handling complex systems and abstract concepts. You excel at identifying hidden connections and building long-term strategic plans. This makes you highly suited for roles in system architecture, data analysis, and cutting-edge technology R&D.</text>
      </view>
      <view class="divider"></view>
      <view class="paragraph">
        <text class="p-title">Growth Areas</text>
        <text class="p-content">You may tend to over-prioritize perfection and logical precision, sometimes overlooking the emotional needs of teammates. In roles requiring extensive interpersonal communication and empathy, you might feel drained. We recommend consciously developing your emotional intelligence and communication warmth in future work settings.</text>
      </view>
    </view>

    <view class="bottom-action">
      <button class="btn-primary" @click="goMap">View Recommended Career Map</button>
      <button class="btn-secondary" @click="goBack">Back to Assessment</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const darkPref = ref(false);

const goMap = () => {
  uni.setStorageSync('assessment_recommended_role', 'Frontend Developer');
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
.result-container {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 24px 20px;
  padding-bottom: calc(140px + env(safe-area-inset-bottom));
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.result-header {
  display: flex; flex-direction: column; align-items: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #1e1b4b 0%, #4338ca 100%);
  border-radius: 24px; color: white; margin-bottom: 32px;
  box-shadow: 0 16px 32px -8px rgba(67, 56, 202, 0.4);
}

.result-subtitle { font-size: 14px; font-weight: 500; opacity: 0.8; margin-bottom: 8px; letter-spacing: 1px; }

.result-title { font-size: 36px; font-weight: 800; letter-spacing: -1px; margin-bottom: 20px; }

.tags-container { display: flex; gap: 12px; }

.tag {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px);
  padding: 6px 14px; border-radius: 16px; font-size: 13px; font-weight: 600;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.section-title {
  font-size: 20px; font-weight: 700; color: #000000;
  letter-spacing: -0.5px; margin-top: 12px; margin-bottom: 16px; padding-left: 4px;
}

.radar-card {
  background-color: #ffffff; border-radius: 24px; padding: 24px; margin-bottom: 32px;
}

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

.skill-score { width: 24px; font-size: 14px; font-weight: 700; color: #000000; text-align: right; }

.analysis-card { background-color: #ffffff; border-radius: 24px; padding: 24px; margin-bottom: 24px; }

.paragraph { display: flex; flex-direction: column; gap: 8px; }

.p-title { font-size: 16px; font-weight: 600; color: #1c1c1e; letter-spacing: -0.3px; }

.p-content { font-size: 15px; color: #636366; line-height: 1.6; }

.divider { height: 1px; background-color: #f2f2f7; margin: 20px 0; }

.bottom-action {
  position: fixed; bottom: 0; left: 0; right: 0;
  padding: 16px 20px calc(20px + env(safe-area-inset-bottom)) 20px;
  background: rgba(245, 245, 247, 0.92);
  backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  z-index: 300; display: flex; flex-direction: column; gap: 12px;
  pointer-events: auto;
}

.btn-primary {
  width: 100%; background-color: #007aff; color: #ffffff;
  font-size: 17px; font-weight: 600; border-radius: 16px;
  height: 52px; line-height: 52px; border: none;
}

.btn-primary:active { background-color: #0062cc; }

.btn-secondary {
  width: 100%; background-color: transparent; color: #007aff;
  font-size: 16px; font-weight: 600; height: 44px; line-height: 44px; border: none;
}

.btn-secondary:active { opacity: 0.7; }

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