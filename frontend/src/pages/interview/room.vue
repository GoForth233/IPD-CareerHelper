<template>
  <view class="room-container" :class="{ 'is-dark': darkPref }">
    <view class="video-bg"></view>

    <!-- AI main screen -->
    <view class="main-video">
      <view class="ai-avatar-wrapper">
        <view class="ai-avatar">🤖</view>
        <view class="wave-box" v-if="isAiSpeaking">
          <view class="bar"></view>
          <view class="bar"></view>
          <view class="bar"></view>
          <view class="bar"></view>
        </view>
      </view>
      <text class="ai-name">HR Expert — Sarah</text>
    </view>

    <!-- User picture-in-picture -->
    <view class="user-video">
      <text class="user-placeholder">{{ isCameraOn ? '📹' : '🚫' }}</text>
      <text class="user-state">{{ isCameraOn ? 'Camera on' : 'Camera off' }}</text>
    </view>

    <!-- Top status bar -->
    <view class="top-bar">
      <text class="time-text">{{ formatTime }}</text>
      <view class="status-badge">REC</view>
      <view class="device-state">
        <text class="state-pill" :class="isMicOn ? 'state-on' : 'state-off'">Mic {{ isMicOn ? 'On' : 'Off' }}</text>
        <text class="state-pill" :class="isCameraOn ? 'state-on' : 'state-off'">Cam {{ isCameraOn ? 'On' : 'Off' }}</text>
      </view>
    </view>

    <!-- Subtitle area -->
    <view class="subtitle-area">
      <text class="subtitle-text">{{ currentSubtitle }}</text>
    </view>

    <!-- Bottom controls -->
    <view class="control-bar">
      <view class="control-btn btn-mic" @click="toggleMic">
        <text class="c-icon">{{ isMicOn ? '🎙️' : '🔇' }}</text>
      </view>
      <view class="control-btn btn-end" @click="endInterview">
        <text class="c-icon">📞</text>
      </view>
      <view class="control-btn btn-camera" @click="toggleCamera">
        <text class="c-icon">{{ isCameraOn ? '📹' : '🚫' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';

const isMicOn = ref(true);
const isCameraOn = ref(true);
const isAiSpeaking = ref(true);
const darkPref = ref(false);
const currentSubtitle = ref('Hello, I\'m Sarah, your HR interviewer today. We\'ll begin the frontend developer interview shortly. Please start with a one-minute self-introduction.');

const timeSeconds = ref(0);
const formatTime = ref('00:00');
let timer: any = null;

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  timer = setInterval(() => {
    timeSeconds.value++;
    const m = Math.floor(timeSeconds.value / 60).toString().padStart(2, '0');
    const s = (timeSeconds.value % 60).toString().padStart(2, '0');
    formatTime.value = `${m}:${s}`;
  }, 1000);

  setTimeout(() => {
    isAiSpeaking.value = false;
    currentSubtitle.value = '(Waiting for your response...)';
  }, 6000);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
});

const toggleMic = () => {
  isMicOn.value = !isMicOn.value;
  uni.showToast({ title: isMicOn.value ? 'Microphone enabled' : 'Microphone disabled', icon: 'none' });
  if (!isMicOn.value) {
    currentSubtitle.value = '(Microphone is off — the interviewer cannot hear you)';
  }
};

const toggleCamera = () => {
  isCameraOn.value = !isCameraOn.value;
  uni.showToast({ title: isCameraOn.value ? 'Camera enabled' : 'Camera disabled', icon: 'none' });
};

const endInterview = () => {
  uni.showModal({
    title: 'End Interview',
    content: 'Are you sure you want to end this mock interview and generate the review report?',
    confirmColor: '#ff3b30',
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: 'Generating report...' });
        setTimeout(() => {
          uni.hideLoading();
          uni.redirectTo({ url: '/pages/interview/report' });
        }, 2000);
      }
    }
  });
};
</script>

