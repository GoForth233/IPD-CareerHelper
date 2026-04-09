<template>
  <view class="resume-ai-container" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">Back</text>
      </view>
      <text class="nav-title">AI Resume</text>
      <view class="nav-right"></view>
    </view>

    <view class="header">
      <text class="title">AI Resume Diagnosis</text>
      <text class="subtitle">Select a resume and paste a job description. We will generate targeted optimization suggestions.</text>
    </view>

    <view class="card">
      <view class="section">
        <text class="section-title">1. Select Resume</text>
        <view class="select-box" @click="selectResume">
          <text class="s-icon">📄</text>
          <text class="s-text">{{ selectedResume || 'Choose a resume from your library' }}</text>
          <text class="s-chevron">Select</text>
        </view>
      </view>

      <view class="section">
        <text class="section-title">2. Target Job Description (JD)</text>
        <textarea 
          class="jd-input" 
          v-model="jdText" 
          placeholder="Paste the target job description here. AI will conduct a targeted diagnosis based on this..."
          placeholder-class="ph"
        ></textarea>
      </view>

      <button class="btn-primary" :loading="analyzing" @click="startAnalysis">
        Start AI Diagnosis
      </button>
    </view>

    <!-- Loading overlay -->
    <view class="loading-overlay" v-if="analyzing">
      <view class="spinner"></view>
      <text class="loading-text">{{ loadingMessage }}</text>
      <view class="progress-bar-container">
        <view class="progress-bar-fill" :style="{ width: loadingProgress + '%' }"></view>
      </view>
    </view>

    <view class="result-card" v-if="showResult">
      <view class="r-header">
        <text class="r-title">Diagnosis Result</text>
        <view class="score-ring">
          <text class="score-val">85</text>
          <text class="score-label">Match</text>
        </view>
      </view>
      
      <view class="r-body">
        <text class="point-title">🟢 Strengths</text>
        <text class="point-text">· Proficient in Vue3 and TypeScript, matching JD requirements.</text>
        <text class="point-text">· Demonstrated independent project ownership with strong engineering capability.</text>
        
        <text class="point-title mt">🔴 Areas to Improve</text>
        <text class="point-text">· Missing quantified performance metrics (e.g. first contentful paint time).</text>
        <text class="point-text">· Consider adding CI/CD pipeline experience.</text>
      </view>
      
      <button class="btn-secondary" @click="generateTailored">Generate Tailored Resume</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const selectedResume = ref('');
const jdText = ref('');
const darkPref = ref(false);
const topSafeHeight = ref(44);
const analyzing = ref(false);
const showResult = ref(false);
const loadingMessage = ref('Ready...');
const loadingProgress = ref(0);

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const selectResume = () => {
  uni.showActionSheet({
    itemList: ['Frontend_Developer_2026_Fall.pdf', 'Fullstack_General_v2.pdf'],
    success: (res) => {
      if (res.tapIndex === 0) {
        selectedResume.value = 'Frontend_Developer_2026_Fall.pdf';
      } else {
        selectedResume.value = 'Fullstack_General_v2.pdf';
      }
    }
  });
};

const startAnalysis = () => {
  if (!selectedResume.value) {
    uni.showToast({ title: 'Please select a resume first', icon: 'none' });
    return;
  }
  if (!jdText.value) {
    uni.showToast({ title: 'Please paste the Job Description', icon: 'none' });
    return;
  }

  analyzing.value = true;
  showResult.value = false;
  loadingProgress.value = 0;
  loadingMessage.value = 'Connecting to diagnosis model...';

  setTimeout(() => { loadingMessage.value = 'Extracting experience features...'; loadingProgress.value = 18; }, 350);
  setTimeout(() => { loadingMessage.value = 'Analyzing resume structure & keywords...'; loadingProgress.value = 42; }, 1050);
  setTimeout(() => { loadingMessage.value = 'Comparing against target JD line by line...'; loadingProgress.value = 68; }, 1900);
  setTimeout(() => { loadingMessage.value = 'Generating optimization strategy...'; loadingProgress.value = 88; }, 2800);
  setTimeout(() => {
    loadingMessage.value = 'Almost done, please wait...';
    loadingProgress.value = 100;
  }, 3550);
  setTimeout(() => {
    analyzing.value = false;
    showResult.value = true;
    uni.showToast({ title: 'Diagnosis complete', icon: 'success' });
  }, 4050);
};

