<template>
  <view class="home-page" :class="{ 'is-dark': darkPref }">
    <!-- Status bar spacer -->
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <!-- Top bar: search + avatar -->
    <view class="top-bar">
      <view class="search-bar">
        <view class="search-icon-wrap" @click="onSearch">
          <text class="search-icon-svg">🔍</text>
        </view>
        <input
          class="search-input"
          v-model="searchQuery"
          placeholder="Search videos, topics..."
          placeholder-class="search-ph"
          @confirm="onSearch"
        />
        <view class="search-clear" v-if="searchQuery" @click="clearSearch">
          <text class="clear-icon">×</text>
        </view>
      </view>
      <image
        class="user-avatar"
        :src="userInfo.avatarUrl || '/static/default-avatar.png'"
        mode="aspectFill"
        @click="handleAvatarClick"
      />
    </view>

    <!-- Greeting -->
    <view class="greeting-row">
      <text class="greeting-text">Hello, {{ userInfo.nickname || 'Guest' }}</text>
    </view>

    <!-- Core feature grid -->
    <view class="feature-grid">
      <view class="feature-item" @click="navTo('/pages/assessment/index')">
        <view class="feature-icon icon-assess">
          <text class="fi-char">&#x1F9ED;</text>
        </view>
        <text class="feature-label">Assessment</text>
      </view>
      <view class="feature-item" @click="navTo('/pages/map/index')">
        <view class="feature-icon icon-map">
          <text class="fi-char">&#x1F5FA;</text>
        </view>
        <text class="feature-label">Skill Map</text>
      </view>
      <view class="feature-item" @click="navTo('/pages/resume-ai/index')">
        <view class="feature-icon icon-ai">
          <text class="fi-char">&#x2728;</text>
        </view>
        <text class="feature-label">AI Resume</text>
      </view>
      <view class="feature-item" @click="navTo('/pages/interview/index')">
        <view class="feature-icon icon-interview">
          <text class="fi-char">&#x1F3A4;</text>
        </view>
        <text class="feature-label">Mock Interview</text>
      </view>
    </view>

    <!-- Search empty state -->
    <view class="empty-state" v-if="(searchQuery && filteredFeedList.length === 0 && filteredTopicList.length === 0)">
      <text class="empty-icon">🔍</text>
      <text class="empty-text">No results found for "{{ searchQuery }}"</text>
      <button class="btn-clear" @click="clearSearch">Clear Search</button>
    </view>

    <!-- Feed section -->
    <view class="feed-section" v-if="filteredFeedList.length > 0">
      <view class="feed-header">
        <text class="feed-title">Career Insights</text>
        <text class="feed-more" v-if="!searchQuery" @click="showAllFeed = !showAllFeed">
          {{ showAllFeed ? 'Show Less ›' : 'View All ›' }}
        </text>
      </view>

      <view v-if="showAllFeed || searchQuery" class="feed-grid">
        <view class="feed-card grid-card" v-for="(item, idx) in filteredFeedList" :key="idx" @click="openLink(item.url, item.title)">
          <view class="card-cover">
            <image class="card-cover-img" :src="item.thumbnail" mode="aspectFill" />
            <view class="cover-overlay">
              <text class="cover-tag">{{ item.tag }}</text>
            </view>
            <view class="play-icon-wrap">
              <text class="play-icon">▶</text>
            </view>
          </view>
          <view class="card-body">
            <text class="card-title">{{ item.title }}</text>
            <view class="card-meta-row">
              <text class="card-meta">{{ item.views }} views</text>
            </view>
          </view>
        </view>
      </view>

      <scroll-view v-else class="feed-scroll" scroll-x :show-scrollbar="false">
        <view class="feed-track">
          <SlCard class="feed-card" v-for="(item, idx) in filteredFeedList" :key="idx" @click="openLink(item.url, item.title)">
            <view class="card-cover">
              <image class="card-cover-img" :src="item.thumbnail" mode="aspectFill" />
              <view class="cover-overlay">
                <text class="cover-tag">{{ item.tag }}</text>
              </view>
              <view class="play-icon-wrap">
                <text class="play-icon">▶</text>
              </view>
            </view>
            <view class="card-body">
              <text class="card-title">{{ item.title }}</text>
              <view class="card-meta-row">
                <text class="card-meta">{{ item.views }} views</text>
              </view>
            </view>
          </SlCard>
        </view>
      </scroll-view>
    </view>

    <!-- Hot topics -->
    <view class="topics-section" v-if="filteredTopicList.length > 0">
      <view class="feed-header">
        <text class="feed-title">Trending Topics</text>
      </view>
      <view class="topic-list">
        <view class="topic-item" v-for="(t, i) in filteredTopicList" :key="i" @click="openLink(t.url, t.title)">
          <view class="topic-rank" :class="i < 3 ? 'rank-hot' : ''">
            <text>{{ i + 1 }}</text>
          </view>
          <view class="topic-info">
            <text class="topic-text">{{ t.title }}</text>
            <text class="topic-heat">{{ t.heat }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { openLink } from '@/utils/openLink';
import { getTopSafeHeight } from '@/utils/safeArea';
import { getHomeContentApi, type HomeContentItem, type CareerCard } from '@/api/home';
import { clearAuthState, LOGIN_PAGE } from '@/utils/auth';

const userInfo = ref({
  nickname: '',
  avatarUrl: ''
});

const topSafeHeight = ref(88);
const darkPref = ref(false);
const searchQuery = ref('');
const showAllFeed = ref(false);

const defaultFeedList: HomeContentItem[] = [
  { type: 'video', tag: 'Resume', title: 'How to Write a Resume (step-by-step guide)', views: '2M', thumbnail: 'https://i.ytimg.com/vi/Tt08KmFfIYQ/hqdefault.jpg', url: 'https://www.youtube.com/watch?v=Tt08KmFfIYQ' },
  { type: 'video', tag: 'Tips', title: 'Top 5 Tips to Get a Software Engineering Job', views: '800K', thumbnail: 'https://i.ytimg.com/vi/jZIXKlSJcrc/hqdefault.jpg', url: 'https://www.youtube.com/watch?v=jZIXKlSJcrc' },
  { type: 'video', tag: 'Interview', title: 'Google Software Engineer Mock Interview', views: '3.5M', thumbnail: 'https://i.ytimg.com/vi/XKu_SEDAykw/hqdefault.jpg', url: 'https://www.youtube.com/watch?v=XKu_SEDAykw' },
  { type: 'video', tag: 'Frontend', title: 'Frontend Interview Questions 2024', views: '1.2M', thumbnail: 'https://i.ytimg.com/vi/Sm_pnT5Hd2c/hqdefault.jpg', url: 'https://www.youtube.com/watch?v=Sm_pnT5Hd2c' },
];

const defaultTopicList: HomeContentItem[] = [
  { type: 'article', title: 'Is A Computer Science Degree Still Worth It in 2025?', heat: '126K discussions', url: 'https://www.coursera.org/articles/is-computer-science-hard' },
  { type: 'article', title: 'Non-CS background? Here\'s how to become a developer', heat: '83K discussions', url: 'https://roadmap.sh/frontend' },
  { type: 'article', title: '2025 Developer Salary Breakdown by Role', heat: '67K discussions', url: 'https://www.levels.fyi/' },
  { type: 'article', title: 'Grad School vs Industry: Which Path is Right for You?', heat: '42K discussions', url: 'https://www.prospects.ac.uk/postgraduate-study/what-is-a-masters-degree/is-a-masters-worth-it' },
  { type: 'article', title: 'Will AI Replace Programmers? The Real Answer', heat: '38K discussions', url: 'https://www.weforum.org/stories/2024/01/jobs-ai-artificial-intelligence-workplace/' },
];

const feedList = ref<HomeContentItem[]>([...defaultFeedList]);
const topicList = ref<HomeContentItem[]>([...defaultTopicList]);

const filteredFeedList = computed(() => {
  if (!searchQuery.value) return feedList.value;
  const q = searchQuery.value.toLowerCase();
  return feedList.value.filter(item => item.title.toLowerCase().includes(q) || (item.tag || '').toLowerCase().includes(q));
});

const filteredTopicList = computed(() => {
  if (!searchQuery.value) return topicList.value;
  const q = searchQuery.value.toLowerCase();
  return topicList.value.filter(item => item.title.toLowerCase().includes(q));
});

const onSearch = () => {
  // Computed properties automatically update, we just close keyboard here if needed
  uni.hideKeyboard();
};

const clearSearch = () => {
  searchQuery.value = '';
};

const loadHomeContent = async () => {
  try {
    const userId = uni.getStorageSync('userId');
    const numericUserId = Number(userId);
    const data = await getHomeContentApi(userId && !isNaN(numericUserId) && numericUserId > 0 ? numericUserId : undefined);

    // Backend returns careerCards (career paths) and articles separately
    // Map careerCards to feed items as article-type entries
    const remoteCards: HomeContentItem[] = (data?.careerCards || []).map((c: CareerCard) => ({
      type: 'article' as const,
      title: c.name,
      heat: c.description,
      url: `/pages/career/paths?pathId=${c.pathId}`,
    }));

    // Map backend articles to topic list items
    const remoteTopics: HomeContentItem[] = (data?.articles || []).map(a => ({
      type: 'article' as const,
      title: a.title,
      heat: a.summary,
      url: a.imageUrl || '',
    }));

    feedList.value = [...defaultFeedList]; // Keep curated video feed as-is
    topicList.value = remoteTopics.length > 0 ? remoteTopics : [...defaultTopicList];
  } catch (error) {
    feedList.value = [...defaultFeedList];
    topicList.value = [...defaultTopicList];
  }
};

const syncUserFromStorage = () => {
  const info = uni.getStorageSync('userInfo');
  userInfo.value = info
    ? info
    : { nickname: '', avatarUrl: '' };
};

onMounted(() => {
  syncUserFromStorage();

  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';

  topSafeHeight.value = getTopSafeHeight();

  loadHomeContent();
});

onShow(() => {
  syncUserFromStorage();
});

const navTo = (url: string) => {
  uni.navigateTo({ url });
};

const handleAvatarClick = () => {
  uni.showActionSheet({
    itemList: ['View Profile', 'Log Out'],
    success: (res) => {
      if (res.tapIndex === 0) {
        uni.switchTab({ url: '/pages/user/index' });
      } else if (res.tapIndex === 1) {
        uni.showModal({
          title: 'Log Out',
          content: 'Are you sure you want to log out?',
          success: (r) => {
            if (r.confirm) {
              clearAuthState();
              userInfo.value = { nickname: '', avatarUrl: '' };
              uni.reLaunch({ url: LOGIN_PAGE });
            }
          },
        });
      }
    },
  });
};
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  padding-bottom: env(safe-area-inset-bottom);
}

