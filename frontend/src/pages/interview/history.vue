<template>
  <view class="container">
    <view class="header">
      <button class="btn-start" @click="startNew">+ 开始新面试</button>
    </view>

    <view class="list" v-if="interviews.length > 0">
      <view v-for="item in interviews" :key="item.interviewId" class="interview-card" @click="viewDetail(item)">
        <view class="card-header">
          <text class="position">{{ item.positionName }}</text>
          <view :class="['status-badge', item.status.toLowerCase()]">
            <text>{{ item.status === 'COMPLETED' ? '已完成' : '进行中' }}</text>
          </view>
        </view>
        <view class="card-body">
          <view class="info-row">
            <text class="label">难度:</text>
            <text class="value">{{ item.difficulty }}</text>
          </view>
          <view class="info-row" v-if="item.finalScore">
            <text class="label">评分:</text>
            <text class="value score">{{ item.finalScore }}</text>
          </view>
          <view class="info-row">
            <text class="label">时间:</text>
            <text class="value">{{ formatDate(item.startedAt) }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">💼</text>
      <text class="empty-text">还没有面试记录</text>
      <button class="btn-primary" @click="startNew">开始第一次面试</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getUserInterviewsApi, type Interview } from '@/api/interview';

const interviews = ref<Interview[]>([]);

onMounted(async () => {
  const userId = uni.getStorageSync('userId');
  if (userId) {
    try {
      interviews.value = await getUserInterviewsApi(userId);
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
    uni.showToast({ title: '面试已结束', icon: 'none' });
  }
};

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 15px;
}

.header {
  margin-bottom: 20px;
}

.btn-start {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.interview-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.position {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-badge.completed {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.ongoing {
  background-color: #fff3cd;
  color: #856404;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.label {
  color: #999;
  margin-right: 8px;
}

.value {
  color: #333;
}

.value.score {
  color: #667eea;
  font-weight: bold;
}

.empty {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  color: #999;
  display: block;
  margin-bottom: 30px;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
}
</style>

