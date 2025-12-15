<template>
  <view class="container">
    <view class="header">
      <text class="title">职业发展路径</text>
      <text class="subtitle">选择适合你的职业方向</text>
    </view>

    <view class="path-list">
      <view v-for="path in paths" :key="path.pathId" class="path-card" @click="viewPath(path)">
        <text class="path-icon">{{ path.code === 'java-backend' ? '☕' : '🎨' }}</text>
        <view class="path-info">
          <text class="path-name">{{ path.name }}</text>
          <text class="path-desc">{{ path.description }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="empty" v-if="paths.length === 0 && !loading">
      <text class="empty-text">暂无职业路径</text>
      <button class="btn-primary" @click="initializePaths">初始化路径</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getCareerPathsApi, initializeCareerPathsApi, type CareerPath } from '@/api/career';

const paths = ref<CareerPath[]>([]);
const loading = ref(false);

onMounted(async () => {
  await loadPaths();
});

const loadPaths = async () => {
  loading.value = true;
  try {
    paths.value = await getCareerPathsApi();
  } catch (error) {
    console.error('Failed to load paths:', error);
  } finally {
    loading.value = false;
  }
};

const initializePaths = async () => {
  loading.value = true;
  try {
    await initializeCareerPathsApi();
    uni.showToast({ title: '初始化成功', icon: 'success' });
    await loadPaths();
  } catch (error) {
    console.error('Failed to initialize paths:', error);
  } finally {
    loading.value = false;
  }
};

const viewPath = (path: CareerPath) => {
  uni.navigateTo({ url: `/pages/career/progress?pathId=${path.pathId}` });
};
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.header {
  margin-bottom: 25px;
  text-align: center;
}

.title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 14px;
  color: #666;
  display: block;
}

.path-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.path-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.path-icon {
  font-size: 40px;
  margin-right: 15px;
}

.path-info {
  flex: 1;
}

.path-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 6px;
}

.path-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  display: block;
}

.arrow {
  font-size: 28px;
  color: #ccc;
}

.empty {
  text-align: center;
  padding: 60px 20px;
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

