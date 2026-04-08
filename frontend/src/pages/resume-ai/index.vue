<template>
  <view class="resume-ai-container" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">返回</text>
      </view>
      <text class="nav-title">AI简历诊断</text>
      <view class="nav-right"></view>
    </view>

    <view class="header">
      <text class="title">AI 简历诊断</text>
      <text class="subtitle">选择简历并粘贴目标 JD，系统会生成岗位定制化优化建议。</text>
    </view>

    <view class="card">
      <view class="section">
        <text class="section-title">1. 选择简历</text>
        <view class="select-box" @click="selectResume">
          <text class="s-icon">📄</text>
          <text class="s-text">{{ selectedResume || '从简历库中选择一份简历' }}</text>
          <text class="s-chevron">选择</text>
        </view>
      </view>

      <view class="section">
        <text class="section-title">2. 目标岗位 JD</text>
        <textarea
          class="jd-input"
          v-model="jdText"
          placeholder="请粘贴岗位描述，AI 会基于岗位要求进行定向诊断..."
          placeholder-class="ph"
        ></textarea>
      </view>

      <button class="btn-primary" :loading="analyzing" @click="startAnalysis">开始 AI 诊断</button>
    </view>

    <view class="loading-overlay" v-if="analyzing">
      <view class="spinner"></view>
      <text class="loading-text">{{ loadingMessage }}</text>
      <view class="progress-bar-container">
        <view class="progress-bar-fill" :style="{ width: loadingProgress + '%' }"></view>
      </view>
    </view>

    <view class="result-card" v-if="showResult">
      <view class="r-header">
        <text class="r-title">诊断结果</text>
        <view class="score-ring">
          <text class="score-val">85</text>
          <text class="score-label">匹配度</text>
        </view>
      </view>

      <view class="r-body">
        <text class="point-title">🟢 优势项</text>
        <text class="point-text">· 具备 Vue3 与 TypeScript 项目经验，和 JD 核心技术栈匹配度高。</text>
        <text class="point-text">· 项目内容体现独立负责能力，工程实践完整。</text>

        <text class="point-title mt">🔴 待优化项</text>
        <text class="point-text">· 缺少量化指标（如性能提升百分比、业务转化数据）。</text>
        <text class="point-text">· 可补充 CI/CD 与自动化测试相关经验描述。</text>
      </view>

      <button class="btn-secondary" @click="generateTailored">一键生成特化版简历</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const selectedResume = ref('');
const jdText = ref('');
const darkPref = ref(false);
const topSafeHeight = ref(44);
const analyzing = ref(false);
const showResult = ref(false);
const loadingMessage = ref('准备中...');
const loadingProgress = ref(0);

const goBack = () => {
  uni.navigateBack({ delta: 1 });
};

const selectResume = () => {
  uni.showActionSheet({
    itemList: ['前端开发岗_2026秋招.pdf', '通用简历_v2.pdf'],
    success: (res) => {
      selectedResume.value = res.tapIndex === 0 ? '前端开发岗_2026秋招.pdf' : '通用简历_v2.pdf';
    },
  });
};

const startAnalysis = () => {
  if (!selectedResume.value) {
    uni.showToast({ title: '请先选择简历', icon: 'none' });
    return;
  }
  if (!jdText.value.trim()) {
    uni.showToast({ title: '请先粘贴岗位 JD', icon: 'none' });
    return;
  }

  analyzing.value = true;
  showResult.value = false;
  loadingProgress.value = 0;

  setTimeout(() => {
    loadingMessage.value = '准备中...';
    loadingProgress.value = 12;
  }, 250);
  setTimeout(() => {
    loadingMessage.value = '提取特征中...';
    loadingProgress.value = 34;
  }, 950);
  setTimeout(() => {
    loadingMessage.value = '解析结构中...';
    loadingProgress.value = 56;
  }, 1600);
  setTimeout(() => {
    loadingMessage.value = '比对 JD 中...';
    loadingProgress.value = 78;
  }, 2400);
  setTimeout(() => {
    loadingMessage.value = '生成优化方案中...';
    loadingProgress.value = 100;
  }, 3200);

  setTimeout(() => {
    analyzing.value = false;
    showResult.value = true;
    uni.showToast({ title: '诊断完成', icon: 'success' });
  }, 3900);
};

const generateTailored = () => {
  uni.showLoading({ title: '生成中...' });
  setTimeout(() => {
    uni.hideLoading();
    uni.showToast({ title: '已生成特化版', icon: 'success' });
    setTimeout(() => {
      uni.switchTab({ url: '/pages/resume/index' });
    }, 1000);
  }, 1800);
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  const sysInfo = uni.getSystemInfoSync();
  topSafeHeight.value = (sysInfo.statusBarHeight || 20) + 8;
});
</script>

