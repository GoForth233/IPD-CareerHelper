<template>
  <view class="map-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">Back</text>
      </view>
      <text class="nav-title">Skill Map</text>
      <view class="nav-right" @click="switchRole">
        <text class="nav-action">Role</text>
      </view>
    </view>

    <!-- Role header card -->
    <view class="role-card">
      <view class="role-info">
        <text class="role-name" @click="switchRole">{{ currentRole }}</text>
        <text class="role-desc">Skill progression & career development roadmap</text>
      </view>
      <view class="progress-ring">
        <text class="ring-val">45%</text>
      </view>
    </view>

    <!-- Demo notice -->
    <view class="demo-notice">
      <text class="demo-text">📌 Demo: Tap any stage node to view skill details</text>
    </view>

    <!-- Skill timeline -->
    <view class="timeline">
      <view class="tl-line"></view>

      <!-- L1: Completed -->
      <view class="tl-node" @click="openDetail(0)">
        <view class="tl-dot dot-done">✅</view>
        <view class="tl-card card-done">
          <text class="tl-level">L1 · Fundamentals</text>
          <text class="tl-title">HTML / CSS</text>
          <text class="tl-desc">Semantic markup, Flexbox & Grid layout, responsive design, CSS animations</text>
          <view class="tl-badge badge-done">
            <text class="badge-text">Completed</text>
          </view>
        </view>
      </view>

      <!-- L2: In Progress -->
      <view class="tl-node" @click="openDetail(1)">
        <view class="tl-dot dot-active">
          <view class="pulse-ring"></view>
          🔥
        </view>
        <view class="tl-card card-active">
          <text class="tl-level">L2 · Core Skills</text>
          <text class="tl-title">JavaScript / TypeScript</text>
          <text class="tl-desc">ES6+ syntax, event loop, Promise async, TS generics & type system</text>
          <view class="tl-progress">
            <view class="tl-progress-bg">
              <view class="tl-progress-fill" style="width: 60%;"></view>
            </view>
            <text class="tl-progress-num">60%</text>
          </view>
        </view>
      </view>

      <!-- L3: Locked -->
      <view class="tl-node" @click="openDetail(2)">
        <view class="tl-dot dot-locked">🔒</view>
        <view class="tl-card card-locked">
          <text class="tl-level">L3 · Framework Proficiency</text>
          <text class="tl-title">Vue3 / React</text>
          <text class="tl-desc">Composition API, virtual DOM, state management, SSR basics</text>
          <view class="tl-badge badge-locked">
            <text class="badge-text">Locked</text>
          </view>
        </view>
      </view>

      <!-- L4: Locked -->
      <view class="tl-node" @click="openDetail(3)">
        <view class="tl-dot dot-locked">🔒</view>
        <view class="tl-card card-locked">
          <text class="tl-level">L4 · Architecture & Leadership</text>
          <text class="tl-title">Engineering & DevOps</text>
          <text class="tl-desc">CI/CD pipelines, micro-frontends, performance profiling, team management</text>
          <view class="tl-badge badge-locked">
            <text class="badge-text">Locked</text>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>

    <!-- Detail sheet -->
    <view class="sheet-mask" v-if="showDetail" @click="showDetail = false"></view>
    <view class="detail-sheet" :class="{ 'sheet-open': showDetail }">
      <view class="sheet-handle"></view>
      <view class="sheet-header">
        <text class="sheet-title">{{ detailData.title }}</text>
        <text class="sheet-mastery">Mastery: {{ detailData.mastery }}</text>
      </view>
      <view class="sheet-section">
        <text class="sheet-label">Core Topics</text>
        <view class="topic-tags">
          <view class="topic-tag" v-for="(t, i) in detailData.topics" :key="i">
            <text class="topic-text">{{ t }}</text>
          </view>
        </view>
      </view>
      <view class="sheet-section">
        <text class="sheet-label">Learning Advice</text>
        <text class="sheet-advice">{{ detailData.advice }}</text>
      </view>
      <button class="sheet-btn" @click="openLink(detailData.url, detailData.title)">Start Learning</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { openLink } from '@/utils/openLink';

const darkPref = ref(false);
const topSafeHeight = ref(44);
const showDetail = ref(false);
const currentRole = ref('Frontend Developer');

