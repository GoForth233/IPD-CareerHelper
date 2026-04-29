<template>
  <view class="progress-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>
    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">Back</text>
      </view>
      <text class="nav-title">Progress</text>
      <view style="width:64px;"></view>
    </view>

    <view class="header">
      <text class="title">Learning Progress</text>
      <text class="subtitle">Track which milestones are complete, currently active, or still locked in this path.</text>
    </view>

    <view class="node-list" v-if="nodes.length > 0">
      <view v-for="node in nodes" :key="node.nodeId" class="node-card">
        <view class="node-dot" :class="'dot-' + getNodeStatus(node.nodeId).toLowerCase()">
          <text v-if="getNodeStatus(node.nodeId) === 'COMPLETED'" class="dot-icon">✓</text>
          <text v-else-if="getNodeStatus(node.nodeId) === 'UNLOCKED'" class="dot-icon">○</text>
          <text v-else class="dot-icon">🔒</text>
        </view>
        <view class="node-info">
          <text class="node-name">{{ node.name }}</text>
          <text class="node-level">Level {{ node.level }}</text>
        </view>
        <view class="status-pill" :class="'pill-' + getNodeStatus(node.nodeId).toLowerCase()">
          <text class="pill-text">{{ getStatusText(node.nodeId) }}</text>
        </view>
      </view>
    </view>

    <view class="empty" v-else>
      <text class="empty-icon">📚</text>
      <text class="empty-text">No learning records yet</text>
      <text class="empty-desc">Open the available career paths first, then choose one to start building progress data.</text>
      <button class="btn-primary" @click="goToPaths">View Career Paths</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getTopSafeHeight } from '@/utils/safeArea';
import { getPathNodesApi, getUserProgressApi, type CareerNode, type UserCareerProgress } from '@/api/career';

const nodes = ref<CareerNode[]>([]);
const progress = ref<UserCareerProgress[]>([]);
const pathId = ref<number>(1);
const darkPref = ref(false);
const topSafeHeight = ref(52);

const goBack = () => uni.navigateBack({ delta: 1 });

onMounted(async () => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  topSafeHeight.value = getTopSafeHeight();

  const pages = getCurrentPages();
  const currentPage = pages[pages.length - 1] as any;
  pathId.value = parseInt(currentPage.options?.pathId || '1');

  const userId = uni.getStorageSync('userId');
  if (!userId) {
    uni.showToast({ title: 'Please sign in first', icon: 'none' });
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
    case 'COMPLETED': return 'Completed';
    case 'UNLOCKED': return 'In Progress';
    default: return 'Locked';
  }
};

const goToPaths = () => {
  uni.navigateTo({ url: '/pages/career/paths' });
};
</script>

<style scoped>
.progress-page {
  min-height: 100vh;
  background-color: #f5f5f7;
  padding: 0 20px 24px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.status-spacer { width: 100%; }

.nav-row {
  display: flex; align-items: center;
  height: 44px; padding: 0 2px; margin-bottom: 4px;
}

.back-btn {
  display: inline-flex; align-items: center; gap: 2px;
  color: #2563eb; width: 64px;
}

.back-icon { font-size: 24px; font-weight: 300; line-height: 1; }
.back-text { font-size: 16px; font-weight: 500; }

.nav-title {
  flex: 1; text-align: center;
  font-size: 17px; font-weight: 600;
  color: #0f172a; letter-spacing: -0.3px;
}

.header { margin-bottom: 20px; }

.title {
  font-size: 28px; font-weight: 800; color: #0f172a;
  letter-spacing: -0.5px; display: block;
}

.subtitle {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.5;
  color: #475569;
}

.node-list { display: flex; flex-direction: column; gap: 10px; }

.node-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: 16px; padding: 16px 18px;
  display: flex; align-items: center;
  box-shadow: var(--shadow-sm);
}

.node-dot {
  width: 44px; height: 44px; border-radius: 22px;
  display: flex; align-items: center; justify-content: center;
  margin-right: 14px; flex-shrink: 0;
}

.dot-icon { font-size: 18px; }

.dot-completed { background: #dcfce7; }
.dot-completed .dot-icon { color: #16a34a; font-weight: 700; }

.dot-unlocked { background: #fef3c7; }
.dot-unlocked .dot-icon { color: #d97706; }

.dot-locked { background: #f1f5f9; }
.dot-locked .dot-icon { font-size: 16px; }

.node-info { flex: 1; min-width: 0; }

.node-name {
  font-size: 16px; font-weight: 600; color: #1e293b;
  display: block; margin-bottom: 3px;
}

.node-level { font-size: 13px; color: #94a3b8; display: block; }

.status-pill {
  padding: 5px 12px; border-radius: 10px; flex-shrink: 0; margin-left: 8px;
}

.pill-text { font-size: 12px; font-weight: 600; }

.pill-completed { background: #dcfce7; }
.pill-completed .pill-text { color: #16a34a; }

.pill-unlocked { background: #fef3c7; }
.pill-unlocked .pill-text { color: #d97706; }

.pill-locked { background: #f1f5f9; }
.pill-locked .pill-text { color: #94a3b8; }

.empty { text-align: center; padding: 80px 20px; }

.empty-icon { font-size: 56px; display: block; margin-bottom: 16px; }

.empty-text {
  font-size: 16px; color: #94a3b8; font-weight: 500;
  display: block; margin-bottom: 24px;
}

.empty-desc {
  display: block;
  margin: -14px 0 24px;
  font-size: 13px;
  line-height: 1.45;
  color: var(--text-secondary);
}

.btn-primary {
  background: #2563eb; color: #fff; font-size: 16px; font-weight: 600;
  border-radius: 14px; height: 48px; line-height: 48px; border: none;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.2);
}

.btn-primary:active { background: #1d4ed8; }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .nav-title { color: #f8fafc; }

.is-dark .title,
.is-dark .node-name { color: #f8fafc; }

.is-dark .subtitle,
.is-dark .empty-desc { color: #94a3b8; }

.is-dark .node-card { background: #1e293b; box-shadow: none; }
</style>