.status-spacer {
  width: 100%;
}

/* ---- Top bar ---- */
.top-bar {
  display: flex;
  align-items: center;
  padding: 8px 20px 0;
  gap: 12px;
}

.search-bar {
  flex: 1;
  display: flex;
  align-items: center;
  height: 38px;
  background: #ffffff;
  border-radius: 19px;
  padding: 0 16px;
  box-shadow: 0 2px 6px rgba(15, 23, 42, 0.05);
}

.search-icon-wrap {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 6px;
}

.search-icon-svg {
  font-size: 14px;
}

.search-input {
  flex: 1;
  font-size: 14px;
  color: #0f172a;
  height: 38px;
}

.search-ph {
  color: #94a3b8;
  font-size: 14px;
}

.search-clear {
  padding: 4px;
  display: flex; align-items: center; justify-content: center;
}

.clear-icon {
  font-size: 18px; color: #94a3b8; line-height: 1;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 18px;
  background: #e2e8f0;
  flex-shrink: 0;
}

/* ---- Empty search state ---- */
.empty-state { text-align: center; padding: 60px 20px 20px; }

.empty-icon { font-size: 48px; display: block; margin-bottom: 16px; }

.empty-text {
  font-size: 15px; color: #64748b; font-weight: 500;
  display: block; margin-bottom: 24px;
}