const generateTailored = () => {
  uni.showLoading({ title: 'Generating...' });
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: 'Resume tailored!', icon: 'success' });
    setTimeout(() => {
      uni.switchTab({ url: '/pages/resume/index' });
    }, 1500);
  }, 2000);
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';

  const sysInfo = uni.getSystemInfoSync();
  const menuButton = uni.getMenuButtonBoundingClientRect?.();
  if (menuButton && menuButton.top) {
    topSafeHeight.value = menuButton.top;
  } else {
    topSafeHeight.value = (sysInfo.statusBarHeight || 20) + 8;
  }
});
</script>

<style scoped>
.resume-ai-container {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 0 20px 60px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.status-spacer { width: 100%; }

.nav-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0 14px;
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

.nav-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.nav-right { width: 64px; }

.header { margin-bottom: 20px; }

.title {
  font-size: 28px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
  display: block;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  display: block;
}

.card {
  background-color: #ffffff;
  border-radius: 24px;
  padding: 24px 20px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
  margin-bottom: 24px;
}

.section { margin-bottom: 24px; }

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 12px;
  display: block;
}

.select-box {
  display: flex;
  align-items: center;
  background-color: #f8fafc;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.select-box:active { background-color: #f1f5f9; }

.s-icon { font-size: 20px; margin-right: 12px; }

.s-text {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  color: #334155;
  line-height: 1.45;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-right: 10px;
}

.s-chevron {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
  flex-shrink: 0;
}

.jd-input {
  width: 100%;
  height: 160px;
  background-color: #f8fafc;
  border-radius: 16px;
  padding: 16px;
  box-sizing: border-box;
  font-size: 15px;
  color: #334155;
  border: 1px solid #e2e8f0;
  line-height: 1.5;
}

.ph { color: #94a3b8; }

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-hover) 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--btn-radius);
  height: var(--btn-height-lg);
  line-height: var(--btn-height-lg);
  border: none;
  box-shadow: var(--shadow-card);
}

.btn-primary:active { transform: scale(0.98); }

.result-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 24px;
  padding: 24px 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06);
}

.r-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.r-title { font-size: 20px; font-weight: 700; color: #0f172a; }

.score-ring {
  width: 64px;
  height: 64px;
  border-radius: 32px;
  border: 4px solid #10b981;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #ecfdf5;
}

.score-val { font-size: 20px; font-weight: 800; color: #047857; line-height: 1; }

.score-label { font-size: 10px; color: #059669; margin-top: 2px; }

.r-body { margin-bottom: 24px; }

.point-title { font-size: 15px; font-weight: 600; color: #1e293b; margin-bottom: 8px; display: block; }

.mt { margin-top: 16px; }

.point-text { font-size: 14px; color: #475569; line-height: 1.6; margin-bottom: 4px; display: block; }

.btn-secondary {
  background-color: #eff6ff;
  color: #2563eb;
  font-size: 15px;
  font-weight: 600;
  border-radius: 14px;
  height: 48px;
  line-height: 48px;
  border: none;
}

.btn-secondary:active { background-color: #dbeafe; }

.loading-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(4px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #eff6ff;
  border-top: 4px solid #2563eb;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text { font-size: 16px; font-weight: 600; color: #1e293b; }

.progress-bar-container {
  width: 220px;
  height: 6px;
  background-color: #e2e8f0;
  border-radius: 3px;
  margin-top: 16px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background-color: #2563eb;
  border-radius: 3px;
  transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.is-dark { background-color: #0f172a; }

.is-dark .title,
.is-dark .section-title,
.is-dark .r-title,
.is-dark .point-title,
.is-dark .loading-text { color: #f8fafc; }

.is-dark .subtitle,
.is-dark .point-text,
.is-dark .s-text { color: #94a3b8; }

.is-dark .card,
.is-dark .result-card,
.is-dark .select-box,
.is-dark .jd-input { background-color: #1e293b; border-color: #334155; }

.is-dark .loading-overlay { background: rgba(15, 23, 42, 0.88); }

.is-dark .back-text,
.is-dark .back-icon { color: #60a5fa; }
</style>