const detailDataList = [
  {
    title: 'HTML / CSS',
    mastery: '100%',
    topics: ['HTML5 Semantics', 'Flexbox & Grid', 'Responsive Design', 'CSS Animations'],
    advice: 'You\'ve completed the fundamentals. Consider diving into accessibility (WCAG standards) and CSS-in-JS solutions to broaden your toolkit.',
    url: 'https://www.freecodecamp.org/learn/2022/responsive-web-design/'
  },
  {
    title: 'JavaScript / TypeScript',
    mastery: '60%',
    topics: ['ES6+ Features', 'Event Loop', 'Promise & Async/Await', 'TS Generics'],
    advice: 'Focus on understanding the event loop and microtask queue deeply. Practice implementing Promise.all from scratch. For TypeScript, master generics and utility types — they\'re interview favorites.',
    url: 'https://javascript.info/'
  },
  {
    title: 'Vue3 / React',
    mastery: '0%',
    topics: ['Composition API', 'Virtual DOM Diffing', 'Pinia / Redux', 'SSR with Nuxt/Next'],
    advice: 'Complete L2 first to unlock this stage. Once ready, start with Vue3 Composition API since you\'re already using it in this project.',
    url: 'https://vuejs.org/guide/introduction.html'
  },
  {
    title: 'Engineering & DevOps',
    mastery: '0%',
    topics: ['Webpack / Vite Config', 'CI/CD Pipelines', 'Micro-frontend Architecture', 'Performance Profiling'],
    advice: 'This is the senior-level stage. Focus on building real CI/CD pipelines and understanding build tool internals. Leading a team project will accelerate growth here.',
    url: 'https://roadmap.sh/frontend'
  }
];

const detailData = ref(detailDataList[0]);

const openDetail = (idx: number) => {
  detailData.value = detailDataList[idx];
  showDetail.value = true;
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const switchRole = () => {
  uni.showActionSheet({
    itemList: ['Frontend Developer', 'Backend Developer', 'Product Manager', 'Data Analyst'],
    success: (res) => {
      const roles = ['Frontend Developer', 'Backend Developer', 'Product Manager', 'Data Analyst'];
      currentRole.value = roles[res.tapIndex];
      uni.showToast({ title: `Switched to ${currentRole.value}`, icon: 'none' });
    }
  });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const systemInfo = uni.getSystemInfoSync();
  topSafeHeight.value = (systemInfo.statusBarHeight || 20) + 8;
});
</script>

<style scoped>
.map-page {
  min-height: 100vh;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  padding: 0 20px;
  box-sizing: border-box;
}

.status-spacer { width: 100%; }

.nav-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0 14px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: #2563eb;
  width: var(--nav-back-width);
}

.back-icon { font-size: 22px; font-weight: 300; line-height: 1; }

.back-text { font-size: 16px; font-weight: 500; }

.nav-title { font-size: 18px; font-weight: 700; color: #0f172a; letter-spacing: -0.3px; }

.nav-right { width: 64px; text-align: right; }

.nav-action {
  font-size: 13px;
  color: #2563eb;
  font-weight: 600;
  background: #eff6ff;
  border-radius: 999px;
  padding: 4px 10px;
  display: inline-block;
}

.role-card {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  border-radius: 20px; padding: 24px; color: #ffffff;
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.15);
}

.role-info { flex: 1; }

.role-name { font-size: 20px; font-weight: 700; display: block; margin-bottom: 6px; }

.role-desc { font-size: 13px; opacity: 0.7; }

.progress-ring {
  width: 56px; height: 56px; border-radius: 28px;
  border: 3px solid #60a5fa; display: flex;
  justify-content: center; align-items: center;
  background: rgba(96, 165, 250, 0.1);
}

