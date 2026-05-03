<template>
  <view class="home-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="top-bar">
      <view class="search-bar">
        <view class="search-icon-wrap" @click="onSearch">
          <text class="search-icon-svg">🔍</text>
        </view>
        <input
          class="search-input"
          v-model="searchQuery"
          placeholder="Search videos, articles, paths..."
          placeholder-class="search-ph"
          @confirm="onSearch"
        />
        <view class="search-clear" v-if="searchQuery" @click="clearSearch">
          <text class="clear-icon">×</text>
        </view>
      </view>
      <image
        class="user-avatar"
        :src="avatarSrc"
        mode="aspectFill"
        @click="handleAvatarClick"
      />
    </view>

    <view class="greeting-row">
      <text class="greeting-kicker">Career Loop</text>
      <text class="greeting-title">Hello, {{ userInfo.nickname || 'Guest' }}</text>
      <text class="greeting-text">Pull down to refresh — fresh videos and articles every day.</text>
    </view>

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
      <view class="feature-item" @click="navTo('/pages/interview/start')">
        <view class="feature-icon icon-interview">
          <text class="fi-char">&#x1F3A4;</text>
        </view>
        <text class="feature-label">Mock Interview</text>
      </view>
    </view>

    <!-- 7-day check-in chip — short, glanceable, taps through to the calendar -->
    <view v-if="checkin" class="checkin-card" @click="navTo('/pages/checkin/index')">
      <view class="checkin-left">
        <text class="checkin-kicker">Daily check-in</text>
        <text class="checkin-title">{{ checkin.streakDays || 0 }} day streak</text>
        <text class="checkin-sub">{{ checkin.todayCompleted }}/{{ checkin.todayTotal }} done today · {{ checkin.weeklyDays }}/7 this week</text>
      </view>
      <view class="checkin-right">
        <view class="checkin-bar">
          <view class="checkin-bar-fill" :style="{ width: checkinPercent + '%' }"></view>
        </view>
        <text class="checkin-cta">View calendar ›</text>
      </view>
    </view>
    <view v-if="checkin && (!checkin.streakDays || !checkin.todayCompleted)" class="checkin-tip">
      <text class="checkin-tip-text">Pick one task below to keep the streak alive.</text>
    </view>

    <!-- Section 1 — Career Videos (Bilibili) -->
    <view class="section" v-if="filteredVideos.length > 0">
      <view class="section-header">
        <view class="section-titles">
          <text class="section-title">Career Videos</text>
          <text class="section-meta">From Bilibili · refreshed daily</text>
        </view>
      </view>

      <scroll-view class="hscroll" scroll-x :show-scrollbar="false">
        <view class="hscroll-track">
          <view
            class="video-card"
            v-for="(v, idx) in filteredVideos"
            :key="v.id"
            @click="openLink(v.url, v.title)"
          >
            <view class="video-cover" :class="'cover-tone-' + (idx % 4)">
              <image v-if="v.coverUrl" class="video-cover-img" :src="v.coverUrl" mode="aspectFill" />
              <view class="play-icon-wrap">
                <text class="play-icon">▶</text>
              </view>
              <view class="duration-badge" v-if="v.durationSec">
                <text class="duration-text">{{ formatDuration(v.durationSec) }}</text>
              </view>
            </view>
            <view class="video-body">
              <text class="video-title">{{ v.title }}</text>
              <view class="video-meta-row">
                <text class="video-up">{{ v.upName || 'UP' }}</text>
                <text class="video-views" v-if="v.viewCount">· {{ formatViews(v.viewCount) }}</text>
              </view>
            </view>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- Section 2 — Career Insights (articles) -->
    <view class="section" v-if="filteredArticles.length > 0">
      <view class="section-header">
        <view class="section-titles">
          <text class="section-title">Career Insights</text>
          <text class="section-meta">Read, then act on the next action card</text>
        </view>
      </view>

      <view class="article-list">
        <view
          class="article-card"
          v-for="(a, idx) in filteredArticles"
          :key="a.id"
          @click="openArticle(a)"
        >
          <view class="article-cover" :class="'cover-tone-' + (idx % 4)">
            <image v-if="a.imageUrl" class="article-cover-img" :src="a.imageUrl" mode="aspectFill" />
          </view>
          <view class="article-body">
            <text class="article-title">{{ a.title }}</text>
            <text v-if="a.summary" class="article-summary">{{ a.summary }}</text>
            <view class="article-tag" v-if="a.category">
              <text class="article-tag-text">{{ a.category }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- Section 3 — Career Consultations -->
    <view class="section" v-if="filteredConsultations.length > 0">
      <view class="section-header">
        <view class="section-titles">
          <text class="section-title">From the Field</text>
          <text class="section-meta">Short tips from HRs and senior engineers</text>
        </view>
      </view>

      <view class="consult-list">
        <view
          class="consult-card"
          v-for="c in filteredConsultations"
          :key="c.id"
          @click="c.sourceUrl ? openLink(c.sourceUrl, c.title) : undefined"
          :style="c.sourceUrl ? 'cursor:pointer' : ''"
        >
          <view class="consult-head">
            <text class="consult-title">{{ c.title }}</text>
            <text v-if="c.author" class="consult-author">{{ c.author }}</text>
          </view>
          <text v-if="c.body" class="consult-body">{{ c.body }}</text>
          <text v-if="c.sourceUrl" class="consult-link-hint">阅读全文 ›</text>
        </view>
      </view>
    </view>

    <!-- Section 4 — Career path spotlights -->
    <view class="section" v-if="filteredCareerCards.length > 0">
      <view class="section-header">
        <view class="section-titles">
          <text class="section-title">Career Paths</text>
          <text class="section-meta">Explore the full Skill Map</text>
        </view>
        <view class="section-more" @click="navTo('/pages/map/index')">
          <text class="section-more-text">Open Map</text>
          <text class="section-more-arrow">›</text>
        </view>
      </view>

      <view class="path-grid">
        <view
          class="path-card"
          v-for="(p, idx) in filteredCareerCards"
          :key="p.pathId"
          :class="'cover-tone-' + (idx % 4)"
          @click="navTo('/pages/map/index?pathId=' + p.pathId)"
        >
          <text class="path-name">{{ p.name }}</text>
          <text class="path-desc">{{ p.description }}</text>
        </view>
      </view>
    </view>

    <view class="empty-state"
          v-if="searchQuery && filteredVideos.length === 0 && filteredArticles.length === 0 && filteredConsultations.length === 0 && filteredCareerCards.length === 0">
      <text class="empty-icon">🔍</text>
      <text class="empty-text">No results found for "{{ searchQuery }}"</text>
      <button class="btn-clear" @click="clearSearch">Clear Search</button>
    </view>

    <view class="bottom-safe"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { onShow, onPullDownRefresh } from '@dcloudio/uni-app';
import { openLink } from '@/utils/openLink';
import { getTopSafeHeight } from '@/utils/safeArea';
import {
  getHomeContentApi,
  type BiliVideoCard,
  type HomeArticle,
  type HomeConsultation,
  type CareerCard,
} from '@/api/home';
import { getCheckInStatusApi, type CheckInStatus } from '@/api/checkin';
import { clearAuthState, LOGIN_PAGE } from '@/utils/auth';

const userInfo = ref<{
  nickname: string;
  avatarUrl: string;
  avatarViewUrl?: string;
}>({
  nickname: '',
  avatarUrl: '',
  avatarViewUrl: '',
});

const avatarSrc = computed(() => {
  if (userInfo.value.avatarViewUrl) return userInfo.value.avatarViewUrl;
  const raw = userInfo.value.avatarUrl;
  if (raw && /^https?:\/\//i.test(raw)) return raw;
  return '/static/default-avatar.png';
});

const topSafeHeight = ref(88);
const darkPref = ref(false);
const searchQuery = ref('');

const videos = ref<BiliVideoCard[]>([]);
const articles = ref<HomeArticle[]>([]);
const consultations = ref<HomeConsultation[]>([]);
const careerCards = ref<CareerCard[]>([]);
const checkin = ref<CheckInStatus | null>(null);
const checkinPercent = computed(() => {
  if (!checkin.value || !checkin.value.todayTotal) return 0;
  return Math.round((checkin.value.todayCompleted / checkin.value.todayTotal) * 100);
});

// Search filters every section so the home page works as a quick triage tool.
const matches = (haystack: string | undefined | null, q: string) =>
  !!haystack && haystack.toLowerCase().includes(q);

const filteredVideos = computed(() => {
  if (!searchQuery.value) return videos.value;
  const q = searchQuery.value.toLowerCase();
  return videos.value.filter(v => matches(v.title, q) || matches(v.upName, q) || matches(v.keyword, q));
});

const filteredArticles = computed(() => {
  if (!searchQuery.value) return articles.value;
  const q = searchQuery.value.toLowerCase();
  return articles.value.filter(a => matches(a.title, q) || matches(a.summary, q) || matches(a.category, q));
});

const filteredConsultations = computed(() => {
  if (!searchQuery.value) return consultations.value;
  const q = searchQuery.value.toLowerCase();
  return consultations.value.filter(c => matches(c.title, q) || matches(c.body, q) || matches(c.author, q));
});

const filteredCareerCards = computed(() => {
  if (!searchQuery.value) return careerCards.value;
  const q = searchQuery.value.toLowerCase();
  return careerCards.value.filter(p => matches(p.name, q) || matches(p.description, q));
});

const onSearch = () => {
  uni.hideKeyboard();
};

const clearSearch = () => {
  searchQuery.value = '';
};

const formatDuration = (sec: number): string => {
  if (!sec || sec < 0) return '';
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${m}:${String(s).padStart(2, '0')}`;
};

const formatViews = (n: number): string => {
  if (n >= 100_000_000) return (n / 100_000_000).toFixed(1) + '亿';
  if (n >= 10_000) return (n / 10_000).toFixed(1) + '万';
  if (n >= 1000) return (n / 1000).toFixed(1) + 'k';
  return String(n);
};

const openArticle = (a: HomeArticle) => {
  if (!a.url) {
    uni.showToast({ title: 'No link attached', icon: 'none' });
    return;
  }
  // In-app routes go through navigateTo; external https URLs go through the
  // shared openLink util (which handles the WeChat MP web-view fallback).
  if (a.url.startsWith('/pages/')) {
    uni.navigateTo({ url: a.url });
  } else {
    openLink(a.url, a.title);
  }
};

const loadHomeContent = async () => {
  try {
    const userId = uni.getStorageSync('userId');
    const numericUserId = Number(userId);
    const data = await getHomeContentApi(
      userId && !isNaN(numericUserId) && numericUserId > 0 ? numericUserId : undefined
    );

    videos.value = data?.videos || [];
    articles.value = data?.articles || [];
    consultations.value = data?.consultations || [];
    careerCards.value = data?.careerCards || [];
  } catch {
    videos.value = [];
    articles.value = [];
    consultations.value = [];
    careerCards.value = [];
  }
};

// Best-effort: failing to load streak should never break the home feed.
const loadCheckin = async () => {
  const uid = Number(uni.getStorageSync('userId'));
  if (!uid || uid <= 0) {
    checkin.value = null;
    return;
  }
  try {
    checkin.value = await getCheckInStatusApi();
  } catch {
    checkin.value = null;
  }
};

const syncUserFromStorage = () => {
  const info = uni.getStorageSync('userInfo');
  userInfo.value = info
    ? { avatarUrl: '', avatarViewUrl: '', nickname: '', ...info }
    : { nickname: '', avatarUrl: '', avatarViewUrl: '' };
};

onMounted(() => {
  syncUserFromStorage();
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  topSafeHeight.value = getTopSafeHeight();
  loadHomeContent();
  loadCheckin();
});

onShow(() => {
  syncUserFromStorage();
  // Refresh streak on tab return so finishing an interview/assessment
  // immediately bumps the chip without requiring a pull-to-refresh.
  loadCheckin();
});

// Pull-to-refresh: the user wants a fresh batch *now* without waiting for
// the daily cron. We re-fetch from the same endpoint; the backend rotates
// per-user pseudo-randomly using dayOfYear+userId so the next morning the
// batch shifts naturally.
onPullDownRefresh(async () => {
  try {
    await Promise.all([loadHomeContent(), loadCheckin()]);
    uni.showToast({ title: 'Refreshed', icon: 'success' });
  } catch {
    uni.showToast({ title: 'Refresh failed', icon: 'none' });
  } finally {
    uni.stopPullDownRefresh();
  }
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
              userInfo.value = { nickname: '', avatarUrl: '', avatarViewUrl: '' };
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

.status-spacer { width: 100%; }

/* ---- Top bar ---- */
.top-bar { display: flex; align-items: center; padding: 8px 20px 0; gap: 12px; }
.search-bar {
  flex: 1; display: flex; align-items: center;
  height: 42px; background: #ffffff;
  border: 1px solid var(--border-color); border-radius: 14px;
  padding: 0 16px; box-shadow: var(--shadow-xs);
}
.search-icon-wrap { width: 18px; height: 18px; display: flex; align-items: center; justify-content: center; margin-right: 6px; }
.search-icon-svg { font-size: 14px; }
.search-input { flex: 1; font-size: 14px; color: #0f172a; height: 38px; }
.search-ph { color: #94a3b8; font-size: 14px; }
.search-clear { padding: 4px; }
.clear-icon { font-size: 18px; color: #94a3b8; line-height: 1; }
.user-avatar { width: 36px; height: 36px; border-radius: 18px; background: #e2e8f0; flex-shrink: 0; }

/* ---- Greeting ---- */
.greeting-row { padding: 18px 20px 6px; }
.greeting-kicker {
  display: block; font-size: 11px; font-weight: 700;
  color: var(--primary-color); letter-spacing: 0.08em;
  text-transform: uppercase; margin-bottom: 6px;
}
.greeting-title { display: block; font-size: 30px; line-height: 1.12; font-weight: 800; color: var(--text-primary); }
.greeting-text { display: block; margin-top: 8px; font-size: 14px; line-height: 1.5; color: var(--text-secondary); }

/* ---- Feature grid ---- */
.feature-grid { display: flex; flex-wrap: wrap; gap: 12px; padding: 16px 20px 8px; }
.feature-item {
  width: calc(50% - 6px);
  display: flex; flex-direction: column; align-items: flex-start; gap: 12px;
  background: #ffffff; border: 1px solid var(--border-color);
  border-radius: 16px; padding: 16px;
  box-shadow: var(--shadow-sm); box-sizing: border-box;
}
.feature-icon {
  width: 56px; height: 56px; border-radius: 18px;
  display: flex; justify-content: center; align-items: center;
  transition: transform 0.15s ease;
}
.feature-icon:active { transform: scale(0.92); }
.fi-char { font-size: 26px; }
.icon-assess  { background: linear-gradient(145deg, #dbeafe, #bfdbfe); box-shadow: 0 4px 12px rgba(37, 99, 235, 0.12); }
.icon-map     { background: linear-gradient(145deg, #e0e7ff, #c7d2fe); box-shadow: 0 4px 12px rgba(99, 102, 241, 0.12); }
.icon-ai      { background: linear-gradient(145deg, #fae8ff, #f0abfc 30%, #e9d5ff); box-shadow: 0 4px 12px rgba(168, 85, 247, 0.12); }
.icon-interview { background: linear-gradient(145deg, #ffedd5, #fed7aa); box-shadow: 0 4px 12px rgba(249, 115, 22, 0.12); }
.feature-label { font-size: 14px; font-weight: 700; color: #1e293b; line-height: 1.25; min-height: 36px; }

/* ---- Daily check-in chip ---- */
.checkin-card {
  display: flex; align-items: center; justify-content: space-between;
  margin: 16px 20px 0; padding: 14px 16px;
  background: linear-gradient(135deg, #ecfeff, #cffafe 60%, #a5f3fc);
  border: 1px solid #bae6fd; border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}
.checkin-card:active { transform: scale(0.99); }
.checkin-left { display: flex; flex-direction: column; gap: 2px; min-width: 0; flex: 1; }
.checkin-kicker {
  font-size: 10px; font-weight: 700; color: #0e7490;
  letter-spacing: 0.08em; text-transform: uppercase;
}
.checkin-title { font-size: 18px; font-weight: 800; color: #0c4a6e; line-height: 1.1; }
.checkin-sub { font-size: 12px; color: #155e75; margin-top: 2px; }
.checkin-right {
  display: flex; flex-direction: column; align-items: flex-end; gap: 6px;
  min-width: 110px;
}
.checkin-bar {
  width: 96px; height: 6px; border-radius: 3px;
  background: rgba(14, 116, 144, 0.18); overflow: hidden;
}
.checkin-bar-fill {
  height: 100%; background: linear-gradient(90deg, #0891b2, #06b6d4);
  border-radius: 3px; transition: width 0.3s ease;
}
.checkin-cta { font-size: 12px; font-weight: 700; color: #0e7490; }
.checkin-tip {
  margin: 6px 20px 0; padding: 6px 14px;
  font-size: 11.5px; color: #64748b; line-height: 1.4;
}
.checkin-tip-text { font-size: 11.5px; color: #64748b; }

/* ---- Generic section header ---- */
.section { padding: 24px 0 0; }
.section-header {
  display: flex; justify-content: space-between; align-items: flex-end;
  padding: 0 20px; margin-bottom: 14px; gap: 12px;
}
.section-titles { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.section-title { font-size: var(--font-title); font-weight: 700; color: var(--text-primary); letter-spacing: -0.3px; }
.section-meta { font-size: 12px; color: var(--text-tertiary); }
.section-more { display: flex; align-items: center; gap: 4px; min-height: 32px; padding: 0 6px; }
.section-more-text { font-size: 13px; color: #2563eb; font-weight: 600; }
.section-more-arrow { font-size: 16px; color: #2563eb; line-height: 1; }

/* ---- Reusable cover tints (used when no image) ---- */
.cover-tone-0 { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.cover-tone-1 { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); }
.cover-tone-2 { background: linear-gradient(135deg, #fae8ff, #f0abfc); }
.cover-tone-3 { background: linear-gradient(135deg, #ffedd5, #fed7aa); }

/* ---- Horizontal scroller (used by videos) ---- */
.hscroll { width: 100%; white-space: nowrap; }
.hscroll-track { display: inline-flex; padding: 0 20px 4px; gap: 16px; padding-right: 40px; }

/* ---- Video card ---- */
.video-card {
  display: inline-flex; flex-direction: column;
  width: 240px; flex-shrink: 0;
  background: #ffffff; border-radius: var(--radius-md);
  overflow: hidden; border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm); transition: transform 0.15s;
}
.video-card:active { transform: scale(0.98); }
.video-cover { width: 100%; height: 134px; position: relative; overflow: hidden; background: #e2e8f0; }
.video-cover-img { width: 100%; height: 100%; display: block; }
.duration-badge {
  position: absolute; bottom: 8px; right: 8px;
  background: rgba(0,0,0,0.55); padding: 2px 8px; border-radius: 6px;
}
.duration-text { color: #fff; font-size: 11px; font-weight: 600; }
.play-icon-wrap {
  position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
  width: 40px; height: 40px; border-radius: 20px; background: rgba(0,0,0,0.5);
  display: flex; align-items: center; justify-content: center;
}
.play-icon { font-size: 14px; color: #fff; margin-left: 2px; }
.video-body { padding: 10px 14px 14px; white-space: normal; }
.video-title {
  font-size: 14px; font-weight: 600; color: #1e293b; line-height: 1.4;
  margin-bottom: 6px; display: -webkit-box;
  line-clamp: 2; -webkit-line-clamp: 2; -webkit-box-orient: vertical;
  overflow: hidden; height: 38px;
}
.video-meta-row { display: flex; align-items: center; gap: 4px; }
.video-up { font-size: 12px; color: #2563eb; font-weight: 600; }
.video-views { font-size: 12px; color: #94a3b8; }

/* ---- Article cards (vertical list) ---- */
.article-list { display: flex; flex-direction: column; gap: 12px; padding: 0 20px; }
.article-card {
  display: flex; align-items: stretch; gap: 14px;
  background: #ffffff; border: 1px solid var(--border-color);
  border-radius: var(--radius-md); padding: 14px;
  box-shadow: var(--shadow-sm);
  transition: transform 0.15s;
}
.article-card:active { transform: scale(0.99); }
.article-cover {
  width: 96px; height: 96px; flex-shrink: 0;
  border-radius: 12px; overflow: hidden;
}
.article-cover-img { width: 100%; height: 100%; display: block; }
.article-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 4px; }
.article-title {
  font-size: 15px; font-weight: 700; color: #0f172a;
  display: -webkit-box; line-clamp: 2; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden; line-height: 1.35;
}
.article-summary {
  font-size: 12.5px; color: #64748b; line-height: 1.45;
  display: -webkit-box; line-clamp: 2; -webkit-line-clamp: 2;
  -webkit-box-orient: vertical; overflow: hidden;
}
.article-tag {
  align-self: flex-start; margin-top: 4px;
  background: #eff6ff; padding: 3px 8px; border-radius: 6px;
}
.article-tag-text { font-size: 10px; font-weight: 700; color: #2563eb; text-transform: uppercase; letter-spacing: 0.04em; }

/* ---- Consultation cards ---- */
.consult-list { display: flex; flex-direction: column; gap: 12px; padding: 0 20px; }
.consult-card {
  background: #ffffff; border: 1px solid var(--border-color);
  border-radius: var(--radius-md); padding: 14px 16px;
  box-shadow: var(--shadow-sm);
  display: flex; flex-direction: column; gap: 6px;
}
.consult-head { display: flex; flex-direction: column; gap: 4px; }
.consult-title { font-size: 15px; font-weight: 700; color: #0f172a; line-height: 1.3; }
.consult-author { font-size: 11px; color: #94a3b8; font-weight: 500; }
.consult-body { font-size: 13px; color: #475569; line-height: 1.55; white-space: pre-line; }
.consult-link-hint { font-size: 12px; color: #2563eb; margin-top: 6px; font-weight: 500; }

/* ---- Career path spotlight ---- */
.path-grid { display: flex; flex-wrap: wrap; gap: 12px; padding: 0 20px; }
.path-card {
  width: calc(50% - 6px); box-sizing: border-box;
  border-radius: var(--radius-md); padding: 16px;
  display: flex; flex-direction: column; gap: 6px;
  border: 1px solid var(--border-color); box-shadow: var(--shadow-sm);
}
.path-name { font-size: 15px; font-weight: 700; color: #0f172a; }
.path-desc {
  font-size: 12px; color: #475569; line-height: 1.45;
  display: -webkit-box; line-clamp: 3; -webkit-line-clamp: 3;
  -webkit-box-orient: vertical; overflow: hidden;
}

/* ---- Empty / safe area ---- */
.empty-state { text-align: center; padding: 60px 20px 20px; }
.empty-icon { font-size: 48px; display: block; margin-bottom: 16px; }
.empty-text { font-size: 15px; color: #64748b; font-weight: 500; display: block; margin-bottom: 24px; }
.btn-clear {
  background: #2563eb; color: #fff; font-size: 14px; font-weight: 600;
  border-radius: 12px; height: 40px; line-height: 40px; border: none; width: 140px;
}
.bottom-safe { height: calc(var(--tab-bar-height, 50px) + 20px); }

/* ---- Dark Mode ---- */
.is-dark { background-color: #0f172a; }
.is-dark .search-bar { background: #1e293b; box-shadow: none; }
.is-dark .search-input { color: #f8fafc; }
.is-dark .feature-label { color: #e2e8f0; }
.is-dark .feature-item { background: #1e293b; box-shadow: none; border-color: #334155; }
.is-dark .section-title { color: #f8fafc; }
.is-dark .video-card,
.is-dark .article-card,
.is-dark .consult-card,
.is-dark .path-card { background: #1e293b; box-shadow: none; border-color: #334155; }
.is-dark .checkin-card { background: linear-gradient(135deg, #082f49, #0c4a6e); border-color: #0e7490; }
.is-dark .checkin-kicker { color: #67e8f9; }
.is-dark .checkin-title { color: #f0f9ff; }
.is-dark .checkin-sub { color: #bae6fd; }
.is-dark .checkin-cta { color: #67e8f9; }
.is-dark .video-title,
.is-dark .article-title,
.is-dark .consult-title,
.is-dark .path-name { color: #f8fafc; }
.is-dark .article-summary,
.is-dark .consult-body,
.is-dark .path-desc { color: #94a3b8; }

/* ================================================================
 *  MP-WEIXIN parity overrides — HARDCODED values, no CSS vars.
 *  CSS custom properties set on :root / page may not cascade into
 *  scoped component styles in the mini-program runtime, so we
 *  bypass var() entirely and write concrete values here.
 * ================================================================ */
/* #ifdef MP-WEIXIN */

/* ---- Page background: slightly more contrast vs. white cards ---- */
.home-page {
  background-color: #eaeff5;
}

/* ---- Feature grid cards ---- */
.feature-item {
  overflow: visible;
  background: #ffffff;
  border: 1.5px solid #b0bfd0;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.22),
              0 2px 6px  rgba(0,0,0,0.14);
}

/* ---- Icon pills: force GPU compositing to fix blurry border-radius ---- */
.feature-icon {
  transform: translateZ(0);
  -webkit-transform: translateZ(0);
  will-change: transform;
}

/* ---- Stronger icon-background colours ---- */
.icon-assess   { background: linear-gradient(145deg, #c3d8ff, #93b8ff); box-shadow: 0 4px 14px rgba(37,99,235,0.28); }
.icon-map      { background: linear-gradient(145deg, #ccd5ff, #a5b3fa); box-shadow: 0 4px 14px rgba(99,102,241,0.28); }
.icon-ai       { background: linear-gradient(145deg, #eed4ff, #d68ef5); box-shadow: 0 4px 14px rgba(168,85,247,0.28); }
.icon-interview{ background: linear-gradient(145deg, #ffd7b0, #ffb96a); box-shadow: 0 4px 14px rgba(249,115,22,0.28); }

/* ---- Search bar ---- */
.search-bar {
  background: #ffffff;
  border: 1.5px solid #b0bfd0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
}

/* ---- Daily check-in card ---- */
.checkin-card {
  overflow: visible;
  background: linear-gradient(135deg, #baeeff, #7dd8f5 60%, #4dc4ea);
  border: 1.5px solid #45b8d8;
  box-shadow: 0 4px 16px rgba(6,182,212,0.30),
              0 2px 6px  rgba(0,0,0,0.10);
}
.checkin-kicker { color: #065278; }
.checkin-title  { color: #031e2f; }
.checkin-sub    { color: #084d6a; }
.checkin-cta    { color: #065278; }

/* ---- Article / consult / path cards ---- */
.article-card,
.consult-card {
  overflow: visible;
  border: 1.5px solid #b0bfd0;
  box-shadow: 0 3px 14px rgba(0,0,0,0.18),
              0 1px 5px  rgba(0,0,0,0.10);
}

.path-card {
  overflow: visible;
  border: 1.5px solid #b0bfd0;
  box-shadow: 0 3px 14px rgba(0,0,0,0.18),
              0 1px 5px  rgba(0,0,0,0.10);
}

/* ---- Video cards: overflow:hidden must stay for image clipping,
        so use filter:drop-shadow which renders outside the layer ---- */
.video-card {
  overflow: hidden;
  box-shadow: none;
  filter: drop-shadow(0 3px 12px rgba(0,0,0,0.22));
}

/* #endif */
</style>
