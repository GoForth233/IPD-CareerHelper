<template>
  <view class="profile-page">
    <!-- header -->
    <view class="profile-header">
      <view class="profile-back" @click="goBack">
        <text class="profile-back-icon">←</text>
      </view>
      <text class="profile-header-title">帮 Agent 了解你</text>
    </view>

    <!-- completeness banner -->
    <view v-if="agentProfile" class="profile-banner">
      <view class="profile-banner-bar-wrap">
        <view
          class="profile-banner-bar-fill"
          :class="'pct-' + agentProfile.personalizationLevel.toLowerCase()"
          :style="{ width: agentProfile.completenessScore + '%' }"
        ></view>
      </view>
      <text class="profile-banner-label">
        Agent 对你的了解程度：{{ agentProfile.completenessScore }}%
        （{{ levelLabel }}）
      </text>
    </view>

    <!-- form -->
    <view class="profile-form">
      <view class="profile-section">
        <text class="profile-section-title">职业目标</text>

        <view class="profile-field">
          <text class="profile-label">目标岗位城市</text>
          <input
            v-model="form.targetCity"
            class="profile-input"
            placeholder="例：上海、北京、成都"
            maxlength="30"
          />
        </view>

        <view class="profile-field">
          <text class="profile-label">目标行业</text>
          <input
            v-model="form.targetIndustry"
            class="profile-input"
            placeholder="例：互联网、金融、教育"
            maxlength="30"
          />
        </view>

        <view class="profile-field">
          <text class="profile-label">求职时间线</text>
          <view class="profile-chips">
            <view
              v-for="opt in timelineOptions"
              :key="opt"
              class="profile-chip"
              :class="{ 'profile-chip-active': form.timeline === opt }"
              @click="form.timeline = opt"
            >
              <text class="profile-chip-text">{{ opt }}</text>
            </view>
          </view>
        </view>

        <view class="profile-field">
          <text class="profile-label">职业目标备注</text>
          <textarea
            v-model="form.careerGoalNote"
            class="profile-textarea"
            placeholder="简单描述你的职业规划方向（选填）"
            maxlength="200"
          />
        </view>
      </view>

      <view class="profile-section">
        <text class="profile-section-title">学习投入</text>

        <view class="profile-field">
          <text class="profile-label">每周可投入时间</text>
          <view class="profile-chips">
            <view
              v-for="opt in weeklyOptions"
              :key="opt.val"
              class="profile-chip"
              :class="{ 'profile-chip-active': form.weeklyHours === opt.val }"
              @click="form.weeklyHours = opt.val"
            >
              <text class="profile-chip-text">{{ opt.label }}</text>
            </view>
          </view>
        </view>

        <view class="profile-field">
          <text class="profile-label">偏好任务难度</text>
          <view class="profile-chips">
            <view
              v-for="opt in difficultyOptions"
              :key="opt.val"
              class="profile-chip"
              :class="{ 'profile-chip-active': form.preferredDifficulty === opt.val }"
              @click="form.preferredDifficulty = opt.val"
            >
              <text class="profile-chip-text">{{ opt.label }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="profile-section">
        <text class="profile-section-title">升学 / 出国意向</text>
        <view class="profile-toggle-row">
          <text class="profile-label">考虑考研</text>
          <view class="profile-toggle-group">
            <view
              class="profile-toggle-btn"
              :class="{ 'profile-toggle-active': form.considerGradSchool === true }"
              @click="form.considerGradSchool = true"
            ><text class="profile-toggle-text">是</text></view>
            <view
              class="profile-toggle-btn"
              :class="{ 'profile-toggle-active': form.considerGradSchool === false }"
              @click="form.considerGradSchool = false"
            ><text class="profile-toggle-text">否</text></view>
          </view>
        </view>
        <view class="profile-toggle-row">
          <text class="profile-label">考虑出国留学</text>
          <view class="profile-toggle-group">
            <view
              class="profile-toggle-btn"
              :class="{ 'profile-toggle-active': form.considerStudyAbroad === true }"
              @click="form.considerStudyAbroad = true"
            ><text class="profile-toggle-text">是</text></view>
            <view
              class="profile-toggle-btn"
              :class="{ 'profile-toggle-active': form.considerStudyAbroad === false }"
              @click="form.considerStudyAbroad = false"
            ><text class="profile-toggle-text">否</text></view>
          </view>
        </view>
      </view>
    </view>

    <!-- submit -->
    <view class="profile-submit-wrap">
      <view
        class="profile-submit-btn"
        :class="{ 'profile-submit-loading': saving }"
        @click="saveInputs"
      >
        <text class="profile-submit-text">{{ saving ? '保存中…' : '保存并更新 Agent 画像' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getAgentProfileApi, saveProfileInputsApi, type AgentUserProfile, type ProfileInputsRequest } from '@/api/agent';

const agentProfile = ref<AgentUserProfile | null>(null);
const saving = ref(false);

const form = ref<ProfileInputsRequest>({
  targetCity: '',
  targetIndustry: '',
  timeline: '',
  weeklyHours: '',
  preferredDifficulty: '',
  considerGradSchool: undefined,
  considerStudyAbroad: undefined,
  careerGoalNote: '',
});

const timelineOptions = ['1个月', '3个月', '6个月', '校招季', '不确定'];
const weeklyOptions = [
  { label: '< 5小时', val: '3' },
  { label: '5-10小时', val: '7' },
  { label: '10-20小时', val: '15' },
  { label: '> 20小时', val: '25' },
];
const difficultyOptions = [
  { label: '轻松', val: 'EASY' },
  { label: '适中', val: 'MEDIUM' },
  { label: '挑战', val: 'HARD' },
];

const levelLabel = computed(() => {
  const lvl = agentProfile.value?.personalizationLevel || 'LOW';
  if (lvl === 'HIGH') return '非常了解你';
  if (lvl === 'MEDIUM') return '正在了解你';
  return '刚刚开始';
});

onMounted(async () => {
  try {
    agentProfile.value = await getAgentProfileApi();
  } catch { /* ignore */ }
});

const saveInputs = async () => {
  if (saving.value) return;
  saving.value = true;
  try {
    const payload: ProfileInputsRequest = {};
    if (form.value.targetCity?.trim()) payload.targetCity = form.value.targetCity.trim();
    if (form.value.targetIndustry?.trim()) payload.targetIndustry = form.value.targetIndustry.trim();
    if (form.value.timeline) payload.timeline = form.value.timeline;
    if (form.value.weeklyHours) payload.weeklyHours = form.value.weeklyHours;
    if (form.value.preferredDifficulty) payload.preferredDifficulty = form.value.preferredDifficulty;
    if (form.value.considerGradSchool !== undefined) payload.considerGradSchool = form.value.considerGradSchool;
    if (form.value.considerStudyAbroad !== undefined) payload.considerStudyAbroad = form.value.considerStudyAbroad;
    if (form.value.careerGoalNote?.trim()) payload.careerGoalNote = form.value.careerGoalNote.trim();

    agentProfile.value = await saveProfileInputsApi(payload);
    uni.showToast({ title: 'Agent 画像已更新', icon: 'success' });
    setTimeout(() => uni.navigateBack(), 800);
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' });
  } finally {
    saving.value = false;
  }
};

const goBack = () => uni.navigateBack();
</script>

<style scoped>
.profile-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 120rpx;
}
.profile-header {
  display: flex;
  align-items: center;
  padding: 56rpx 28rpx 20rpx;
  background: #fff;
  gap: 16rpx;
}
.profile-back {
  width: 64rpx; height: 64rpx;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%; background: #f1f5f9;
}
.profile-back-icon { font-size: 20px; color: #374151; }
.profile-header-title { font-size: 17px; font-weight: 700; color: #111827; }

.profile-banner {
  margin: 16rpx 28rpx;
  background: linear-gradient(135deg, #1d4ed8 0%, #6366f1 100%);
  border-radius: 18rpx;
  padding: 20rpx 24rpx;
}
.profile-banner-bar-wrap {
  height: 8rpx; background: rgba(255,255,255,.25); border-radius: 4rpx; overflow: hidden; margin-bottom: 10rpx;
}
.profile-banner-bar-fill {
  height: 100%; border-radius: 4rpx; transition: width .4s ease;
}
.pct-low    { background: #94a3b8; }
.pct-medium { background: #38bdf8; }
.pct-high   { background: #34d399; }
.profile-banner-label { font-size: 12px; color: rgba(255,255,255,.85); }

.profile-form { padding: 16rpx 28rpx; display: flex; flex-direction: column; gap: 20rpx; }
.profile-section {
  background: #fff; border-radius: 18rpx; padding: 24rpx 24rpx 8rpx;
  display: flex; flex-direction: column; gap: 20rpx;
}
.profile-section-title {
  font-size: 13px; font-weight: 700; color: #374151;
  border-left: 4rpx solid #1d4ed8; padding-left: 10rpx;
}
.profile-field { display: flex; flex-direction: column; gap: 8rpx; }
.profile-label { font-size: 12.5px; color: #6b7280; }
.profile-input {
  border: 1.5rpx solid #e5e7eb;
  border-radius: 10rpx;
  padding: 14rpx 16rpx;
  font-size: 13.5px;
  color: #111827;
  background: #f9fafb;
}
.profile-textarea {
  border: 1.5rpx solid #e5e7eb;
  border-radius: 10rpx;
  padding: 14rpx 16rpx;
  font-size: 13px;
  color: #111827;
  background: #f9fafb;
  height: 120rpx;
}
.profile-chips { display: flex; flex-wrap: wrap; gap: 10rpx; }
.profile-chip {
  border: 1.5rpx solid #d1d5db;
  border-radius: 999rpx;
  padding: 8rpx 18rpx;
  background: #f9fafb;
}
.profile-chip-active {
  border-color: #1d4ed8;
  background: #eff6ff;
}
.profile-chip-text { font-size: 12.5px; color: #374151; }
.profile-chip-active .profile-chip-text { color: #1d4ed8; font-weight: 700; }

.profile-toggle-row {
  display: flex; align-items: center; justify-content: space-between;
}
.profile-toggle-group { display: flex; gap: 10rpx; }
.profile-toggle-btn {
  border: 1.5rpx solid #d1d5db;
  border-radius: 999rpx;
  padding: 8rpx 22rpx;
  background: #f9fafb;
}
.profile-toggle-active {
  border-color: #1d4ed8; background: #eff6ff;
}
.profile-toggle-text { font-size: 12.5px; color: #374151; }
.profile-toggle-active .profile-toggle-text { color: #1d4ed8; font-weight: 700; }

.profile-submit-wrap {
  position: fixed; bottom: 0; left: 0; right: 0;
  padding: 20rpx 28rpx 48rpx;
  background: #fff;
  border-top: 1rpx solid #f1f5f9;
}
.profile-submit-btn {
  background: linear-gradient(135deg, #1d4ed8 0%, #6366f1 100%);
  border-radius: 999rpx;
  padding: 26rpx 0;
  text-align: center;
}
.profile-submit-loading { opacity: .65; }
.profile-submit-text { font-size: 15px; font-weight: 700; color: #fff; }
</style>