<style scoped>
.resume-ai-container { min-height: 100vh; background-color: #f5f5f7; padding: 0 20px 60px; font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif; box-sizing: border-box; }
.status-spacer { width: 100%; }
.nav-row { display: flex; align-items: center; justify-content: space-between; padding: 6px 0 14px; }
.back-btn { display: inline-flex; align-items: center; gap: 2px; color: #2563eb; width: 64px; }
.back-icon { font-size: 22px; font-weight: 300; line-height: 1; }
.back-text { font-size: 16px; font-weight: 500; }
.nav-title { font-size: 18px; font-weight: 700; color: #0f172a; }
.nav-right { width: 64px; }
.header { margin-bottom: 20px; }
.title { font-size: 28px; font-weight: 800; color: #0f172a; letter-spacing: -0.5px; display: block; margin-bottom: 8px; }
.subtitle { font-size: 14px; color: #64748b; line-height: 1.6; display: block; }
.card { background-color: #fff; border-radius: 24px; padding: 24px 20px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04); margin-bottom: 24px; }
.section { margin-bottom: 24px; }
.section-title { font-size: 16px; font-weight: 600; color: #1e293b; margin-bottom: 12px; display: block; }
.select-box { display: flex; align-items: center; background-color: #f8fafc; padding: 16px; border-radius: 16px; border: 1px solid #e2e8f0; }
.s-icon { font-size: 20px; margin-right: 12px; }
.s-text { flex: 1; min-width: 0; font-size: 15px; color: #334155; line-height: 1.45; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-right: 10px; }
.s-chevron { font-size: 12px; color: #94a3b8; font-weight: 600; flex-shrink: 0; }
.jd-input { width: 100%; height: 160px; background-color: #f8fafc; border-radius: 16px; padding: 16px; box-sizing: border-box; font-size: 15px; color: #334155; border: 1px solid #e2e8f0; line-height: 1.5; }
.ph { color: #94a3b8; }
.btn-primary { background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%); color: #fff; font-size: 16px; font-weight: 600; border-radius: 12px; height: 48px; line-height: 48px; border: none; }
.result-card { background: linear-gradient(135deg, #fff 0%, #f8fafc 100%); border-radius: 24px; padding: 24px 20px; border: 1px solid #e2e8f0; box-shadow: 0 12px 32px rgba(15, 23, 42, 0.06); }
.r-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #e2e8f0; }
.r-title { font-size: 20px; font-weight: 700; color: #0f172a; }
.score-ring { width: 64px; height: 64px; border-radius: 32px; border: 4px solid #10b981; display: flex; flex-direction: column; justify-content: center; align-items: center; background-color: #ecfdf5; }
.score-val { font-size: 20px; font-weight: 800; color: #047857; line-height: 1; }
.score-label { font-size: 10px; color: #059669; margin-top: 2px; }
.r-body { margin-bottom: 24px; }
.point-title { font-size: 15px; font-weight: 600; color: #1e293b; margin-bottom: 8px; display: block; }
.mt { margin-top: 16px; }
.point-text { font-size: 14px; color: #475569; line-height: 1.6; margin-bottom: 4px; display: block; }
.btn-secondary { background-color: #eff6ff; color: #2563eb; font-size: 15px; font-weight: 600; border-radius: 14px; height: 48px; line-height: 48px; border: none; }
.loading-overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(255, 255, 255, 0.9); backdrop-filter: blur(4px); display: flex; flex-direction: column; justify-content: center; align-items: center; z-index: 1000; }
.spinner { width: 50px; height: 50px; border: 4px solid #eff6ff; border-top: 4px solid #2563eb; border-radius: 50%; animation: spin 1s linear infinite; margin-bottom: 20px; }
@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
.loading-text { font-size: 16px; font-weight: 600; color: #1e293b; }
.progress-bar-container { width: 220px; height: 6px; background-color: #e2e8f0; border-radius: 3px; margin-top: 16px; overflow: hidden; }
.progress-bar-fill { height: 100%; background-color: #2563eb; border-radius: 3px; transition: width 0.4s cubic-bezier(0.25, 0.8, 0.25, 1); }
.is-dark { background-color: #0f172a; }
.is-dark .title,
.is-dark .section-title,
.is-dark .r-title,
.is-dark .point-title,
.is-dark .loading-text { color: #f8fafc; }
.is-dark .subtitle,
.is-dark .point-text,
.is-dark .s-text { color: #94a3b8; }
.is-dark .card,
.is-dark .result-card,
.is-dark .select-box,
.is-dark .jd-input { background-color: #1e293b; border-color: #334155; }
.is-dark .loading-overlay { background: rgba(15, 23, 42, 0.88); }
.is-dark .back-text,
.is-dark .back-icon { color: #60a5fa; }
</style>
