<template>
  <view class="paths-page" :class="{ 'is-dark': darkPref }">
    <view class="header">
      <text class="title">Career Paths</text>
      <text class="subtitle">Choose your career direction</text>
    </view>

    <view class="path-list">
      <view v-for="path in paths" :key="path.pathId" class="path-card" @click="viewPath(path)">
        <view class="path-icon-wrap" :class="'wrap-' + (path.code === 'java-backend' ? '0' : '1')">
          <text class="path-icon">{{ path.code === 'java-backend' ? '☕' : '🎨' }}</text>
        </view>
        <view class="path-info">
          <text class="path-name">{{ path.name }}</text>
          <text class="path-desc">{{ path.description }}</text>
        </view>
        <text class="arrow">›</text>
      </view>
    </view>

    <view class="empty" v-if="paths.length === 0 && !loading">
      <text class="empty-icon">🗺️</text>
      <text class="empty-text">No career paths available</text>
      <button class="btn-primary" @click="initializePaths">Initialize Paths</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { getCareerPathsApi, initializeCareerPathsApi, type CareerPath } from '@/api/career';

const paths = ref<CareerPath[]>([]);
const loading = ref(false);
const darkPref = ref(false);

onMounted(async () => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
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
    uni.showToast({ title: 'Initialization successful', icon: 'success' });
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
.paths-page {
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
  margin-bottom: 6px;
}

.subtitle {
  font-size: var(--font-caption);
  color: var(--text-tertiary);
  display: block;
  line-height: var(--line-height-caption);
}

.path-list { display: flex; flex-direction: column; gap: 12px; }

.path-card {
  background-color: #ffffff;
  border-radius: var(--radius-md);
  padding: 18px;
  display: flex;
  align-items: center;
  box-shadow: var(--shadow-sm);
  transition: transform 0.15s;
}

.path-card:active { transform: scale(0.98); }

.path-icon-wrap {
  width: 52px; height: 52px; border-radius: 16px;
  display: flex; justify-content: center; align-items: center;
  margin-right: 16px; flex-shrink: 0;
}

.wrap-0 { background: linear-gradient(135deg, #fef3c7, #fde68a); }
.wrap-1 { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }

.path-icon { font-size: 28px; }

.path-info { flex: 1; min-width: 0; }

.path-name {
  font-size: 17px; font-weight: 600; color: #1e293b;
  display: block; margin-bottom: 4px;
}

.path-desc {
  font-size: 13px; color: #64748b; line-height: 1.4;
  display: block;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
}

.arrow { font-size: 22px; color: #c7c7cc; margin-left: 8px; }

.empty {
  text-align: center; padding: 80px 20px;
}

.empty-icon { font-size: 56px; display: block; margin-bottom: 16px; }

.empty-text {
  font-size: 16px; color: #94a3b8; font-weight: 500;
  display: block; margin-bottom: 24px;
}

.btn-primary {
  background: var(--primary-color);
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--btn-radius);
  height: var(--btn-height-md);
  line-height: var(--btn-height-md);
  border: none;
  box-shadow: var(--shadow-card);
}

.btn-primary:active { background: var(--primary-hover); }

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .title,
.is-dark .path-name { color: #f8fafc; }

.is-dark .subtitle,
.is-dark .path-desc { color: #94a3b8; }

.is-dark .path-card { background: #1e293b; box-shadow: none; }
</style>
