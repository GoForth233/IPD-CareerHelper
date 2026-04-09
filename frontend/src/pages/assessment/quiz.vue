<template>
  <view class="quiz-container" :class="{ 'is-dark': darkPref }">
    <view class="header-bar">
      <view class="header-top">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
          <text class="back-text">Back</text>
        </view>
      </view>
      <view class="progress-wrapper">
        <view class="progress-track">
          <view class="progress-fill" :style="{ width: progressPercentage + '%' }"></view>
        </view>
        <text class="progress-text">{{ currentIndex + 1 }} / {{ questions.length }}</text>
      </view>
    </view>

    <view class="question-card">
      <text class="q-type">Question {{ currentIndex + 1 }}</text>
      <text class="q-title">{{ currentQuestion.title }}</text>
    </view>

    <view class="options-list">
      <view 
        class="option-item" 
        :class="{ 'option-selected': currentAnswer === index }"
        v-for="(option, index) in currentQuestion.options" 
        :key="index"
        @click="selectOption(index)"
      >
        <view class="option-label">{{ String.fromCharCode(65 + index) }}</view>
        <text class="option-text">{{ option }}</text>
      </view>
    </view>

    <view class="bottom-action">
      <button 
        class="btn-prev" 
        :class="{ 'btn-disabled': currentIndex === 0 }"
        @click="handlePrev"
      >Previous</button>
      <button 
        class="btn-next" 
        @click="handleNext"
      >{{ isLastQuestion ? 'View Results' : 'Next' }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';

const questions = [
  {
    title: 'At social gatherings, you usually:',
    options: [
      'Actively meet new people and enjoy the energy of the crowd',
      'Have deep conversations with a few close friends; large groups drain you'
    ]
  },
  {
    title: 'When handling daily tasks, you tend to:',
    options: [
      'Follow established procedures and plans step by step',
      'Stay flexible and ready to adapt to new changes and inspiration'
    ]
  },
  {
    title: 'When facing a complex problem, you first:',
    options: [
      'Focus on details and concrete facts, drawing from past experience',
      'Think about underlying patterns and potential long-term implications'
    ]
  }
];

const currentIndex = ref(0);
const darkPref = ref(false);
const answers = ref<Record<number, number>>({});

const currentQuestion = computed(() => questions[currentIndex.value]);
const currentAnswer = computed(() => answers.value[currentIndex.value]);
const isLastQuestion = computed(() => currentIndex.value === questions.length - 1);
const progressPercentage = computed(() => ((currentIndex.value + 1) / questions.length) * 100);

const selectOption = (index: number) => {
  answers.value[currentIndex.value] = index;
  if (!isLastQuestion.value) {
    setTimeout(() => {
      currentIndex.value++;
    }, 300);
  }
};

const handlePrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

const handleNext = () => {
  if (currentAnswer.value === undefined) {
    uni.showToast({ title: 'Please select an option', icon: 'none' });
    return;
  }

  if (isLastQuestion.value) {
    uni.showLoading({ title: 'AI analyzing...' });
    setTimeout(() => {
      uni.hideLoading();
      uni.redirectTo({
        url: '/pages/assessment/result'
      });
    }, 1500);
  } else {
    currentIndex.value++;
  }
};

const goBack = () => {
  if (currentIndex.value > 0) {
    uni.showModal({
      title: 'Confirm Exit',
      content: 'Your progress will be saved for this session. Continue?',
      success: (res) => {
        if (res.confirm) uni.navigateBack({ delta: 1 });
      }
    });
    return;
  }
  uni.navigateBack({ delta: 1 });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
});
</script>

<style scoped>
.quiz-container {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 122px 20px 120px 20px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.header-bar {
  position: fixed;
  top: 0; left: 0; right: 0;
  background: rgba(245, 245, 247, 0.9);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  z-index: 100;
  padding: calc(var(--status-bar-height, 20px) + 6px) 20px 14px 20px;
  border-bottom: 0.5px solid rgba(60, 60, 67, 0.1);
}

.header-top { display: flex; align-items: center; margin-bottom: 10px; }

.back-btn { display: inline-flex; align-items: center; gap: 2px; color: #2563eb; }

.back-icon { font-size: 20px; font-weight: 300; line-height: 1; }

.back-text { font-size: 16px; font-weight: 500; }

.progress-wrapper { width: 100%; display: flex; align-items: center; gap: 12px; }

.progress-track { flex: 1; height: 6px; background-color: #e5e5ea; border-radius: 3px; overflow: hidden; }

.progress-fill { height: 100%; background-color: #007aff; border-radius: 3px; transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); }

.progress-text { font-size: 13px; font-weight: 500; color: #8e8e93; width: 40px; text-align: right; }

.question-card { margin-bottom: 40px; padding: 0 8px; }

.q-type { font-size: 14px; font-weight: 600; color: #007aff; margin-bottom: 12px; display: block; letter-spacing: 1px; }

.q-title { font-size: 26px; font-weight: 700; color: #000000; line-height: 1.4; letter-spacing: -0.5px; }

.options-list { display: flex; flex-direction: column; gap: 16px; }

.option-item {
  background-color: #ffffff;
  border-radius: 20px;
  padding: 20px;
  display: flex;
  align-items: flex-start;
  border: 1.5px solid transparent;
  transition: all 0.2s ease;
}

.option-item:active { transform: scale(0.98); }

.option-selected { background-color: #eff6ff; border-color: #3b82f6; }

.option-label {
  width: 28px; height: 28px; border-radius: 14px;
  background-color: #f2f2f7; color: #636366;
  font-size: 14px; font-weight: 600;
  display: flex; justify-content: center; align-items: center;
  margin-right: 16px; flex-shrink: 0;
}

.option-selected .option-label { background-color: #3b82f6; color: #ffffff; }

.option-text { font-size: 17px; color: #1c1c1e; line-height: 1.5; flex: 1; }

.option-selected .option-text { color: #1e3a8a; font-weight: 500; }

.bottom-action {
  position: fixed; bottom: 0; left: 0; right: 0;
  padding: 16px 20px 32px 20px;
  background: rgba(245, 245, 247, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  z-index: 100;
  display: flex; gap: 16px;
}

.btn-prev {
  flex: 1; background-color: #ffffff; color: #007aff;
  font-size: 17px; font-weight: 600; border-radius: 16px;
  height: 52px; line-height: 52px; border: none;
}

.btn-disabled { color: #c7c7cc; background-color: #f2f2f7; }

.btn-next {
  flex: 2; background-color: #007aff; color: #ffffff;
  font-size: 17px; font-weight: 600; border-radius: 16px;
  height: 52px; line-height: 52px; border: none;
}

.btn-next:active { background-color: #0062cc; }

.is-dark { background-color: #0f172a; }

.is-dark .header-bar,
.is-dark .option-item,
.is-dark .bottom-action { background: #1e293b; border-color: #334155; }

.is-dark .q-title,
.is-dark .option-text,
.is-dark .progress-text,
.is-dark .back-text { color: #f8fafc; }

.is-dark .option-label,
.is-dark .progress-track { background-color: #334155; color: #94a3b8; }
</style>