<style scoped>
.room-container {
  height: 100vh;
  background-color: #1c1c1e;
  position: relative;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.video-bg {
  position: absolute; top: 0; left: 0; right: 0; bottom: 0;
  background: radial-gradient(circle at center, #2c2c2e 0%, #000000 100%);
  z-index: 1;
}

.main-video {
  position: absolute; top: 30%; left: 50%; transform: translate(-50%, -50%);
  z-index: 2; display: flex; flex-direction: column; align-items: center;
}

.ai-avatar-wrapper {
  width: 120px; height: 120px; border-radius: 60px;
  background: rgba(255, 255, 255, 0.1);
  display: flex; justify-content: center; align-items: center;
  margin-bottom: 16px; position: relative;
}

.ai-avatar { font-size: 60px; }

.wave-box {
  position: absolute; bottom: -10px;
  display: flex; gap: 4px; align-items: flex-end; height: 20px;
}

.bar {
  width: 4px; background-color: #0a84ff; border-radius: 2px;
  animation: sound-wave 0.8s infinite ease-in-out alternate;
}

.bar:nth-child(1) { animation-delay: 0.1s; height: 10px; }
.bar:nth-child(2) { animation-delay: 0.3s; height: 20px; }
.bar:nth-child(3) { animation-delay: 0.2s; height: 15px; }
.bar:nth-child(4) { animation-delay: 0.4s; height: 8px; }

@keyframes sound-wave {
  0% { height: 4px; }
  100% { height: 20px; }
}

.ai-name { color: #ffffff; font-size: 16px; font-weight: 500; }

.user-video {
  position: absolute; top: calc(var(--status-bar-height, 47px) + 60px); right: 20px;
  width: 100px; height: 140px; background-color: #3a3a3c;
  border-radius: 12px; z-index: 5;
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex; justify-content: center; align-items: center;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.5);
}

.user-placeholder { font-size: 32px; opacity: 0.75; }

.user-state {
  position: absolute; bottom: 8px; left: 8px; right: 8px;
  text-align: center; font-size: 10px; color: rgba(255, 255, 255, 0.8);
}

.device-state { display: flex; gap: 6px; }

.state-pill { font-size: 10px; padding: 2px 6px; border-radius: 8px; }

.state-on { background: rgba(52, 199, 89, 0.25); color: #9ff0bf; }
.state-off { background: rgba(255, 59, 48, 0.25); color: #ffb3ad; }

.top-bar {
  position: absolute; top: calc(var(--status-bar-height, 20px) + 10px); left: 20px;
  z-index: 5; display: flex; align-items: center; gap: 10px;
  background: rgba(0, 0, 0, 0.4); padding: 6px 12px; border-radius: 16px;
  backdrop-filter: blur(10px); flex-wrap: wrap; max-width: calc(100% - 140px);
}

.time-text { color: #ffffff; font-size: 14px; font-weight: 500; font-variant-numeric: tabular-nums; }

.status-badge {
  background-color: #ff3b30; color: #ffffff;
  font-size: 10px; font-weight: 700; padding: 2px 6px; border-radius: 4px;
}

.subtitle-area {
  position: absolute; bottom: 160px; left: 20px; right: 20px; z-index: 5;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px);
  padding: 16px; border-radius: 16px;
  border: 0.5px solid rgba(255, 255, 255, 0.1);
}

.subtitle-text { color: #ffffff; font-size: 16px; line-height: 1.5; }

.control-bar {
  position: absolute; bottom: 0; left: 0; right: 0; height: 120px;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0) 100%);
  z-index: 10; display: flex; justify-content: center; align-items: center;
  gap: 32px; padding-bottom: calc(20px + env(safe-area-inset-bottom));
}

.control-btn {
  width: 60px; height: 60px; border-radius: 30px;
  display: flex; justify-content: center; align-items: center;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px);
  transition: transform 0.2s ease;
}

.control-btn:active { transform: scale(0.9); }

.btn-end { background-color: #ff3b30; width: 70px; height: 70px; border-radius: 35px; }

.c-icon { font-size: 24px; }

.btn-end .c-icon { font-size: 28px; }

.is-dark .video-bg {
  background: radial-gradient(circle at center, #1e293b 0%, #020617 100%);
}
</style>