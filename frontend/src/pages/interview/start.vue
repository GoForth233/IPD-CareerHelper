<template>
  <view class="start-page" :class="{ 'is-dark': darkPref }">
    <view class="header">
      <text class="title">Start Mock Interview</text>
      <text class="subtitle">Select position and difficulty to begin your practice session</text>
    </view>

    <view class="form-card">
      <view class="form-item">
        <text class="label">Target Position</text>
        <picker :range="positions" @change="onPositionChange">
          <view class="picker-box">
            <text class="picker-val" :class="{ 'has-val': selectedPosition }">{{ selectedPosition || 'Select Position' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">Difficulty</text>
        <view class="difficulty-options">
          <view 
            v-for="item in difficulties" 
            :key="item.value"
            :class="['diff-btn', selectedDifficulty === item.value ? 'active' : '']"
            @click="selectedDifficulty = item.value"
          >
            <text>{{ item.label }}</text>
          </view>
        </view>
      </view>

      <button class="btn-primary" @click="startInterview" :loading="loading">
        Start Interview
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { startInterviewApi } from '@/api/interview';
import { LOGIN_PAGE } from '@/utils/auth';

const positions = ['Java Backend Engineer', 'Frontend Engineer', 'Full Stack Engineer', 'Product Manager', 'Data Analyst'];
const selectedPosition = ref('');
const selectedPositionIndex = ref(0);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');

const difficulties = [
  { label: 'Easy', value: 'Easy' },
  { label: 'Medium', value: 'Normal' },
  { label: 'Hard', value: 'Hard' }
];
const selectedDifficulty = ref('Normal');
const loading = ref(false);

const onPositionChange = (e: any) => {
  selectedPositionIndex.value = e.detail.value;
  selectedPosition.value = positions[e.detail.value];
};

const startInterview = async () => {
  if (!selectedPosition.value) {
    uni.showToast({ title: 'Please select a position', icon: 'none' });
    return;
  }

  const userId = uni.getStorageSync('userId');
  const numericId = Number(userId);
  if (!userId || isNaN(numericId) || numericId <= 0) {
    uni.showToast({ title: 'Please sign in first', icon: 'none' });
    setTimeout(() => {
      uni.reLaunch({ url: LOGIN_PAGE });
    }, 600);
    return;
  }

  loading.value = true;
  try {
    const interview = await startInterviewApi({
      userId: numericId,
      positionName: selectedPosition.value,
      difficulty: selectedDifficulty.value
    });

    uni.showToast({ title: 'Interview started', icon: 'success' });
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/interview/chat?interviewId=${interview.interviewId}`
      });
    }, 1000);
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.start-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  padding: 24px 20px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.header { margin-bottom: 28px; padding: 0 4px; }

.title {
  font-size: var(--font-hero);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
  display: block;
  margin-bottom: 8px;
}

.subtitle {
  font-size: var(--font-caption);
  color: var(--text-tertiary);
  display: block;
  line-height: var(--line-height-caption);
}

.form-card {
  background: #ffffff;
  border-radius: var(--radius-lg);
  padding: 24px 20px;
  box-shadow: var(--shadow-card);
}

.form-item { margin-bottom: 24px; }

.label {
  font-size: 16px; font-weight: 600; color: #1e293b;
  margin-bottom: 12px; display: block;
}

.picker-box {
  display: flex; align-items: center; justify-content: space-between;
  border: 1.5px solid #e2e8f0; border-radius: 14px;
  padding: 14px 16px; background: #f8fafc;
}

.picker-val { font-size: 15px; color: #94a3b8; }

.has-val { color: #1e293b; }

.picker-arrow { font-size: 18px; color: #c7c7cc; }

.difficulty-options { display: flex; gap: 10px; }

.diff-btn {
  flex: 1; text-align: center; padding: 14px;
  border: 2px solid #e2e8f0; border-radius: 14px;
  font-size: 14px; font-weight: 500; color: #64748b; background: #ffffff;
  transition: all 0.2s;
}

.diff-btn.active {
  border-color: #2563eb; background: #2563eb;
  color: #fff; font-weight: 600;
}

.diff-btn:active { transform: scale(0.96); }

.btn-primary {
  width: 100%;
  background: var(--primary-color);
  color: #fff;
  border-radius: var(--btn-radius);
  margin-top: 8px;
  font-size: 16px;
  font-weight: 700;
  height: var(--btn-height-lg);
  line-height: var(--btn-height-lg);
  border: none;
  box-shadow: var(--shadow-card);
}

.btn-primary:active { background: var(--primary-hover); }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .title,
.is-dark .label,
.is-dark .has-val { color: #f8fafc; }

.is-dark .form-card { background: #1e293b; box-shadow: none; }

.is-dark .picker-box,
.is-dark .diff-btn { border-color: #334155; background: #0f172a; color: #94a3b8; }

.is-dark .diff-btn.active { background: #2563eb; color: #fff; border-color: #2563eb; }
</style>
