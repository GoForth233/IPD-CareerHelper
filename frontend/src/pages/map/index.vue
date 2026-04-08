<template>
  <view class="map-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">返回</text>
      </view>
      <text class="nav-title">技能地图</text>
      <view class="nav-right" @click="switchRole">
        <text class="nav-action">切换岗位</text>
      </view>
    </view>

    <view class="role-card">
      <view class="role-info">
        <text class="role-name" @click="switchRole">{{ currentRole }}</text>
        <text class="role-desc">技能成长路径与职业发展路线图</text>
      </view>
      <view class="progress-ring">
        <text class="ring-val">45%</text>
      </view>
    </view>

    <view class="demo-notice">
      <text class="demo-text">📌 点击任意阶段节点可查看技能详情</text>
    </view>

    <view class="timeline">
      <view class="tl-line"></view>

      <view class="tl-node" @click="openDetail(0)">
        <view class="tl-dot dot-done">✅</view>
        <view class="tl-card card-done">
          <text class="tl-level">L1 · 入门基础</text>
          <text class="tl-title">HTML / CSS</text>
          <text class="tl-desc">语义化标签、布局、响应式与动画基础</text>
          <view class="tl-badge badge-done">
            <text class="badge-text">已完成</text>
          </view>
        </view>
      </view>

      <view class="tl-node" @click="openDetail(1)">
        <view class="tl-dot dot-active">
          <view class="pulse-ring"></view>
          🔥
        </view>
        <view class="tl-card card-active">
          <text class="tl-level">L2 · 核心能力</text>
          <text class="tl-title">JavaScript / TypeScript</text>
          <text class="tl-desc">ES6+、事件循环、Promise 异步与 TS 泛型</text>
          <view class="tl-progress">
            <view class="tl-progress-bg">
              <view class="tl-progress-fill" style="width: 60%;"></view>
            </view>
            <text class="tl-progress-num">60%</text>
          </view>
        </view>
      </view>

      <view class="tl-node" @click="openDetail(2)">
        <view class="tl-dot dot-locked">🔒</view>
        <view class="tl-card card-locked">
          <text class="tl-level">L3 · 框架进阶</text>
          <text class="tl-title">Vue3 / React</text>
          <text class="tl-desc">组合式 API、状态管理与 SSR 基础</text>
          <view class="tl-badge badge-locked">
            <text class="badge-text">未解锁</text>
          </view>
        </view>
      </view>

      <view class="tl-node" @click="openDetail(3)">
        <view class="tl-dot dot-locked">🔒</view>
        <view class="tl-card card-locked">
          <text class="tl-level">L4 · 架构与工程</text>
          <text class="tl-title">工程体系与 DevOps</text>
          <text class="tl-desc">CI/CD、性能优化、工程化规范与协作能力</text>
          <view class="tl-badge badge-locked">
            <text class="badge-text">未解锁</text>
          </view>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>

    <view class="sheet-mask" v-if="showDetail" @click="showDetail = false"></view>
    <view class="detail-sheet" :class="{ 'sheet-open': showDetail }">
      <view class="sheet-handle"></view>
      <view class="sheet-header">
        <text class="sheet-title">{{ detailData.title }}</text>
        <text class="sheet-mastery">掌握度：{{ detailData.mastery }}</text>
      </view>
      <view class="sheet-section">
        <text class="sheet-label">核心考点</text>
        <view class="topic-tags">
          <view class="topic-tag" v-for="(t, i) in detailData.topics" :key="i">
            <text class="topic-text">{{ t }}</text>
          </view>
        </view>
      </view>
      <view class="sheet-section">
        <text class="sheet-label">学习建议</text>
        <text class="sheet-advice">{{ detailData.advice }}</text>
      </view>
      <button class="sheet-btn" @click="openResource(detailData.url)">开始学习</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const darkPref = ref(false);
const topSafeHeight = ref(44);
const showDetail = ref(false);
const currentRole = ref('前端开发工程师');

type Detail = {
  title: string;
  mastery: string;
  topics: string[];
  advice: string;
  url: string;
};

const detailDataList: Detail[] = [
  {
    title: 'HTML / CSS',
    mastery: '100%',
    topics: ['语义化标签', 'Flex / Grid 布局', '响应式设计', 'CSS 动画'],
    advice: '基础已完成，建议继续补充无障碍规范（WCAG）和复杂布局实战，提升页面工程质量。',
    url: 'https://www.freecodecamp.org/learn/2022/responsive-web-design/',
  },
  {
    title: 'JavaScript / TypeScript',
    mastery: '60%',
    topics: ['ES6+', 'Event Loop', 'Promise', 'TS 泛型'],
    advice: '重点攻克事件循环和异步流程，建议手写 Promise.all 与防抖节流；TS 重点练习泛型与工具类型。',
    url: 'https://javascript.info/',
  },
  {
    title: 'Vue3 / React',
    mastery: '0%',
    topics: ['Composition API', 'Virtual DOM', 'Pinia/Redux', 'SSR'],
    advice: '先完成 L2 再进入该阶段。优先从你当前项目使用的 Vue3 生态入手，逐步扩展到 React。',
    url: 'https://vuejs.org/guide/introduction.html',
  },
  {
    title: '工程体系与 DevOps',
    mastery: '0%',
    topics: ['构建配置', 'CI/CD', '微前端', '性能分析'],
    advice: '该阶段偏中高级，建议在实战中落地自动化部署和性能监控，建立完整工程思维。',
    url: 'https://roadmap.sh/frontend',
  },
];

