<template>
  <view class="history-page" :class="{ 'is-dark': darkPref }">
    <view class="header">
      <text class="title">Interview History</text>
      <button class="btn-new" @click="startNew">+ New Session</button>
    </view>

    <view class="list" v-if="interviews.length > 0">
      <view v-for="item in interviews" :key="item.interviewId" class="interview-card" @click="viewDetail(item)">
        <view class="card-top">
          <text class="position">{{ item.positionName }}</text>
          <view :class="['status-pill', (item.status ?? '').toLowerCase()]">
            <text class="pill-text">{{ item.status === 'COMPLETED' ? 'Completed' : 'Ongoing' }}</text>
          </view>
        </view>
        <view class="card-bottom">
          <view class="info-item">
            <text class="info-label">Difficulty</text>
            <text class="info-val">{{ item.difficulty }}</text>
          </view>
          <view class="info-item" v-if="item.finalScore">
            <text class="info-label">Score</text>
            <text class="info-val score-val">{{ item.finalScore }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">Date</text>
            <text class="info-val">{{ formatDate(item.startedAt) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">💼</text>
      <text class="empty-text">No interview history yet</text>
      <button class="btn-primary" @click="startNew">Start your first interview</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getUserInterviewsApi, type Interview } from '@/api/interview';

const interviews = ref<Interview[]>([]);
const darkPref = ref(false);

onMounted(async () => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const userId = uni.getStorageSync('userId');
  const numericId = Number(userId);
  if (userId && !isNaN(numericId) && numericId > 0) {
    try {
      interviews.value = await getUserInterviewsApi(numericId);
    } catch (error) {
      console.error('Failed to load interviews:', error);
    }
  }
});

const startNew = () => {
  uni.navigateTo({ url: '/pages/interview/start' });
};

const viewDetail = (item: Interview) => {
  if (item.status === 'ONGOING') {
    uni.navigateTo({ url: `/pages/interview/chat?interviewId=${item.interviewId}` });
  } else {
    uni.showToast({ title: 'Interview ended', icon: 'none' });
  }
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  padding: 24px 20px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.header {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
}

.title {
  font-size: var(--font-hero);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.btn-new {
  background: var(--primary-color);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  border-radius: 12px;
  padding: 0 16px;
  height: 36px;
  line-height: 36px;
  border: none;
}

.btn-new:active { background: var(--primary-hover); }

.list { display: flex; flex-direction: column; gap: 12px; }

.interview-card {
  background: #ffffff;
  border-radius: var(--radius-md);
  padding: 18px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.15s;
}

.interview-card:active { transform: scale(0.98); }

.card-top {
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 14px;
}

.position { font-size: 17px; font-weight: 600; color: #1e293b; }

.status-pill { padding: 4px 12px; border-radius: 10px; }

.pill-text { font-size: 12px; font-weight: 600; }

.completed { background: #dcfce7; }
.completed .pill-text { color: #16a34a; }

.ongoing { background: #fef3c7; }
.ongoing .pill-text { color: #d97706; }

.card-bottom { display: flex; gap: 20px; }

.info-item { display: flex; flex-direction: column; gap: 2px; }

.info-label { font-size: 11px; color: #94a3b8; font-weight: 500; text-transform: uppercase; letter-spacing: 0.3px; }

.info-val { font-size: 14px; color: #334155; font-weight: 500; }

.score-val { color: #2563eb; font-weight: 700; }

.empty { text-align: center; padding: 80px 20px; }

.empty-icon { font-size: 56px; display: block; margin-bottom: 16px; }

.empty-text {
  font-size: 16px; color: #94a3b8; font-weight: 500;
  display: block; margin-bottom: 24px;
}

.btn-primary {
  background: #2563eb; color: #fff; font-size: 16px; font-weight: 600;
  border-radius: 14px; height: 48px; line-height: 48px; border: none;
}

.btn-primary:active { background: #1d4ed8; }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .title,
.is-dark .position { color: #f8fafc; }

.is-dark .interview-card { background: #1e293b; box-shadow: none; }

.is-dark .info-val { color: #e2e8f0; }
</style>
