<template>
  <view class="onboarding-page">
    <swiper
      class="slides"
      :current="current"
      :duration="350"
      :indicator-dots="false"
      @change="onSlideChange"
    >
      <!-- Slide 1: Welcome -->
      <swiper-item class="slide">
        <view class="slide-inner">
          <view class="slide-emoji">🎓</view>
          <text class="slide-title">欢迎来到 CareerLoop</text>
          <text class="slide-desc">
            你的 AI 职业成长伙伴。从简历诊断、面试模拟，到专属职业路径规划——一站式搞定求职。
          </text>
          <view class="feature-list">
            <view class="feature-row">
              <text class="feature-icon">🤖</text>
              <text class="feature-text">AI 智能助手，7×24 小时陪你准备</text>
            </view>
            <view class="feature-row">
              <text class="feature-icon">🎯</text>
              <text class="feature-text">6 套测评 + 专属职业规划</text>
            </view>
            <view class="feature-row">
              <text class="feature-icon">🎤</text>
              <text class="feature-text">模拟面试 + 肢体语言分析</text>
            </view>
          </view>
        </view>
      </swiper-item>

      <!-- Slide 2: AI Personas -->
      <swiper-item class="slide">
        <view class="slide-inner">
          <view class="slide-emoji">✨</view>
          <text class="slide-title">三位 AI 导师，各有所长</text>
          <text class="slide-desc">根据你的需求随时切换，找到最适合你的陪伴方式。</text>
          <view class="persona-list">
            <view class="persona-card">
              <text class="persona-emoji">😊</text>
              <view class="persona-info">
                <text class="persona-name">小职</text>
                <text class="persona-role">温和导师 · 鼓励为主</text>
              </view>
            </view>
            <view class="persona-card">
              <text class="persona-emoji">🔥</text>
              <view class="persona-info">
                <text class="persona-name">小严</text>
                <text class="persona-role">严格学长 · 深度追问</text>
              </view>
            </view>
            <view class="persona-card">
              <text class="persona-emoji">🎙️</text>
              <view class="persona-info">
                <text class="persona-name">小面</text>
                <text class="persona-role">面试官 · 真实模拟</text>
              </view>
            </view>
          </view>
        </view>
      </swiper-item>

      <!-- Slide 3: Get Started -->
      <swiper-item class="slide">
        <view class="slide-inner">
          <view class="slide-emoji">🚀</view>
          <text class="slide-title">开始你的职业旅程</text>
          <text class="slide-desc">
            建议先完成一项测评，AI 会根据你的结果为你定制专属职业路径。整个流程只需 5 分钟！
          </text>
          <view class="tip-card">
            <text class="tip-title">💡 快速上手建议</text>
            <text class="tip-item">① 完成「性格倾向测评」了解自己</text>
            <text class="tip-item">② 上传简历，获取 AI 诊断报告</text>
            <text class="tip-item">③ 与小职聊聊你的职业目标</text>
          </view>
        </view>
      </swiper-item>
    </swiper>

    <!-- Dot indicators -->
    <view class="dots">
      <view
        v-for="(_, i) in 3"
        :key="i"
        class="dot"
        :class="{ 'dot-active': current === i }"
      />
    </view>

    <!-- Bottom actions -->
    <view class="bottom-bar">
      <view v-if="current < 2" class="btn-row">
        <text class="btn-skip" @click="finish">跳过</text>
        <view class="btn-next" @click="next">
          <text class="btn-next-text">下一步</text>
          <text class="btn-next-arrow">→</text>
        </view>
      </view>
      <view v-else class="btn-row btn-row-single">
        <view class="btn-start" @click="finish">
          <text class="btn-start-text">开始探索 →</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const ONBOARDING_KEY = 'onboarding_v1_seen';

const current = ref(0);

const onSlideChange = (e: any) => {
  current.value = e.detail.current;
};

const next = () => {
  if (current.value < 2) current.value++;
};

const finish = () => {
  uni.setStorageSync(ONBOARDING_KEY, '1');
  if (uni.getStorageSync('userId')) {
    uni.reLaunch({ url: '/pages/home/index' });
  } else {
    uni.reLaunch({ url: '/pages/login/index' });
  }
};
</script>

<style scoped>
.onboarding-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #eff6ff 0%, #f8fafc 60%, #f0fdf4 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: env(safe-area-inset-bottom);
}

.slides {
  width: 100%;
  height: 72vh;
}

.slide { width: 100%; height: 100%; }

.slide-inner {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 32px 0;
  box-sizing: border-box;
}

.slide-emoji {
  font-size: 72px;
  line-height: 1;
  margin-bottom: 24px;
}

.slide-title {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  text-align: center;
  margin-bottom: 16px;
}

.slide-desc {
  font-size: 15px;
  line-height: 1.65;
  color: #475569;
  text-align: center;
  margin-bottom: 28px;
}

/* Slide 1: feature list */
.feature-list { width: 100%; display: flex; flex-direction: column; gap: 12px; }
.feature-row {
  display: flex; align-items: center; gap: 12px;
  background: #ffffff;
  border: 1px solid #dbeafe;
  border-radius: 14px;
  padding: 14px 16px;
}
.feature-icon { font-size: 22px; }
.feature-text { font-size: 14px; color: #1e293b; font-weight: 500; flex: 1; }

/* Slide 2: personas */
.persona-list { width: 100%; display: flex; flex-direction: column; gap: 12px; }
.persona-card {
  display: flex; align-items: center; gap: 14px;
  background: #ffffff;
  border: 1px solid #dbeafe;
  border-radius: 16px;
  padding: 16px;
}
.persona-emoji { font-size: 32px; }
.persona-info { display: flex; flex-direction: column; gap: 3px; }
.persona-name { font-size: 16px; font-weight: 700; color: #0f172a; }
.persona-role { font-size: 13px; color: #64748b; }

/* Slide 3: tip card */
.tip-card {
  width: 100%;
  background: #ffffff;
  border: 1px solid #bbf7d0;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.tip-title { font-size: 15px; font-weight: 700; color: #065f46; display: block; margin-bottom: 14px; }
.tip-item { font-size: 14px; color: #374151; line-height: 1.6; display: block; margin-bottom: 6px; }

/* Dots */
.dots {
  display: flex; gap: 8px; margin: 16px 0;
}
.dot {
  width: 8px; height: 8px; border-radius: 4px;
  background: #cbd5e1;
  transition: all 0.3s;
}
.dot-active {
  width: 24px;
  background: #2563eb;
}

/* Bottom bar */
.bottom-bar {
  width: 100%;
  padding: 0 24px 32px;
  box-sizing: border-box;
}
.btn-row {
  display: flex; align-items: center; justify-content: space-between;
}
.btn-row-single { justify-content: center; }

.btn-skip {
  font-size: 15px; color: #94a3b8; font-weight: 500;
  padding: 12px 8px;
}

.btn-next {
  display: flex; align-items: center; gap: 6px;
  background: #2563eb; border-radius: 16px;
  padding: 14px 28px;
  min-height: 50px;
}
.btn-next-text { font-size: 16px; font-weight: 700; color: #ffffff; }
.btn-next-arrow { font-size: 18px; color: #ffffff; }

.btn-start {
  width: 100%;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  border-radius: 16px;
  padding: 16px 0;
  text-align: center;
  min-height: 52px;
  display: flex; align-items: center; justify-content: center;
}
.btn-start-text { font-size: 17px; font-weight: 700; color: #ffffff; }
</style>
