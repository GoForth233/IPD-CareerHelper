<template>
  <view class="home-page">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="top-bar">
      <view class="search-bar">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="searchQuery"
          placeholder="搜索岗位、公司、关键词"
          placeholder-class="search-ph"
          @confirm="onSearch"
        />
        <text v-if="searchQuery" class="clear-icon" @click="clearSearch">×</text>
      </view>
      <view class="avatar" @click="handleAvatarClick">
        <text class="avatar-text">{{ displayInitial }}</text>
      </view>
    </view>

    <view class="greeting-row">
      <text class="greeting">Hello，{{ userInfo.nickname || '体验用户' }}</text>
    </view>

    <view class="feature-grid">
      <view class="feature-item" @click="navTo('/pages/assessment/index')">
        <view class="feature-icon assess">🧭</view>
        <text class="feature-label">职业测评</text>
      </view>
      <view class="feature-item" @click="navTo('/pages/map/index')">
        <view class="feature-icon map">🗺️</view>
        <text class="feature-label">技能地图</text>
      </view>
      <view class="feature-item" @click="navTo('/pages/resume-ai/index')">
        <view class="feature-icon ai">✨</view>
        <text class="feature-label">AI简历诊断</text>
      </view>
      <view class="feature-item" @click="navTo('/pages/interview/index')">
        <view class="feature-icon interview">🎤</view>
        <text class="feature-label">模拟面试</text>
      </view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">职场加油站</text>
      </view>
      <scroll-view class="card-scroll" scroll-x :show-scrollbar="false">
        <view class="card-track">
          <view class="info-card" v-for="(item, idx) in filteredFeed" :key="idx" @click="openLink(item.url)">
            <view class="info-cover" :style="{ background: item.cover }"></view>
            <view class="info-body">
              <text class="info-tag">{{ item.tag }}</text>
              <text class="info-title">{{ item.title }}</text>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <view class="section">
      <view class="section-header">
        <text class="section-title">热门话题</text>
      </view>
      <view class="topic-list">
        <view class="topic-item" v-for="(item, idx) in filteredTopics" :key="idx" @click="openLink(item.url)">
          <view class="topic-rank" :class="{ hot: idx < 3 }">{{ idx + 1 }}</view>
          <view class="topic-content">
            <text class="topic-title">{{ item.title }}</text>
            <text class="topic-heat">{{ item.heat }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { clearAuthState, requireAuth } from '@/utils/auth';

type FeedItem = {
  tag: string;
  title: string;
  cover: string;
  url: string;
};

type TopicItem = {
  title: string;
  heat: string;
  url: string;
};

const topSafeHeight = ref(44);
const searchQuery = ref('');

const userInfo = ref({
  nickname: '',
});

const feedList = ref<FeedItem[]>([
  {
    tag: '面试',
    title: '校招高频前端面试题梳理（含答题模板）',
    cover: 'linear-gradient(135deg, #93c5fd, #3b82f6)',
    url: 'https://www.nowcoder.com/',
  },
  {
    tag: '简历',
    title: '一页纸简历如何写出项目亮点',
    cover: 'linear-gradient(135deg, #c4b5fd, #8b5cf6)',
    url: 'https://www.zhipin.com/',
  },
  {
    tag: '成长',
    title: '30天建立前端知识体系的学习路线图',
    cover: 'linear-gradient(135deg, #f9a8d4, #ec4899)',
    url: 'https://roadmap.sh/frontend',
  },
]);

const topicList = ref<TopicItem[]>([
  { title: '2026届校招提前批如何准备？', heat: '126K 讨论', url: 'https://www.nowcoder.com/' },
  { title: 'AI时代前端工程师核心竞争力是什么', heat: '87K 讨论', url: 'https://roadmap.sh/' },
  { title: '实习转正答辩要注意哪些细节', heat: '74K 讨论', url: 'https://www.zhihu.com/' },
  { title: '应届生薪资谈判常见误区', heat: '42K 讨论', url: 'https://www.zhipin.com/' },
]);

const displayInitial = computed(() => {
  const name = userInfo.value.nickname || '体验用户';
  return name.slice(0, 1);
});

const filteredFeed = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return feedList.value;
  return feedList.value.filter((item) => item.title.toLowerCase().includes(query) || item.tag.toLowerCase().includes(query));
});

