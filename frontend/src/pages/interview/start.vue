<template>
  <view class="container">
    <view class="header">
      <text class="title">开始模拟面试</text>
      <text class="subtitle">选择职位和难度，开始你的面试准备</text>
    </view>

    <view class="card">
      <view class="form-item">
        <text class="label">目标职位</text>
        <picker :range="positions" @change="onPositionChange">
          <view class="picker">
            {{ selectedPosition || '请选择职位' }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">面试难度</text>
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
        开始面试
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { startInterviewApi } from '@/api/interview';

const positions = ['Java后端工程师', '前端工程师', '全栈工程师', '产品经理', '数据分析师'];
const selectedPosition = ref('');
const selectedPositionIndex = ref(0);

const difficulties = [
  { label: '简单', value: 'Easy' },
  { label: '中等', value: 'Normal' },
  { label: '困难', value: 'Hard' }
];
const selectedDifficulty = ref('Normal');
const loading = ref(false);

const onPositionChange = (e: any) => {
  selectedPositionIndex.value = e.detail.value;
  selectedPosition.value = positions[e.detail.value];
};

const startInterview = async () => {
  if (!selectedPosition.value) {
    uni.showToast({ title: '请选择职位', icon: 'none' });
    return;
  }

  const userId = uni.getStorageSync('userId');
  if (!userId) {
    uni.showToast({ title: '请先登录', icon: 'none' });
    setTimeout(() => {
      uni.navigateTo({ url: '/pages/login/login' });
    }, 1000);
    return;
  }

  loading.value = true;
  try {
    const interview = await startInterviewApi({
      userId,
      positionName: selectedPosition.value,
      difficulty: selectedDifficulty.value
    });

    uni.showToast({ title: '面试已开始', icon: 'success' });
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
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.header {
  margin-bottom: 30px;
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 14px;
  color: #666;
  display: block;
}

.card {
  background-color: #fff;
  border-radius: 16px;
  padding: 30px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.form-item {
  margin-bottom: 25px;
}

.label {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: block;
}

.picker {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px;
  font-size: 14px;
  background-color: #f9f9f9;
  color: #333;
}

.difficulty-options {
  display: flex;
  gap: 10px;
}

.diff-btn {
  flex: 1;
  text-align: center;
  padding: 12px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
  background-color: #fff;
}

.diff-btn.active {
  border-color: #667eea;
  background-color: #667eea;
  color: #fff;
  font-weight: bold;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 8px;
  margin-top: 20px;
  font-size: 16px;
  font-weight: bold;
}
</style>

