<template>
  <view class="checkin-page" :class="[themeClass, fontClass]">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="nav-row">
      <view class="back-btn" @click="goBack">
        <text class="back-icon">‹</text>
        <text class="back-text">{{ t('common.back') }}</text>
      </view>
      <text class="nav-title">{{ t('checkin.navTitle') }}</text>
      <view class="nav-spacer"></view>
    </view>

    <view class="hero-card">
      <view class="hero-row">
        <view class="hero-streak">
          <text class="streak-num">{{ status?.streakDays || 0 }}</text>
          <text class="streak-label">{{ t('checkin.dayStreakLabel') }}</text>
        </view>
        <view class="hero-progress">
          <text class="hero-progress-text">{{ status?.todayCompleted || 0 }}/{{ status?.todayTotal || 3 }}</text>
          <text class="hero-progress-label">{{ t('checkin.tasksDoneToday') }}</text>
        </view>
      </view>
      <view class="hero-bar">
        <view class="hero-bar-fill" :style="{ width: progressPercent + '%' }"></view>
      </view>
      <text class="hero-tip">{{ heroTip }}</text>
    </view>

    <view class="actions-card">
      <text class="actions-title">{{ t('checkin.todayTasksTitle') }}</text>
      <view class="action-list">
        <view
          v-for="a in actionItems"
          :key="a.code"
          :class="['action-row', a.done ? 'action-done' : '']"
          @click="navTo(a.target)"
        >
          <view class="action-icon" :class="a.tone">{{ a.icon }}</view>
          <view class="action-body">
            <text class="action-name">{{ a.label }}</text>
            <text class="action-desc">{{ a.done ? t('checkin.completedToday') : a.cta }}</text>
          </view>
          <text class="action-arrow">›</text>
        </view>
      </view>
    </view>

    <view class="week-card">
      <text class="week-title">{{ t('checkin.last7Title') }}</text>
      <view class="week-grid">
        <view
          v-for="(d, idx) in last7Days"
          :key="idx"
          :class="['week-cell', d.active ? 'week-cell-active' : '', d.isToday ? 'week-cell-today' : '']"
        >
          <text class="week-cell-dow">{{ d.dow }}</text>
          <text class="week-cell-day">{{ d.dayLabel }}</text>
          <view class="week-cell-dot" v-if="d.active"></view>
        </view>
      </view>
      <text class="week-meta">{{ t('checkin.weekMeta', { n: status?.weeklyDays || 0 }) }}</text>
      <view class="badge-row" v-if="status?.badgeEarnedThisWeek">
        <text class="badge-icon">🏆</text>
        <text class="badge-text">{{ t('checkin.weeklyBadge') }}</text>
      </view>
    </view>

    <view class="bottom-safe"></view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { onShow } from '@dcloudio/uni-app';
import { getTopSafeHeight } from '@/utils/safeArea';
import { getCheckInStatusApi, getCheckInCalendarApi, type CheckInStatus, type CheckInDay } from '@/api/checkin';
import { useTheme } from '@/utils/theme';

const { t } = useI18n();
const { themeClass, fontClass, refresh: refreshTheme } = useTheme();
const topSafeHeight = ref(52);

const status = ref<CheckInStatus | null>(null);
const calendar = ref<CheckInDay[]>([]);

const goBack = () => uni.navigateBack({ delta: 1 });

const heroTip = computed(() => {
  if (!status.value) return t('checkin.heroLoading');
  if (status.value.todayCompleted >= status.value.todayTotal) return t('checkin.heroAllDone');
  if (status.value.todayCompleted > 0) return t('checkin.heroAlmostDone');
  return t('checkin.heroStartStreak');
});

const progressPercent = computed(() => {
  if (!status.value || status.value.todayTotal === 0) return 0;
  return Math.round((status.value.todayCompleted / status.value.todayTotal) * 100);
});

interface ActionItem {
  code: string;
  label: string;
  cta: string;
  icon: string;
  tone: string;
  target: string;
  done: boolean;
}

const actionItems = computed<ActionItem[]>(() => {
  const done = new Set(status.value?.completedActionsToday || []);
  return [
    {
      code: 'ASSESSMENT',
      label: t('checkin.actionAssessmentLabel'),
      cta: t('checkin.actionAssessmentCta'),
      icon: '�',
      tone: 'tone-blue',
      target: '/pages/assessment/index',
      done: done.has('ASSESSMENT'),
    },
    {
      code: 'INTERVIEW',
      label: t('checkin.actionInterviewLabel'),
      cta: t('checkin.actionInterviewCta'),
      icon: '🎤',
      tone: 'tone-orange',
      target: '/pages/interview/start',
      done: done.has('INTERVIEW'),
    },
    {
      code: 'SKILL_NODE',
      label: t('checkin.actionSkillLabel'),
      cta: t('checkin.actionSkillCta'),
      icon: '🗺',
      tone: 'tone-violet',
      target: '/pages/map/index',
      done: done.has('SKILL_NODE'),
    },
  ];
});

interface DayCell {
  date: string;
  dow: string;
  dayLabel: string;
  active: boolean;
  isToday: boolean;
}

const last7Days = computed<DayCell[]>(() => {
  const days: DayCell[] = [];
  const dowNames = [t('checkin.dow.sun'), t('checkin.dow.mon'), t('checkin.dow.tue'), t('checkin.dow.wed'), t('checkin.dow.thu'), t('checkin.dow.fri'), t('checkin.dow.sat')];
  const today = new Date();
  const activeSet = new Set(calendar.value.map((c) => c.day));
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    days.push({
      date: iso,
      dow: dowNames[d.getDay()],
      dayLabel: String(d.getDate()),
      active: activeSet.has(iso),
      isToday: i === 0,
    });
  }
  return days;
});