.btn-clear {
  background: #2563eb; color: #fff; font-size: 14px; font-weight: 600;
  border-radius: 12px; height: 40px; line-height: 40px; border: none; width: 140px;
}

/* ---- Greeting ---- */
.greeting-row {
  padding: 12px 20px 4px;
}

.greeting-text {
  font-size: var(--font-body);
  font-weight: 500;
  color: var(--text-secondary);
}

/* ---- Feature grid ---- */
.feature-grid {
  display: flex;
  justify-content: space-between;
  padding: 20px 24px 8px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  flex: 1;
  min-width: 0;
}

.feature-icon {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: transform 0.15s ease;
}

.feature-icon:active {
  transform: scale(0.92);
}

.fi-char {
  font-size: 26px;
}

.icon-assess {
  background: linear-gradient(145deg, #dbeafe 0%, #bfdbfe 100%);
  box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12);
}

.icon-map {
  background: linear-gradient(145deg, #e0e7ff 0%, #c7d2fe 100%);
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.12);
}

.icon-ai {
  background: linear-gradient(145deg, #fae8ff 0%, #f0abfc 30%, #e9d5ff 100%);
  box-shadow: 0 4px 12px rgba(168, 85, 247, 0.12);
}

.icon-interview {
  background: linear-gradient(145deg, #ffedd5 0%, #fed7aa 100%);
  box-shadow: 0 4px 12px rgba(249, 115, 22, 0.12);
}

.feature-label {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  letter-spacing: 0.02em;
  text-align: center;
  line-height: 1.25;
  min-height: 32px;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ---- Feed section ---- */
.feed-section {
  padding: 24px 0 0;
}

.feed-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-bottom: 14px;
}

.feed-title {
  font-size: var(--font-title);
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.3px;
}

.feed-more {
  font-size: 13px;
  color: #2563eb; font-weight: 500;
}

.feed-scroll {
  width: 100%;
  white-space: nowrap;
}

.feed-track {
  display: inline-flex;
  padding: 0 20px;
  gap: 16px;
  padding-right: 40px;
}

.feed-grid { display: flex; flex-wrap: wrap; gap: 16px; padding: 0 20px; }

.feed-card {
  display: inline-flex;
  flex-direction: column;
  width: 220px;
  background: #ffffff;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--border-strong);
  box-shadow: 0 5px 16px rgba(15, 23, 42, 0.10);
  flex-shrink: 0;
  transition: transform 0.15s;
}

.feed-card:active { transform: scale(0.98); }

.grid-card { width: calc(50% - 8px); }

.card-cover {
  width: 100%;
  height: 110px;
  position: relative;
  overflow: hidden;
  background: #e2e8f0;
}

.card-cover-img { width: 100%; height: 100%; display: block; }

.cover-overlay {
  position: absolute;
  top: 10px;
  left: 10px;
}

.cover-tag {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #ffffff;
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 8px;
}

.play-icon-wrap {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 36px; height: 36px; border-radius: 18px; background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(4px); display: flex; align-items: center; justify-content: center;
}

.play-icon { font-size: 14px; color: #fff; margin-left: 2px; }

.card-body {
  padding: 12px 14px;
  white-space: normal;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1.4;
  margin-bottom: 8px;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  height: 38px;
}

.card-meta-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.card-meta {
  font-size: 12px;
  color: #94a3b8;
}

/* ---- Hot topics ---- */
.topics-section {
  padding: 28px 0 20px;
}

.topic-list {
  padding: 0 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-item {
  display: flex;
  align-items: center;
  background: #ffffff;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--border-strong);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.08);
  transition: transform 0.15s ease;
}

.topic-item:active {
  transform: scale(0.98);
}

.topic-rank {
  width: 24px;
  height: 24px;
  border-radius: 8px;
  background: #f1f5f9;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 14px;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.rank-hot {
  background: #fee2e2;
  color: #ef4444;
}

.topic-info {
  flex: 1;
  min-width: 0;
}

.topic-text {
  font-size: 15px;
  font-weight: 500;
  color: #1e293b;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 4px;
  overflow: hidden;
  line-height: 1.4;
}

.topic-heat {
  font-size: 12px;
  color: #94a3b8;
}

.bottom-safe {
  height: calc(var(--tab-bar-height, 50px) + 20px);
}

/* ---- Dark Mode ---- */
.is-dark { background-color: #0f172a; }

.is-dark .search-bar { background: #1e293b; box-shadow: none; }
.is-dark .search-input { color: #f8fafc; }
.is-dark .feature-label { color: #e2e8f0; }

.is-dark .feed-title { color: #f8fafc; }
.is-dark .feed-card { background: #1e293b; box-shadow: none; border-color: #334155; }
.is-dark .card-title { color: #f8fafc; }

.is-dark .topic-item { background: #1e293b; box-shadow: none; border-color: #334155; }
.is-dark .topic-text { color: #f8fafc; }
.is-dark .topic-rank { background: #334155; color: #94a3b8; }
.is-dark .rank-hot { background: #7f1d1d; color: #fca5a5; }
</style>
