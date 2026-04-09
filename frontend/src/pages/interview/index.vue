<template>
  <view class="interview-container" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">Back</text>
      </view>
      <text class="nav-title">Mock Interview</text>
      <view class="nav-right"></view>
    </view>

    <text class="page-title">Interview Setup</text>

    <!-- AI interviewer card -->
    <view class="ai-card">
      <view class="ai-header">
        <view class="ai-avatar">🤖</view>
        <view class="ai-meta">
          <text class="ai-name">AI Interviewer</text>
          <text class="ai-desc">Simulating senior HR behavioral interview</text>
        </view>
      </view>
      <view class="ai-stats">
        <view class="stat-item">
          <text class="stat-val">82</text>
          <text class="stat-label">Best Score</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-val">3</text>
          <text class="stat-label">Sessions</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <text class="stat-val last-role">Frontend</text>
          <text class="stat-label">Last Role</text>
        </view>
      </view>
    </view>

    <!-- Demo notice -->
    <view class="demo-notice">
      <text class="demo-text">📌 Demo mode: Interview simulation with mock data</text>
    </view>

    <!-- Settings form -->
    <text class="form-title">Interview Settings</text>
    <view class="form-card">
      <view class="form-item">
        <text class="form-label">Target Role</text>
        <picker :range="positions" @change="onPositionChange">
          <view class="form-picker">
            <text class="picker-val" :class="{ 'has-val': selectedPosition }">{{ selectedPosition || 'Select role' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">Difficulty</text>
        <picker :range="difficulties" @change="onDifficultyChange">
          <view class="form-picker">
            <text class="picker-val has-val">{{ selectedDifficulty }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>
      <view class="form-item">
        <text class="form-label">Resume</text>
        <view class="form-picker">
          <text class="picker-val has-val">Frontend_Developer_2026.pdf</text>
        </view>
      </view>
    </view>

    <!-- Tips -->
    <view class="tips-box">
      <text class="tips-icon">💡</text>
      <text class="tips-text">This session requires microphone and camera access for a realistic interview simulation. Please grant permissions when prompted.</text>
    </view>

    <!-- Bottom action bar -->
    <view class="bottom-bar">
      <view class="start-btn" @click="handleStart">
        <view class="pulse-glow"></view>
        <text class="start-text">Start Mock Interview</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const darkPref = ref(false);
const topSafeHeight = ref(44);
const selectedPosition = ref('');
const selectedDifficulty = ref('Medium');

const positions = ['Frontend Developer', 'Backend Developer', 'Full Stack Developer', 'Product Manager'];
const difficulties = ['Easy', 'Medium', 'Hard'];

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const onPositionChange = (e: any) => {
  selectedPosition.value = positions[e.detail.value];
};

const onDifficultyChange = (e: any) => {
  selectedDifficulty.value = difficulties[e.detail.value];
};

const handleStart = () => {
  if (!selectedPosition.value) {
    uni.showToast({ title: 'Please select a target role first', icon: 'none' });
    return;
  }

  uni.showModal({
    title: 'Permission Required',
    content: 'This simulation needs access to your microphone and camera for a realistic interview experience. Grant access?',
    confirmText: 'Allow',
    cancelText: 'Cancel',
    success: (res) => {
      if (res.confirm) {
        uni.authorize({
          scope: 'scope.record',
          success: () => {
            uni.authorize({
              scope: 'scope.camera',
              success: () => {
                uni.navigateTo({ url: '/pages/interview/room' });
              },
              fail: () => {
                uni.showToast({ title: 'Camera access denied', icon: 'none' });
              }
            });
          },
          fail: () => {
            uni.showToast({ title: 'Microphone access denied', icon: 'none' });
          }
        });
      }
    }
  });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const sysInfo = uni.getSystemInfoSync();
  topSafeHeight.value = (sysInfo.statusBarHeight || 20) + 8;
});
</script>

<style scoped>
.interview-container {
  min-height: 100vh;
  background: #f5f5f7;
  padding: 0 20px;
  padding-bottom: 140px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.status-spacer { width: 100%; }

.nav-row {
  padding: 6px 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-btn { display: inline-flex; align-items: center; gap: 2px; color: #2563eb; width: var(--nav-back-width); }

.back-icon { font-size: 22px; font-weight: 300; line-height: 1; }

.back-text { font-size: 16px; font-weight: 500; }

.nav-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.nav-right { width: var(--nav-back-width); }

.page-title {
  display: block;
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
  margin-bottom: 18px;
}

/* AI card */
.ai-card {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  border-radius: 20px; padding: 20px; margin-bottom: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.15);
}

.ai-header { display: flex; align-items: center; margin-bottom: 16px; }

.ai-avatar {
  width: 44px; height: 44px; border-radius: 22px;
  background: rgba(96, 165, 250, 0.2); display: flex;
  align-items: center; justify-content: center;
  font-size: 24px; margin-right: 14px;
}

.ai-meta { flex: 1; }

.ai-name { font-size: 17px; font-weight: 700; color: #ffffff; display: block; }

.ai-desc { font-size: 12px; color: rgba(255, 255, 255, 0.6); margin-top: 2px; }

.ai-stats { display: flex; align-items: center; }

.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; }

.stat-val { font-size: 22px; font-weight: 700; color: #ffffff; }

.last-role { font-size: 14px; }

.stat-label { font-size: 11px; color: rgba(255, 255, 255, 0.5); margin-top: 4px; }

.stat-divider { width: 1px; height: 24px; background: rgba(255, 255, 255, 0.1); }

.demo-notice { background: #eff6ff; border-radius: 12px; padding: 10px 14px; margin-bottom: 20px; }

.demo-text { font-size: 12px; color: #2563eb; }

.form-title {
  font-size: 17px; font-weight: 700; color: #0f172a;
  margin-bottom: 12px; display: block;
}

.form-card {
  background: #ffffff; border-radius: 16px; overflow: hidden;
  margin-bottom: 16px;
}

.form-item {
  display: flex; align-items: center; padding: 16px;
  position: relative;
}

.form-item:not(:last-child)::after {
  content: ''; position: absolute; bottom: 0;
  left: 16px; right: 0; height: 0.5px; background: #e5e5ea;
}

.form-label { font-size: 15px; color: #1e293b; width: 90px; flex-shrink: 0; }

.form-picker { flex: 1; display: flex; align-items: center; justify-content: flex-end; }

.picker-val { font-size: 15px; color: #c7c7cc; }

.has-val { color: #1e293b; }

.picker-arrow { font-size: 18px; color: #c7c7cc; margin-left: 6px; }

.tips-box {
  display: flex; gap: 10px; align-items: flex-start;
  background: #eff6ff; border-radius: 16px; padding: 16px;
  border: 1px solid #bfdbfe;
}

.tips-icon { font-size: 18px; flex-shrink: 0; margin-top: 2px; }

.tips-text { font-size: 13px; color: #1e40af; line-height: 1.5; }

/* Bottom bar */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  padding: 16px 20px calc(20px + env(safe-area-inset-bottom));
  background: rgba(245, 245, 247, 0.88);
  backdrop-filter: blur(24px); -webkit-backdrop-filter: blur(24px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1); z-index: 100;
}

.start-btn {
  position: relative; width: 100%; height: 56px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-radius: 18px; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.35);
}

.start-btn:active { transform: scale(0.98); }

.pulse-glow {
  position: absolute; width: 100%; height: 100%;
  border-radius: 18px;
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  animation: breathe 2.5s ease-in-out infinite;
  z-index: -1;
}

@keyframes breathe {
  0%, 100% { transform: scale(1); opacity: 0.4; }
  50% { transform: scale(1.04); opacity: 0.15; }
}

.start-text { font-size: 17px; font-weight: 700; color: #ffffff; z-index: 1; }

/* Dark mode */
.is-dark { background: #0f172a; }

.is-dark .page-title,
.is-dark .form-title,
.is-dark .form-label,
.is-dark .has-val { color: #f8fafc; }

.is-dark .form-card { background: #1e293b; }

.is-dark .demo-notice,
.is-dark .tips-box { background: #1e293b; border-color: #334155; }

.is-dark .tips-text { color: #93c5fd; }

.is-dark .bottom-bar { background: rgba(15, 23, 42, 0.88); border-color: #334155; }
</style>