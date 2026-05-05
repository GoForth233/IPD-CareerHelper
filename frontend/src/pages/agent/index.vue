<template>
  <view class="hub-page">
    <!-- header -->
    <view class="hub-header">
      <view class="hub-back" @click="goBack"><text class="hub-back-icon">←</text></view>
      <text class="hub-title">Career Agent Hub</text>
      <view class="hub-refresh" @click="loadAll"><text class="hub-refresh-icon">↻</text></view>
    </view>

    <scroll-view scroll-y class="hub-scroll">

      <!-- ① Profile completeness -->
      <view class="hub-section" @click="navTo('/pages/agent/profile')">
        <view class="hub-section-head">
          <text class="hub-section-title">Agent 对你的了解</text>
          <text class="hub-section-arrow">›</text>
        </view>
        <view v-if="agentProfile" class="hub-pct-wrap">
          <view class="hub-pct-bar">
            <view
              class="hub-pct-fill"
              :class="'pct-' + agentProfile.personalizationLevel.toLowerCase()"
              :style="{ width: agentProfile.completenessScore + '%' }"
            />
          </view>
          <text class="hub-pct-label">{{ levelLabel }} · {{ agentProfile.completenessScore }}%</text>
        </view>
        <view v-if="agentProfile?.missingSignals?.length" class="hub-missing-row">
          <view
            v-for="sig in agentProfile.missingSignals.slice(0, 3)"
            :key="sig.key"
            class="hub-missing-chip"
            :class="'chip-' + sig.priority.toLowerCase()"
          >
            <text class="hub-missing-chip-text">+ {{ sig.label }}</text>
          </view>
        </view>
      </view>

      <!-- ② Today -->
      <view v-if="agentToday" class="hub-section">
        <view class="hub-section-head">
          <text class="hub-section-title">今日 Agent</text>
          <view class="hub-stage-pill"><text class="hub-stage-text">{{ stageLabel }}</text></view>
        </view>
        <text class="hub-headline">{{ agentToday.headline }}</text>
        <text class="hub-focus">{{ agentToday.todayFocus }}</text>
        <view class="hub-progress-bar">
          <view class="hub-progress-fill" :style="{ width: agentToday.progressPercent + '%' }" />
        </view>
        <text class="hub-progress-text">{{ agentToday.progressPercent }}% 就绪度</text>
        <view v-if="agentToday.actions?.length" class="hub-actions">
          <view
            v-for="action in agentToday.actions.slice(0, 3)"
            :key="action.label"
            class="hub-action"
            :class="{ 'hub-action-primary': action.priority === 'HIGH' }"
            @click="navTo(action.target)"
          >
            <text class="hub-action-text">{{ action.label }}</text>
          </view>
        </view>
      </view>

      <!-- ③ Tasks -->
      <view class="hub-section">
        <view class="hub-section-head">
          <text class="hub-section-title">任务列表</text>
          <text class="hub-task-count">{{ openTasks.length }} 个进行中</text>
        </view>
        <view v-if="openTasks.length === 0" class="hub-empty">
          <text class="hub-empty-text">暂无待办任务，今日表现不错 🎉</text>
        </view>
        <view v-for="task in openTasks" :key="task.taskId" class="hub-task">
          <view class="hub-task-header" @click="toggleTask(task.taskId)">
            <view class="hub-task-meta">
              <view class="hub-task-badges">
                <view v-if="task.difficulty" class="hub-badge" :class="'diff-' + task.difficulty.toLowerCase()">
                  <text class="hub-badge-text">{{ task.difficulty }}</text>
                </view>
                <view v-if="task.estimatedMinutes" class="hub-badge hub-badge-time">
                  <text class="hub-badge-text">{{ task.estimatedMinutes }}min</text>
                </view>
              </view>
              <text class="hub-task-title">{{ task.title }}</text>
              <text v-if="task.description" class="hub-task-desc">{{ task.description }}</text>
            </view>
            <view class="hub-task-right">
              <view class="hub-task-actions">
                <view class="hub-task-btn hub-task-done" @click.stop="completeTask(task.taskId)">
                  <text class="hub-task-btn-text">Done</text>
                </view>
                <view class="hub-task-btn" @click.stop="dismissTask(task.taskId)">
                  <text class="hub-task-btn-text">Skip</text>
                </view>
              </view>
              <view class="hub-expand-btn" @click.stop="expandTask(task)">
                <text class="hub-expand-text">{{ expandedTasks.has(task.taskId) ? '收起' : '拆解' }}</text>
              </view>
            </view>
          </view>
          <!-- subtasks -->
          <view v-if="expandedTasks.has(task.taskId)" class="hub-subtasks">
            <view v-if="subtaskLoading.has(task.taskId)" class="hub-subtask-loading">
              <text class="hub-subtask-loading-text">正在生成子任务…</text>
            </view>
            <view
              v-for="sub in subtaskMap.get(task.taskId) || []"
              :key="sub.taskId"
              class="hub-subtask"
              :class="{ 'hub-subtask-done': sub.status === 'DONE' }"
            >
              <view class="hub-subtask-badges">
                <view v-if="sub.difficulty" class="hub-badge" :class="'diff-' + sub.difficulty.toLowerCase()">
                  <text class="hub-badge-text">{{ sub.difficulty }}</text>
                </view>
                <view v-if="sub.estimatedMinutes" class="hub-badge hub-badge-time">
                  <text class="hub-badge-text">{{ sub.estimatedMinutes }}min</text>
                </view>
              </view>
              <text class="hub-subtask-title">{{ sub.title }}</text>
              <view v-if="sub.status !== 'DONE'" class="hub-subtask-done-btn" @click="completeTask(sub.taskId)">
                <text class="hub-subtask-done-text">✓</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- ④ Risk Watch -->
      <view v-if="agentRisk" class="hub-section">
        <view class="hub-section-head">
          <text class="hub-section-title">风险监控</text>
          <view class="hub-risk-pill" :class="'risk-' + agentRisk.overallLevel.toLowerCase()">
            <text class="hub-risk-pill-text">{{ agentRisk.overallLevel }}</text>
          </view>
        </view>
        <text class="hub-risk-primary">{{ agentRisk.primaryRiskTitle }}</text>
        <text class="hub-risk-summary">{{ agentRisk.summary }}</text>
        <view class="hub-risks-list">
          <view v-for="r in agentRisk.risks" :key="r.code" class="hub-risk-row">
            <view class="hub-risk-row-left">
              <view class="hub-risk-dot" :class="'dot-' + r.level.toLowerCase()" />
              <text class="hub-risk-row-title">{{ r.title }}</text>
            </view>
            <view class="hub-risk-row-right">
              <text class="hub-risk-trend" :class="'trend-' + r.trend.toLowerCase()">{{ r.trend }}</text>
              <text class="hub-risk-score">{{ r.score }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- ⑤ Plan -->
      <view v-if="agentPlan" class="hub-section">
        <view class="hub-section-head">
          <text class="hub-section-title">长期计划</text>
          <text class="hub-plan-health" :class="'health-' + (agentPlan.planHealth || 'missing').toLowerCase()">
            {{ planHealthLabel }}
          </text>
        </view>
        <text class="hub-plan-role">{{ agentPlan.targetRole || '目标岗位未设置' }}</text>
        <text v-if="agentPlan.nextMilestoneTitle" class="hub-plan-milestone">
          {{ agentPlan.nextMilestoneHorizon }} · {{ agentPlan.nextMilestoneTitle }}
        </text>
        <view v-if="agentPlan.weeklyFocus?.length" class="hub-focus-list">
          <text v-for="f in agentPlan.weeklyFocus" :key="f" class="hub-focus-item">• {{ f }}</text>
        </view>
        <view
          v-if="!agentPlan.hasPlan || agentPlan.planHealth === 'NEEDS_REFRESH'"
          class="hub-plan-btn"
          @click="ensurePlan"
        >
          <text class="hub-plan-btn-text">{{ planBtnLabel }}</text>
        </view>
      </view>

      <!-- ⑥ Weekly Review -->
      <view v-if="weeklyReview" class="hub-section">
        <view class="hub-section-head">
          <text class="hub-section-title">上次周复盘</text>
          <text class="hub-review-date">{{ weeklyReviewDate }}</text>
        </view>
        <view class="hub-review-stats">
          <view class="hub-stat">
            <text class="hub-stat-val">{{ Math.round((reviewPayload?.completionRate7d || 0) * 100) }}%</text>
            <text class="hub-stat-label">任务完成率</text>
          </view>
          <view class="hub-stat">
            <text class="hub-stat-val">{{ reviewPayload?.currentStage || '-' }}</text>
            <text class="hub-stat-label">当时阶段</text>
          </view>
          <view class="hub-stat">
            <text class="hub-stat-val">{{ reviewPayload?.preferredDifficulty || '-' }}</text>
            <text class="hub-stat-label">偏好难度</text>
          </view>
        </view>
        <view v-if="reviewPayload?.highlights?.length" class="hub-review-highlights">
          <text v-for="h in reviewPayload.highlights" :key="h" class="hub-review-highlight">· {{ h }}</text>
        </view>
      </view>

      <!-- ⑦ Agent State stats -->
      <view v-if="agentState" class="hub-section hub-state-section">
        <text class="hub-section-title">本周数据</text>
        <view class="hub-state-stats">
          <view class="hub-stat">
            <text class="hub-stat-val">{{ Math.round((agentState.taskCompletionRate7d || 0) * 100) }}%</text>
            <text class="hub-stat-label">7日完成率</text>
          </view>
          <view class="hub-stat">
            <text class="hub-stat-val">{{ Math.round((agentState.taskDismissRate7d || 0) * 100) }}%</text>
            <text class="hub-stat-label">7日跳过率</text>
          </view>
          <view class="hub-stat">
            <text class="hub-stat-val">{{ agentState.preferredTaskDifficulty }}</text>
            <text class="hub-stat-label">偏好难度</text>
          </view>
        </view>
      </view>

      <view class="hub-bottom-spacer" />
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import {
  completeAgentTaskApi,
  decomposeTaskApi,
  dismissAgentTaskApi,
  ensureCareerAgentPlanApi,
  getAgentProfileApi,
  getAgentStateApi,
  getCareerAgentPlanSummaryApi,
  getCareerAgentRiskWatchApi,
  getCareerAgentTodayApi,
  getWeeklyReviewLatestApi,
  getTodayAgentTasksApi,
  type AgentEvent,
  type AgentState,
  type AgentTask,
  type AgentUserProfile,
  type CareerAgentPlanSummary,
  type CareerAgentRiskWatch,
  type CareerAgentToday,
} from '@/api/agent';

const agentProfile = ref<AgentUserProfile | null>(null);
const agentToday = ref<CareerAgentToday | null>(null);
const agentRisk = ref<CareerAgentRiskWatch | null>(null);
const agentPlan = ref<CareerAgentPlanSummary | null>(null);
const agentState = ref<AgentState | null>(null);
const weeklyReview = ref<AgentEvent | null>(null);
const openTasks = ref<AgentTask[]>([]);

const expandedTasks = reactive(new Set<number>());
const subtaskMap = reactive(new Map<number, AgentTask[]>());
const subtaskLoading = reactive(new Set<number>());

const levelLabel = computed(() => {
  const lvl = agentProfile.value?.personalizationLevel || 'LOW';
  if (lvl === 'HIGH') return '非常了解你';
  if (lvl === 'MEDIUM') return '正在了解你';
  return '刚刚开始';
});

const stageLabel = computed(() => {
  const s = agentToday.value?.stage || '';
  const map: Record<string, string> = {
    DIRECTION_DISCOVERY: '探索方向',
    TARGET_ROLE_SELECTION: '选定岗位',
    RESUME_BOOTSTRAP: '简历建档',
    RESUME_IMPROVEMENT: '简历优化',
    INTERVIEW_BOOTSTRAP: '面试启动',
    INTERVIEW_IMPROVEMENT: '面试提升',
    EXECUTION_RHYTHM: '执行节奏',
    CAREER_MOMENTUM: '持续冲刺',
  };
  return map[s] || s;
});

const planHealthLabel = computed(() => {
  const h = agentPlan.value?.planHealth || 'MISSING';
  if (h === 'ON_TRACK') return '进行中';
  if (h === 'NEEDS_REFRESH') return '需更新';
  return '未生成';
});

const planBtnLabel = computed(() =>
  agentPlan.value?.hasPlan ? '刷新计划' : '生成计划'
);

const weeklyReviewDate = computed(() => {
  const d = weeklyReview.value?.createdAt;
  return d ? d.substring(0, 10) : '';
});

const reviewPayload = computed(() => {
  try {
    return weeklyReview.value?.eventPayload
      ? JSON.parse(weeklyReview.value.eventPayload)
      : null;
  } catch { return null; }
});

const loadAll = async () => {
  const [profile, today, risk, plan, state, review, tasks] = await Promise.allSettled([
    getAgentProfileApi(),
    getCareerAgentTodayApi(),
    getCareerAgentRiskWatchApi(),
    getCareerAgentPlanSummaryApi(),
    getAgentStateApi(),
    getWeeklyReviewLatestApi(),
    getTodayAgentTasksApi(),
  ]);

  if (profile.status === 'fulfilled') agentProfile.value = profile.value;
  if (today.status === 'fulfilled') agentToday.value = today.value;
  if (risk.status === 'fulfilled') agentRisk.value = risk.value;
  if (plan.status === 'fulfilled') agentPlan.value = plan.value;
  if (state.status === 'fulfilled') agentState.value = state.value;
  if (review.status === 'fulfilled') weeklyReview.value = review.value;
  if (tasks.status === 'fulfilled') openTasks.value = tasks.value || [];
};

const completeTask = async (taskId: number) => {
  try {
    await completeAgentTaskApi(taskId);
    openTasks.value = openTasks.value.filter(t => t.taskId !== taskId);
    subtaskMap.delete(taskId);
    expandedTasks.delete(taskId);
    uni.showToast({ title: '已完成', icon: 'success' });
  } catch (e: any) {
    uni.showToast({ title: e?.message || '操作失败', icon: 'none' });
  }
};

const dismissTask = async (taskId: number) => {
  try {
    await dismissAgentTaskApi(taskId);
    openTasks.value = openTasks.value.filter(t => t.taskId !== taskId);
    subtaskMap.delete(taskId);
    expandedTasks.delete(taskId);
  } catch { /* ignore */ }
};

const expandTask = async (task: AgentTask) => {
  if (expandedTasks.has(task.taskId)) {
    expandedTasks.delete(task.taskId);
    return;
  }
  expandedTasks.add(task.taskId);
  if (!subtaskMap.has(task.taskId)) {
    subtaskLoading.add(task.taskId);
    try {
      const subs = await decomposeTaskApi(task.taskId);
      subtaskMap.set(task.taskId, subs || []);
    } catch {
      subtaskMap.set(task.taskId, []);
    } finally {
      subtaskLoading.delete(task.taskId);
    }
  }
};

const toggleTask = (taskId: number) => {
  if (expandedTasks.has(taskId)) expandedTasks.delete(taskId);
};

const ensurePlan = async () => {
  try {
    agentPlan.value = await ensureCareerAgentPlanApi();
    uni.showToast({ title: '计划已更新', icon: 'success' });
  } catch (e: any) {
    uni.showToast({ title: e?.message || '生成失败', icon: 'none' });
  }
};

const navTo = (path: string) => uni.navigateTo({ url: path });
const goBack = () => uni.navigateBack();

onMounted(loadAll);
</script>

<style scoped>
.hub-page { min-height: 100vh; background: #f5f7fa; }
.hub-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 56rpx 28rpx 20rpx; background: #fff;
}
.hub-back, .hub-refresh {
  width: 64rpx; height: 64rpx; display: flex; align-items: center;
  justify-content: center; border-radius: 50%; background: #f1f5f9;
}
.hub-back-icon, .hub-refresh-icon { font-size: 20px; color: #374151; }
.hub-title { font-size: 17px; font-weight: 700; color: #111827; }

.hub-scroll { height: calc(100vh - 120rpx); }

.hub-section {
  margin: 16rpx 24rpx 0;
  background: #fff; border-radius: 18rpx;
  padding: 24rpx;
}
.hub-state-section { margin-bottom: 0; }
.hub-bottom-spacer { height: 48rpx; }

.hub-section-head {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 16rpx;
}
.hub-section-title { font-size: 13px; font-weight: 700; color: #374151; }
.hub-section-arrow { font-size: 16px; color: #9ca3af; }
.hub-task-count { font-size: 12px; color: #6b7280; }
.hub-review-date { font-size: 11px; color: #9ca3af; }

/* profile */
.hub-pct-wrap { display: flex; align-items: center; gap: 12rpx; margin-bottom: 12rpx; }
.hub-pct-bar { flex: 1; height: 8rpx; background: #e5e7eb; border-radius: 4rpx; overflow: hidden; }
.hub-pct-fill { height: 100%; border-radius: 4rpx; transition: width .4s; }
.pct-low { background: #94a3b8; } .pct-medium { background: #38bdf8; } .pct-high { background: #34d399; }
.hub-pct-label { font-size: 11.5px; color: #6b7280; white-space: nowrap; }
.hub-missing-row { display: flex; flex-wrap: wrap; gap: 8rpx; }
.hub-missing-chip {
  border-radius: 999rpx; padding: 6rpx 14rpx;
  border: 1.5rpx solid #d1d5db;
}
.chip-high { border-color: #ef4444; background: #fef2f2; }
.chip-medium { border-color: #f59e0b; background: #fffbeb; }
.chip-low { border-color: #d1d5db; background: #f9fafb; }
.hub-missing-chip-text { font-size: 11px; color: #374151; }

/* today */
.hub-stage-pill { background: #eff6ff; border-radius: 999rpx; padding: 4rpx 14rpx; }
.hub-stage-text { font-size: 11px; color: #1d4ed8; font-weight: 600; }
.hub-headline { font-size: 15px; font-weight: 700; color: #111827; line-height: 1.4; margin-bottom: 6rpx; }
.hub-focus { font-size: 12.5px; color: #4b5563; margin-bottom: 14rpx; }
.hub-progress-bar { height: 6rpx; background: #e5e7eb; border-radius: 3rpx; overflow: hidden; margin-bottom: 6rpx; }
.hub-progress-fill { height: 100%; background: linear-gradient(90deg,#1d4ed8,#6366f1); border-radius: 3rpx; }
.hub-progress-text { font-size: 11px; color: #9ca3af; margin-bottom: 14rpx; }
.hub-actions { display: flex; flex-wrap: wrap; gap: 10rpx; }
.hub-action {
  border: 1.5rpx solid #d1d5db; border-radius: 999rpx;
  padding: 8rpx 18rpx; background: #f9fafb;
}
.hub-action-primary { border-color: #1d4ed8; background: #eff6ff; }
.hub-action-text { font-size: 12px; color: #374151; }
.hub-action-primary .hub-action-text { color: #1d4ed8; font-weight: 600; }

/* tasks */
.hub-empty { padding: 20rpx 0; }
.hub-empty-text { font-size: 12.5px; color: #9ca3af; }
.hub-task { border-top: 1rpx solid #f3f4f6; padding-top: 16rpx; margin-top: 12rpx; }
.hub-task:first-child { border-top: none; margin-top: 0; }
.hub-task-header { display: flex; gap: 12rpx; }
.hub-task-meta { flex: 1; min-width: 0; }
.hub-task-badges { display: flex; gap: 6rpx; margin-bottom: 6rpx; }
.hub-badge {
  border-radius: 6rpx; padding: 2rpx 10rpx;
}
.diff-easy { background: #d1fae5; } .diff-easy .hub-badge-text { color: #065f46; }
.diff-medium { background: #fef3c7; } .diff-medium .hub-badge-text { color: #92400e; }
.diff-hard { background: #fee2e2; } .diff-hard .hub-badge-text { color: #991b1b; }
.hub-badge-time { background: #ede9fe; }
.hub-badge-time .hub-badge-text { color: #5b21b6; }
.hub-badge-text { font-size: 10px; font-weight: 600; }
.hub-task-title { font-size: 13px; font-weight: 600; color: #111827; }
.hub-task-desc { font-size: 11.5px; color: #6b7280; margin-top: 4rpx; }
.hub-task-right { display: flex; flex-direction: column; align-items: flex-end; gap: 8rpx; }
.hub-task-actions { display: flex; gap: 8rpx; }
.hub-task-btn {
  border: 1.5rpx solid #d1d5db; border-radius: 999rpx;
  padding: 6rpx 14rpx; background: #f9fafb;
}
.hub-task-done { border-color: #1d4ed8; background: #eff6ff; }
.hub-task-btn-text { font-size: 11.5px; color: #374151; }
.hub-task-done .hub-task-btn-text { color: #1d4ed8; font-weight: 600; }
.hub-expand-btn { border-radius: 999rpx; padding: 4rpx 12rpx; background: #f1f5f9; }
.hub-expand-text { font-size: 11px; color: #6b7280; }

/* subtasks */
.hub-subtasks { margin-top: 12rpx; padding-left: 16rpx; border-left: 3rpx solid #e0e7ff; }
.hub-subtask-loading { padding: 8rpx 0; }
.hub-subtask-loading-text { font-size: 12px; color: #9ca3af; }
.hub-subtask {
  display: flex; align-items: flex-start; gap: 8rpx;
  padding: 10rpx 0; border-bottom: 1rpx solid #f3f4f6;
}
.hub-subtask:last-child { border-bottom: none; }
.hub-subtask-done { opacity: .45; }
.hub-subtask-badges { display: flex; gap: 4rpx; margin-bottom: 4rpx; flex-wrap: wrap; }
.hub-subtask-title { font-size: 12px; color: #374151; flex: 1; }
.hub-subtask-done-btn {
  width: 40rpx; height: 40rpx; border-radius: 50%;
  background: #d1fae5; display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.hub-subtask-done-text { font-size: 14px; color: #065f46; }

/* risk */
.hub-risk-pill { border-radius: 999rpx; padding: 4rpx 14rpx; }
.risk-high { background: #fee2e2; } .risk-high .hub-risk-pill-text { color: #991b1b; }
.risk-medium { background: #fef3c7; } .risk-medium .hub-risk-pill-text { color: #92400e; }
.risk-low { background: #d1fae5; } .risk-low .hub-risk-pill-text { color: #065f46; }
.hub-risk-pill-text { font-size: 11px; font-weight: 600; }
.hub-risk-primary { font-size: 14px; font-weight: 700; color: #111827; margin-bottom: 6rpx; }
.hub-risk-summary { font-size: 12px; color: #4b5563; margin-bottom: 14rpx; }
.hub-risks-list { display: flex; flex-direction: column; gap: 10rpx; }
.hub-risk-row { display: flex; align-items: center; justify-content: space-between; }
.hub-risk-row-left { display: flex; align-items: center; gap: 10rpx; }
.hub-risk-dot { width: 10rpx; height: 10rpx; border-radius: 50%; }
.dot-high { background: #ef4444; } .dot-medium { background: #f59e0b; } .dot-low { background: #10b981; }
.hub-risk-row-title { font-size: 12px; color: #374151; }
.hub-risk-row-right { display: flex; align-items: center; gap: 10rpx; }
.hub-risk-trend { font-size: 11px; }
.trend-rising { color: #ef4444; } .trend-stable { color: #6b7280; } .trend-decreasing { color: #10b981; }
.hub-risk-score { font-size: 11px; color: #9ca3af; }

/* plan */
.hub-plan-health { font-size: 12px; font-weight: 600; }
.health-on_track { color: #10b981; } .health-needs_refresh { color: #f59e0b; } .health-missing { color: #ef4444; }
.hub-plan-role { font-size: 14px; font-weight: 700; color: #111827; margin-bottom: 6rpx; }
.hub-plan-milestone { font-size: 12px; color: #6b7280; margin-bottom: 10rpx; }
.hub-focus-list { display: flex; flex-direction: column; gap: 6rpx; margin-bottom: 12rpx; }
.hub-focus-item { font-size: 12px; color: #4b5563; }
.hub-plan-btn {
  background: linear-gradient(135deg, #1d4ed8, #6366f1);
  border-radius: 999rpx; padding: 14rpx 0; text-align: center; margin-top: 8rpx;
}
.hub-plan-btn-text { font-size: 13px; font-weight: 700; color: #fff; }

/* weekly review */
.hub-review-stats, .hub-state-stats {
  display: flex; gap: 0; margin-bottom: 14rpx;
}
.hub-stat { flex: 1; text-align: center; }
.hub-stat-val { font-size: 16px; font-weight: 700; color: #111827; display: block; }
.hub-stat-label { font-size: 11px; color: #9ca3af; display: block; margin-top: 4rpx; }
.hub-review-highlights { display: flex; flex-direction: column; gap: 6rpx; }
.hub-review-highlight { font-size: 12px; color: #4b5563; }
</style>