const filteredTopics = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();
  if (!query) return topicList.value;
  return topicList.value.filter((item) => item.title.toLowerCase().includes(query));
});

const onSearch = () => {
  uni.hideKeyboard();
};

const clearSearch = () => {
  searchQuery.value = '';
};

const navTo = (url: string) => {
  uni.navigateTo({ url });
};

const openLink = (url: string) => {
  // #ifdef H5
  window.open(url, '_blank');
  // #endif

  // #ifndef H5
  uni.setClipboardData({
    data: url,
    success: () => uni.showToast({ title: '链接已复制', icon: 'none' }),
  });
  // #endif
};

const handleAvatarClick = () => {
  uni.showActionSheet({
    itemList: ['进入个人中心', '退出登录'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.switchTab({ url: '/pages/user/index' });
      } else if (res.tapIndex === 1) {
        clearAuthState();
        uni.reLaunch({ url: '/pages/login/login' });
      }
    },
  });
};

onMounted(() => {
  if (!requireAuth('reLaunch')) return;

  const info = uni.getStorageSync('userInfo');
  if (info) userInfo.value = info;

  const systemInfo = uni.getSystemInfoSync();
  const statusBar = systemInfo.statusBarHeight || 20;
  topSafeHeight.value = statusBar + 8;
});
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: #f8fafc;
  padding-bottom: 30px;
}

.status-spacer {
  width: 100%;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 20px 0;
}

.search-bar {
  flex: 1;
  height: 38px;
  border-radius: 19px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 14px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
}

.search-icon {
  font-size: 14px;
  margin-right: 6px;
}

.search-input {
  flex: 1;
  height: 38px;
  font-size: 14px;
  color: #0f172a;
}

.search-ph {
  color: #94a3b8;
}

.clear-icon {
  font-size: 20px;
  color: #94a3b8;
  line-height: 1;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: linear-gradient(135deg, #60a5fa, #2563eb);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-text {
  color: #fff;
  font-size: 14px;
  font-weight: 700;
}

.greeting-row {
  padding: 14px 20px 4px;
}

.greeting {
  font-size: 16px;
  color: #334155;
  font-weight: 600;
}

.feature-grid {
  display: flex;
  justify-content: space-between;
  padding: 18px 20px 0;
  gap: 8px;
}

.feature-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
}

.assess { background: #dbeafe; }
.map { background: #e0e7ff; }
.ai { background: #f5d0fe; }
.interview { background: #ffedd5; }

.feature-label {
  font-size: 12px;
  color: #1e293b;
  font-weight: 600;
  text-align: center;
}

.section {
  margin-top: 24px;
}

.section-header {
  padding: 0 20px;
  margin-bottom: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.card-scroll {
  width: 100%;
  white-space: nowrap;
}

.card-track {
  display: inline-flex;
  gap: 14px;
  padding: 0 20px;
}

.info-card {
  width: 240px;
  border-radius: 16px;
  background: #fff;
  overflow: hidden;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
  border: 1px solid #e2e8f0;
}

.info-cover {
  height: 110px;
}

.info-body {
  padding: 12px;
}

.info-tag {
  font-size: 11px;
  color: #2563eb;
  font-weight: 700;
}

.info-title {
  display: block;
  margin-top: 6px;
  font-size: 14px;
  color: #1e293b;
  line-height: 1.4;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0 20px;
}

.topic-item {
  background: #fff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
  border-radius: 14px;
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.topic-rank {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: #e2e8f0;
  color: #334155;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}

.topic-rank.hot {
  background: #dbeafe;
  color: #2563eb;
}

.topic-content {
  flex: 1;
  min-width: 0;
}

.topic-title {
  display: block;
  font-size: 14px;
  color: #0f172a;
  line-height: 1.4;
}

.topic-heat {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}
</style>
