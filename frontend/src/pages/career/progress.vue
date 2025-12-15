<template>
  <view class="container">
    <view class="header">
      <text class="title">My Learning Progress</text>
    </view>

    <view class="node-list" v-if="nodes.length > 0">
      <view v-for="node in nodes" :key="node.nodeId" class="node-card">
        <view :class="['node-icon', getNodeStatus(node.nodeId).toLowerCase()]">
          <text v-if="getNodeStatus(node.nodeId) === 'COMPLETED'">✓</text>
          <text v-else-if="getNodeStatus(node.nodeId) === 'UNLOCKED'">○</text>
          <text v-else>🔒</text>
        </view>
        <view class="node-info">
          <text class="node-name">{{ node.name }}</text>
          <text class="node-level">Level {{ node.level }}</text>
        </view>
        <view :class="['status-badge', getNodeStatus(node.nodeId).toLowerCase()]">
          <text>{{ getStatusText(node.nodeId) }}</text>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">📚</text>
      <text class="empty-text">No learning records yet</text>
      <button class="btn-primary" @click="goToPaths">View Career Paths</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getPathNodesApi, getUserProgressApi, type CareerNode, type UserCareerProgress } from '@/api/career';

const nodes = ref<CareerNode[]>([]);
const progress = ref<UserCareerProgress[]>([]);
const pathId = ref<number>(1); // Default to Java path

onMounted(async () => {
  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1] as any;
  pathId.value = parseInt(currentPage.options?.pathId || '1');

  const userId = uni.getStorageSync('userId');
  if (!userId) {
    uni.showToast({ title: 'Please login first', icon: 'none' });
    return;
  }

  try {
    nodes.value = await getPathNodesApi(pathId.value);
    progress.value = await getUserProgressApi(userId);
  } catch (error) {
    console.error('Failed to load progress:', error);
  }
});

const getNodeStatus = (nodeId?: number) => {
  if (!nodeId) return 'LOCKED';
  const userProgress = progress.value.find(p => p.nodeId === nodeId);
  return userProgress?.status || 'LOCKED';
};

const getStatusText = (nodeId?: number) => {
  const status = getNodeStatus(nodeId);
  switch (status) {
    case 'COMPLETED':
      return 'Completed';
    case 'UNLOCKED':
      return 'In Progress';
    default:
      return 'Locked';
  }
};

const goToPaths = () => {
  uni.switchTab({ url: '/pages/career/paths' });
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
  font-size: 22px;
  font-weight: bold;
  color: #333;
  display: block;
}

.node-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.node-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 18px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.node-icon {
  width: 50px;
  height: 50px;
  border-radius: 25px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
}

.node-icon.completed {
  background-color: #d4edda;
  color: #155724;
}

.node-icon.unlocked {
  background-color: #fff3cd;
  color: #856404;
}

.node-icon.locked {
  background-color: #f0f0f0;
  color: #999;
}

.node-info {
  flex: 1;
}

.node-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 4px;
}

.node-level {
  font-size: 13px;
  color: #666;
  display: block;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.completed {
  background-color: #d4edda;
  color: #155724;
}

.status-badge.unlocked {
  background-color: #fff3cd;
  color: #856404;
}

.status-badge.locked {
  background-color: #f0f0f0;
  color: #999;
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