const detailData = ref<Detail>(detailDataList[0]);

const openDetail = (idx: number) => {
  detailData.value = detailDataList[idx];
  showDetail.value = true;
};

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const switchRole = () => {
  uni.showActionSheet({
    itemList: ['前端开发工程师', '后端开发工程师', '产品经理', '数据分析师'],
    success: (res) => {
      const roles = ['前端开发工程师', '后端开发工程师', '产品经理', '数据分析师'];
      currentRole.value = roles[res.tapIndex];
      uni.showToast({ title: `已切换为${currentRole.value}`, icon: 'none' });
    },
  });
};

const openResource = (url: string) => {
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

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const systemInfo = uni.getSystemInfoSync();
  topSafeHeight.value = (systemInfo.statusBarHeight || 20) + 8;

  const recommendedRole = uni.getStorageSync('assessment_recommended_role');
  if (recommendedRole) currentRole.value = recommendedRole;
});
</script>

<style scoped>
.map-page { min-height: 100vh; background: #f5f5f7; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; padding: 0 20px; box-sizing: border-box; }
.status-spacer { width: 100%; }
.nav-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0 14px; }
.back-btn { display: inline-flex; align-items: center; gap: 2px; color: #2563eb; width: 70px; }
.back-icon { font-size: 22px; font-weight: 300; line-height: 1; }
.back-text { font-size: 16px; font-weight: 500; }
.nav-title { font-size: 18px; font-weight: 700; color: #0f172a; letter-spacing: -0.3px; }
.nav-right { width: 88px; text-align: right; }
.nav-action { font-size: 13px; color: #2563eb; font-weight: 600; background: #eff6ff; border-radius: 999px; padding: 4px 10px; display: inline-block; }
.role-card { background: linear-gradient(135deg, #1e293b 0%, #334155 100%); border-radius: 20px; padding: 24px; color: #fff; display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.15); }
.role-info { flex: 1; }
.role-name { font-size: 20px; font-weight: 700; display: block; margin-bottom: 6px; }
.role-desc { font-size: 13px; opacity: 0.7; }
.progress-ring { width: 56px; height: 56px; border-radius: 28px; border: 3px solid #60a5fa; display: flex; justify-content: center; align-items: center; background: rgba(96, 165, 250, 0.1); }
.ring-val { font-size: 16px; font-weight: 700; color: #60a5fa; }
.demo-notice { background: #eff6ff; border-radius: 12px; padding: 10px 14px; margin-bottom: 20px; }
.demo-text { font-size: 12px; color: #2563eb; }
.timeline { position: relative; padding-left: 28px; padding-bottom: 20px; }
.tl-line { position: absolute; left: 12px; top: 20px; bottom: 20px; width: 2px; background: #e2e8f0; }
.tl-node { position: relative; margin-bottom: 24px; }
.tl-dot { position: absolute; left: -28px; top: 16px; width: 28px; height: 28px; border-radius: 14px; display: flex; justify-content: center; align-items: center; font-size: 14px; z-index: 2; background: #fff; }
.dot-done { background: #dcfce7; }
.dot-active { background: #dbeafe; position: relative; }
.pulse-ring { position: absolute; width: 36px; height: 36px; border-radius: 18px; border: 2px solid #3b82f6; animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0% { transform: scale(0.8); opacity: 1; } 100% { transform: scale(1.4); opacity: 0; } }
.dot-locked { background: #f1f5f9; opacity: 0.6; }
.tl-card { background: #fff; border-radius: 16px; padding: 18px; box-shadow: 0 2px 10px rgba(15, 23, 42, 0.04); }
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
.sheet-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.35); z-index: 998; }
.detail-sheet { position: fixed; left: 0; right: 0; bottom: -450px; background: #fff; border-radius: 24px 24px 0 0; padding: 12px 24px calc(24px + env(safe-area-inset-bottom)); z-index: 999; transition: bottom 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); max-height: 70vh; overflow-y: auto; }
.sheet-open { bottom: 0; }
.sheet-handle { width: 36px; height: 5px; border-radius: 3px; background: #e2e8f0; margin: 0 auto 16px; }
.sheet-header { margin-bottom: 20px; }
.sheet-title { font-size: 22px; font-weight: 700; color: #0f172a; display: block; margin-bottom: 4px; }
.sheet-mastery { font-size: 14px; color: #64748b; }
.sheet-section { margin-bottom: 20px; }
.sheet-label { font-size: 13px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; display: block; margin-bottom: 10px; }
.topic-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.topic-tag { background: #eff6ff; padding: 6px 14px; border-radius: 12px; }
.topic-text { font-size: 13px; font-weight: 500; color: #2563eb; }
.sheet-advice { font-size: 14px; color: #475569; line-height: 1.6; }
.sheet-btn { width: 100%; background: #2563eb; color: #fff; font-size: 16px; font-weight: 600; border-radius: 14px; height: 48px; line-height: 48px; border: none; margin-top: 8px; }
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