.ring-val { font-size: 16px; font-weight: 700; color: #60a5fa; }

.demo-notice {
  background: #eff6ff; border-radius: 12px; padding: 10px 14px;
  margin-bottom: 20px;
}

.demo-text { font-size: 12px; color: #2563eb; }

/* Timeline */
.timeline { position: relative; padding-left: 28px; padding-bottom: 20px; }

.tl-line {
  position: absolute; left: 12px; top: 20px; bottom: 20px;
  width: 2px; background: #e2e8f0;
}

.tl-node { position: relative; margin-bottom: 24px; }

.tl-dot {
  position: absolute; left: -28px; top: 16px;
  width: 28px; height: 28px; border-radius: 14px;
  display: flex; justify-content: center; align-items: center;
  font-size: 14px; z-index: 2;
  background: #ffffff;
}

.dot-done { background: #dcfce7; }

.dot-active {
  background: #dbeafe; position: relative;
}

.pulse-ring {
  position: absolute; width: 36px; height: 36px;
  border-radius: 18px; border: 2px solid #3b82f6;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0% { transform: scale(0.8); opacity: 1; }
  100% { transform: scale(1.4); opacity: 0; }
}

.dot-locked { background: #f1f5f9; opacity: 0.6; }

.tl-card {
  background: #ffffff; border-radius: 16px; padding: 18px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04);
}

.card-done { border-left: 3px solid #22c55e; }

.card-active { border-left: 3px solid #3b82f6; box-shadow: 0 4px 16px rgba(59, 130, 246, 0.12); }

.card-locked { opacity: 0.5; }

.tl-level { font-size: 11px; font-weight: 600; color: #94a3b8; display: block; margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px; }

.tl-title { font-size: 17px; font-weight: 700; color: #1e293b; display: block; margin-bottom: 6px; }

.tl-desc { font-size: 13px; color: #64748b; line-height: 1.5; display: block; margin-bottom: 10px; }

.tl-badge { display: inline-flex; padding: 3px 10px; border-radius: 8px; }

.badge-done { background: #dcfce7; }
.badge-done .badge-text { color: #16a34a; font-size: 11px; font-weight: 600; }

.badge-locked { background: #f1f5f9; }
.badge-locked .badge-text { color: #94a3b8; font-size: 11px; font-weight: 600; }

.tl-progress { display: flex; align-items: center; gap: 10px; }

.tl-progress-bg { flex: 1; height: 6px; background: #e2e8f0; border-radius: 3px; overflow: hidden; }

.tl-progress-fill { height: 100%; background: #3b82f6; border-radius: 3px; }

.tl-progress-num { font-size: 12px; font-weight: 600; color: #3b82f6; }

.bottom-safe { height: 40px; }

/* Detail sheet */
.sheet-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.35); z-index: 998;
}

.detail-sheet {
  position: fixed; left: 0; right: 0; bottom: -450px;
  background: #ffffff; border-radius: 24px 24px 0 0;
  padding: 12px 24px calc(24px + env(safe-area-inset-bottom));
  z-index: 999; transition: bottom 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  max-height: 70vh; overflow-y: auto;
}

.sheet-open { bottom: 0; }

.sheet-handle {
  width: 36px; height: 5px; border-radius: 3px;
  background: #e2e8f0; margin: 0 auto 16px;
}

.sheet-header { margin-bottom: 20px; }

.sheet-title { font-size: 22px; font-weight: 700; color: #0f172a; display: block; margin-bottom: 4px; }

.sheet-mastery { font-size: 14px; color: #64748b; }

.sheet-section { margin-bottom: 20px; }

.sheet-label { font-size: 13px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 10px; }

.topic-tags { display: flex; flex-wrap: wrap; gap: 8px; }

.topic-tag { background: #eff6ff; padding: 6px 14px; border-radius: 12px; }

.topic-text { font-size: 13px; font-weight: 500; color: #2563eb; }

.sheet-advice { font-size: 14px; color: #475569; line-height: 1.6; }

.sheet-btn {
  width: 100%; background: #2563eb; color: #ffffff;
  font-size: 16px; font-weight: 600; border-radius: 14px;
  height: 48px; line-height: 48px; border: none; margin-top: 8px;
}

.sheet-btn:active { background: #1d4ed8; }

/* Dark mode */
.is-dark { background: #0f172a; }

.is-dark .nav-title,
.is-dark .tl-title,
.is-dark .sheet-title { color: #f8fafc; }

.is-dark .tl-card,
.is-dark .detail-sheet { background: #1e293b; }

.is-dark .tl-desc,
.is-dark .sheet-advice { color: #94a3b8; }

.is-dark .demo-notice { background: #1e293b; }
</style>