const navTo = (url: string) => {
  uni.navigateTo({ url });
};

const load = async () => {
  try {
    const [s, c] = await Promise.all([getCheckInStatusApi(), getCheckInCalendarApi()]);
    status.value = s;
    calendar.value = c || [];
  } catch (e: any) {
    uni.showToast({ title: e?.message || 'Failed to load check-in', icon: 'none' });
  }
};

onMounted(() => {
  refreshTheme();
  topSafeHeight.value = getTopSafeHeight();
});

onShow(() => {
  refreshTheme();
  load();
});
</script>

<style scoped>
.checkin-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  padding: 0 20px 24px;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}
.status-spacer { width: 100%; }
.nav-row { display: flex; align-items: center; height: 44px; padding: 0 2px; margin-bottom: 4px; }
.back-btn { display: inline-flex; align-items: center; gap: 2px; color: #2563eb; width: 64px; }
.back-icon { font-size: 24px; font-weight: 300; line-height: 1; }
.back-text { font-size: 16px; font-weight: 500; }
.nav-title { flex: 1; text-align: center; font-size: 17px; font-weight: 600; color: #0f172a; letter-spacing: -0.3px; }
.nav-spacer { width: 64px; }

.hero-card {
  background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
  border-radius: var(--radius-lg);
  padding: 22px 20px;
  color: #fff;
  margin-top: 8px;
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.32);
}
.hero-row { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 14px; }
.hero-streak { display: flex; align-items: baseline; gap: 6px; }
.streak-num { font-size: 44px; font-weight: 800; letter-spacing: -1px; line-height: 1; }
.streak-label { font-size: 13px; opacity: 0.85; }
.hero-progress { text-align: right; display: flex; flex-direction: column; gap: 2px; }
.hero-progress-text { font-size: 18px; font-weight: 700; }
.hero-progress-label { font-size: 11px; opacity: 0.85; }
.hero-bar { width: 100%; height: 8px; background: rgba(255, 255, 255, 0.25); border-radius: 999px; overflow: hidden; }
.hero-bar-fill { height: 100%; background: #f8fafc; border-radius: 999px; transition: width 0.3s; }
.hero-tip { display: block; margin-top: 14px; font-size: 13px; opacity: 0.92; line-height: 1.5; }

.actions-card { margin-top: 20px; background: #fff; border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 18px; box-shadow: var(--shadow-sm); }
.actions-title { font-size: 13px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.06em; display: block; margin-bottom: 14px; }
.action-list { display: flex; flex-direction: column; gap: 10px; }
.action-row {
  display: flex; align-items: center; gap: 14px;
  background: #f8fafc; border: 1px solid var(--border-color);
  border-radius: 14px; padding: 12px 14px;
  transition: transform 0.15s;
}
.action-row:active { transform: scale(0.99); }
.action-done { background: #ecfdf5; border-color: #86efac; }
.action-icon {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.tone-blue { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.tone-orange { background: linear-gradient(135deg, #ffedd5, #fed7aa); }
.tone-violet { background: linear-gradient(135deg, #ede9fe, #c4b5fd); }
.action-body { flex: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px; }
.action-name { font-size: 14px; font-weight: 700; color: #0f172a; }
.action-desc { font-size: 12px; color: #64748b; }
.action-done .action-desc { color: #047857; font-weight: 600; }
.action-arrow { font-size: 18px; color: #c7c7cc; line-height: 1; }

.week-card { margin-top: 20px; background: #fff; border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 18px; box-shadow: var(--shadow-sm); }
.week-title { font-size: 13px; font-weight: 700; color: #64748b; text-transform: uppercase; letter-spacing: 0.06em; display: block; margin-bottom: 14px; }
.week-grid { display: flex; gap: 8px; }
.week-cell {
  flex: 1; aspect-ratio: 1 / 1.1;
  background: #f1f5f9; border-radius: 12px;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 2px; position: relative;
}
.week-cell-active { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.week-cell-today { border: 2px solid #2563eb; }
.week-cell-dow { font-size: 11px; color: #64748b; font-weight: 600; }
.week-cell-day { font-size: 14px; color: #0f172a; font-weight: 700; }
.week-cell-dot { width: 6px; height: 6px; border-radius: 3px; background: #2563eb; position: absolute; bottom: 6px; }
.week-meta { display: block; margin-top: 12px; font-size: 12px; color: #64748b; }
.badge-row { display: flex; align-items: center; gap: 8px; margin-top: 12px; padding: 10px 12px; background: #fef3c7; border-radius: 10px; }
.badge-icon { font-size: 18px; }
.badge-text { font-size: 12.5px; color: #92400e; font-weight: 600; line-height: 1.4; }

.bottom-safe { height: calc(env(safe-area-inset-bottom, 0px) + 24px); }

.is-dark { background-color: #0f172a; }
.is-dark .nav-title { color: #f8fafc; }
.is-dark .actions-card,
.is-dark .week-card { background: #1e293b; box-shadow: none; border-color: #334155; }
.is-dark .actions-title,
.is-dark .week-title,
.is-dark .week-meta { color: #94a3b8; }
.is-dark .action-row { background: #0f172a; border-color: #334155; }
.is-dark .action-name { color: #f8fafc; }
.is-dark .action-desc { color: #94a3b8; }
.is-dark .action-done { background: rgba(16, 185, 129, 0.16); border-color: #34d399; }
.is-dark .action-done .action-desc { color: #6ee7b7; }
.is-dark .week-cell { background: #0f172a; }
.is-dark .week-cell-day { color: #f8fafc; }
.is-dark .week-cell-dow { color: #94a3b8; }
</style>
