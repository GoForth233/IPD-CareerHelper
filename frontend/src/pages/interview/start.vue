<template>
  <view class="start-page" :class="{ 'is-dark': darkPref }">
    <view class="header">
      <text class="title">Start Mock Interview</text>
      <text class="subtitle">Pick a target role and difficulty. The AI interviewer will adapt every question to your choices.</text>
    </view>

    <view class="form-card">
      <view class="form-item">
        <text class="label">Mode</text>
        <view class="mode-grid">
          <view
            v-for="item in modes"
            :key="item.value"
            :class="['mode-card', selectedMode === item.value ? 'active' : '']"
            @click="selectedMode = item.value as 'voice' | 'text'"
          >
            <view class="mode-head">
              <text class="mode-icon">{{ item.icon }}</text>
              <text class="mode-name">{{ item.label }}</text>
              <text v-if="item.badge" class="mode-badge">{{ item.badge }}</text>
            </view>
            <text class="mode-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>

      <view class="form-item">
        <view class="label-row">
          <text class="label">Target Position</text>
          <text v-if="prefillSource" class="label-hint">{{ prefillSource }}</text>
        </view>
        <picker :range="positions" :value="selectedPositionIndex" @change="onPositionChange">
          <view class="picker-box" :class="{ 'picker-filled': selectedPosition }">
            <text class="picker-val" :class="{ 'has-val': selectedPosition }">{{ selectedPosition || 'Tap to choose a role' }}</text>
            <text class="picker-arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-item form-item-last">
        <text class="label">Difficulty</text>
        <view class="difficulty-grid">
          <view
            v-for="item in difficulties"
            :key="item.value"
            :class="['diff-card', selectedDifficulty === item.value ? 'active' : '']"
            @click="selectedDifficulty = item.value"
          >
            <text class="diff-name">{{ item.label }}</text>
            <text class="diff-desc">{{ item.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- What to expect: fills the bottom half with useful preview context
         instead of empty white space -->
    <view class="expect-card">
      <text class="expect-title">What to expect</text>
      <view class="expect-row">
        <text class="expect-icon">⏱</text>
        <view class="expect-body">
          <text class="expect-h">~10 minutes</text>
          <text class="expect-p">A short, focused session. End any time with the button at the bottom.</text>
        </view>
      </view>
      <view class="expect-row">
        <text class="expect-icon">💬</text>
        <view class="expect-body">
          <text class="expect-h">Adaptive questions</text>
          <text class="expect-p">The AI follows up based on your answers — give specific, structured replies.</text>
        </view>
      </view>
      <view class="expect-row">
        <text class="expect-icon">📊</text>
        <view class="expect-body">
          <text class="expect-h">AI-scored report</text>
          <text class="expect-p">When you finish, you'll get a 5-dimension breakdown with strengths and improvements.</text>
        </view>
      </view>
    </view>

    <!-- Sticky CTA pinned to the bottom thumb zone.
         We use <view> instead of <button> because mp-weixin's native <button>
         element ships with very heavy default styles (white background,
         pseudo-borders) that constantly fight scoped CSS. <view> renders
         exactly what we tell it to. -->
    <view class="sticky-cta">
      <view
        class="btn-primary"
        :class="{ 'btn-disabled': !selectedPosition || loading }"
        @click="startInterview"
      >
        <text class="btn-primary-label" v-if="loading">Starting...</text>
        <text class="btn-primary-label" v-else>{{ selectedPosition ? (selectedMode === 'voice' ? 'Start Voice Interview' : 'Start Text Interview') : 'Choose a position to begin' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { startInterviewApi } from '@/api/interview';
import { getProfileSnapshotApi, updatePreferencesApi } from '@/api/user';
import { LOGIN_PAGE } from '@/utils/auth';

const positions = ref<string[]>(['Java Backend Engineer', 'Frontend Engineer', 'Full Stack Engineer', 'Product Manager', 'Data Analyst']);
const selectedPosition = ref('');
const selectedPositionIndex = ref(0);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');

/**
 * Tells the user *why* a position came pre-selected — e.g. "From your INFP
 * assessment" or "From your last interview" — so the prefill doesn't feel
 * magic / wrong. Empty string when nothing is prefilled.
 */
const prefillSource = ref('');

const difficulties = [
  { label: 'Easy',   value: 'Easy',   desc: 'Conversational warm-up' },
  { label: 'Normal', value: 'Normal', desc: 'Standard interview pace' },
  { label: 'Hard',   value: 'Hard',   desc: 'Senior-level deep dive' },
];
const selectedDifficulty = ref('Normal');

// Voice is the recommended path — it's the differentiated experience and
// what real interviews feel like. We keep Text as the fallback for users
// without a quiet space or with poor mic permissions.
const modes = [
  { label: 'Voice',  value: 'voice', icon: '🎤', badge: 'Recommended', desc: 'Talk to the AI interviewer with your camera on' },
  { label: 'Text',   value: 'text',  icon: '⌨',  badge: '',            desc: 'Classic chat — best in noisy environments' },
];
// Persist the user's last choice so returning users land on their preference.
const selectedMode = ref<'voice' | 'text'>(
  (uni.getStorageSync('interview_mode') as 'voice' | 'text') || 'voice'
);
const loading = ref(false);

const onPositionChange = (e: any) => {
  const idx = Number(e.detail.value);
  selectedPositionIndex.value = idx;
  selectedPosition.value = positions.value[idx];
  // Manual choice -- the prefill hint no longer applies, so clear it.
  prefillSource.value = '';
};

/**
 * If the picker doesn't already include the candidate role, prepend it so
 * we can preselect it without losing the standard options. Returns the
 * index where the role lives in the picker after this call.
 */
const ensurePositionExists = (role: string): number => {
  const existing = positions.value.findIndex((p) => p.toLowerCase() === role.toLowerCase());
  if (existing >= 0) return existing;
  positions.value = [role, ...positions.value];
  return 0;
};

/**
 * Pull cross-tool portrait from the backend on entry so the user lands on
 * a form that's already filled in with their last role / mode preference.
 * Order of precedence:
 *   1. Explicit `?suggestedRole=` query (just clicked a CTA elsewhere)
 *   2. Snapshot preferences.targetRole (set by the assessment CTA earlier)
 *   3. Snapshot interview.positionName (last interview's position)
 * If none of the above we leave the picker empty so the CTA gates on a manual choice.
 */
const applyPrefill = async () => {
  const pages = getCurrentPages();
  const opts = (pages[pages.length - 1] as any).options || {};
  const fromQuery = opts.suggestedRole ? decodeURIComponent(opts.suggestedRole) : '';

  if (fromQuery) {
    const idx = ensurePositionExists(fromQuery);
    selectedPositionIndex.value = idx;
    selectedPosition.value = positions.value[idx];
    prefillSource.value = 'From your assessment';
    return;
  }

  try {
    const snap = await getProfileSnapshotApi();
    const targetRole = snap?.preferences?.targetRole;
    const lastPos = snap?.interview?.positionName;
    const lastMode = snap?.preferences?.interviewMode;

    if (targetRole) {
      const idx = ensurePositionExists(targetRole);
      selectedPositionIndex.value = idx;
      selectedPosition.value = positions.value[idx];
      prefillSource.value = snap?.assessment ? 'From your assessment' : 'Recently chosen';
    } else if (lastPos) {
      const idx = ensurePositionExists(lastPos);
      selectedPositionIndex.value = idx;
      selectedPosition.value = positions.value[idx];
      prefillSource.value = 'From your last interview';
    }

    // The mode prefilled on storage takes priority -- only fall back to the
    // server snapshot when local storage was empty (fresh device).
    const localMode = uni.getStorageSync('interview_mode') as 'voice' | 'text' | '';
    if (!localMode && (lastMode === 'voice' || lastMode === 'text')) {
      selectedMode.value = lastMode;
    }
  } catch {
    // Snapshot is best-effort -- if it blows up we just leave the form empty.
  }
};

onMounted(() => {
  applyPrefill();
});

const startInterview = async () => {
  if (!selectedPosition.value) {
    uni.showToast({ title: 'Please select a position', icon: 'none' });
    return;
  }

  const userId = uni.getStorageSync('userId');
  const numericId = Number(userId);
  if (!userId || isNaN(numericId) || numericId <= 0) {
    uni.showToast({ title: 'Please sign in first', icon: 'none' });
    setTimeout(() => {
      uni.reLaunch({ url: LOGIN_PAGE });
    }, 600);
    return;
  }

  loading.value = true;
  try {
    // userId is resolved server-side from the JWT; don't send it in the body.
    // Persist the modality on the interview row so History can resume in the
    // *same* mode the candidate originally chose.
    const interview = await startInterviewApi({
      positionName: selectedPosition.value,
      difficulty: selectedDifficulty.value,
      mode: selectedMode.value === 'voice' ? 'VOICE' : 'TEXT',
    });

    uni.setStorageSync('interview_mode', selectedMode.value);
    // Mirror the choice into the cross-tool snapshot so the assistant
    // and the next session both know the user's preferred mode. Best-effort.
    updatePreferencesApi({
      targetRole: selectedPosition.value,
      interviewMode: selectedMode.value,
    }).catch(() => { /* snapshot writes are non-blocking */ });

    uni.showToast({ title: 'Interview started', icon: 'success' });
    const targetPath = selectedMode.value === 'voice'
      ? `/pages/interview/room?interviewId=${interview.interviewId}`
      : `/pages/interview/chat?interviewId=${interview.interviewId}`;
    setTimeout(() => {
      uni.navigateTo({ url: targetPath });
    }, 800);
  } catch (error: any) {
    console.error(error);
    uni.showToast({ title: error?.message || 'Failed to start', icon: 'none' });
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.start-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray);
  /* Reserve space for the sticky CTA so content never sits under it */
  padding: 24px 20px calc(120px + env(safe-area-inset-bottom));
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.header { margin-bottom: 24px; padding: 0 4px; }

.title {
  font-size: var(--font-hero);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.5px;
  display: block;
  margin-bottom: 8px;
}

.subtitle {
  font-size: var(--font-caption);
  color: var(--text-tertiary);
  display: block;
  line-height: var(--line-height-caption);
}

.form-card {
  background: #ffffff;
  border-radius: var(--radius-lg);
  padding: 22px 20px 8px;
  box-shadow: var(--shadow-card);
  margin-bottom: 18px;
}

.form-item { margin-bottom: 22px; }
.form-item-last { margin-bottom: 14px; }

.label {
  font-size: 15px; font-weight: 700; color: #0f172a;
  margin-bottom: 12px; display: block;
}

/* Inline hint next to a label, e.g. "From your assessment" — explains the
   provenance of an auto-filled value so the prefill feels intentional rather
   than mysterious. */
.label-row {
  display: flex; align-items: baseline; justify-content: space-between;
  margin-bottom: 12px;
}
.label-row .label { margin-bottom: 0; }
.label-hint {
  font-size: 11px; font-weight: 600;
  color: #4f46e5;
  background: #eef2ff;
  padding: 3px 8px;
  border-radius: 999px;
  letter-spacing: 0.02em;
}

/* Mode picker — same affordance language as the difficulty cards so the
   form reads as a single coherent group. Voice is the hero so we reach
   for the brand gradient when active. */
.mode-grid { display: flex; gap: 10px; }
.mode-card {
  flex: 1;
  display: flex; flex-direction: column; gap: 8px;
  padding: 14px;
  border: 2px solid #e2e8f0; border-radius: 14px;
  background: #ffffff;
  transition: all 0.15s;
  min-height: 96px;
}
.mode-head { display: flex; align-items: center; gap: 8px; }
.mode-icon { font-size: 20px; line-height: 1; }
.mode-name { font-size: 14px; font-weight: 700; color: #1e293b; }
.mode-badge {
  margin-left: auto;
  font-size: 9px; font-weight: 700;
  letter-spacing: 0.04em;
  background: #ecfdf5; color: #047857;
  padding: 2px 6px; border-radius: 999px;
  text-transform: uppercase;
}
.mode-desc { font-size: 11.5px; color: #64748b; line-height: 1.45; }

.mode-card.active {
  border-color: #2563eb;
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}
.mode-card.active .mode-name { color: #ffffff; }
.mode-card.active .mode-desc { color: rgba(255, 255, 255, 0.85); }
.mode-card.active .mode-badge { background: rgba(255, 255, 255, 0.22); color: #ffffff; }
.mode-card:active { transform: scale(0.97); }

/* Position picker -- clearer empty/filled affordance */
.picker-box {
  display: flex; align-items: center; justify-content: space-between;
  border: 1.5px solid #e2e8f0; border-radius: 14px;
  padding: 14px 16px; background: #f8fafc;
  min-height: 52px; box-sizing: border-box;
  transition: border-color 0.15s, background 0.15s;
}
.picker-box:active { border-color: #2563eb; background: #eff6ff; }
.picker-filled { border-color: #c7d2fe; background: #ffffff; }

.picker-val { font-size: 15px; color: #94a3b8; flex: 1; }
.has-val { color: #0f172a; font-weight: 600; }

.picker-arrow { font-size: 20px; color: #c7c7cc; line-height: 1; flex-shrink: 0; }

/* Difficulty cards now show name AND description so the choice is informed
   (HCI: recognition over recall, match real-world expectations) */
.difficulty-grid { display: flex; gap: 10px; }

.diff-card {
  flex: 1;
  display: flex; flex-direction: column; align-items: flex-start;
  gap: 4px;
  padding: 12px;
  border: 2px solid #e2e8f0; border-radius: 14px;
  background: #ffffff;
  transition: all 0.15s;
  min-height: 72px;
}

.diff-name { font-size: 14px; font-weight: 700; color: #1e293b; }
.diff-desc { font-size: 11px; color: #64748b; line-height: 1.35; }

.diff-card.active {
  border-color: #2563eb;
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 100%);
}
.diff-card.active .diff-name { color: #ffffff; }
.diff-card.active .diff-desc { color: rgba(255, 255, 255, 0.85); }

.diff-card:active { transform: scale(0.97); }

/* What-to-expect card */
.expect-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex; flex-direction: column; gap: 16px;
}

.expect-title {
  font-size: 12px; font-weight: 700;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.expect-row { display: flex; gap: 14px; align-items: flex-start; }
.expect-icon {
  width: 32px; height: 32px; flex-shrink: 0;
  background: #eff6ff; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 16px;
}
.expect-body { flex: 1; display: flex; flex-direction: column; gap: 2px; }
.expect-h { font-size: 14px; font-weight: 700; color: #0f172a; }
.expect-p { font-size: 12px; color: #64748b; line-height: 1.45; }

/* Sticky CTA -- pinned to the bottom thumb zone (HCI: Fitts's law) */
.sticky-cta {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  padding: 12px 20px calc(12px + env(safe-area-inset-bottom));
  background: rgba(245, 245, 247, 0.94);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 0.5px solid rgba(60, 60, 67, 0.1);
  z-index: 10;
}

/* Custom-rendered button (just a styled <view>) -- guaranteed colour fidelity
   on mp-weixin where native <button> defaults are aggressive.
   We hardcode colours here rather than using CSS vars to guarantee contrast
   is never accidentally broken by a variable resolution issue. */
.btn-primary {
  width: 100%;
  background: #2563eb;
  border-radius: 16px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.36);
  transition: background 0.15s, opacity 0.15s;
}

.btn-primary-label {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

.btn-primary:active { background: #1d4ed8; }

/* Disabled = no position selected. Use a mid-grey background with dark text
   so "Choose a position to begin" is legible — white-on-lightgrey fails WCAG. */
.btn-disabled {
  background: #e2e8f0;
  box-shadow: none;
}
.btn-disabled .btn-primary-label {
  color: #64748b;
  font-weight: 600;
}

/* Dark mode */
.is-dark { background-color: #0f172a; }

.is-dark .title,
.is-dark .label,
.is-dark .has-val,
.is-dark .expect-h,
.is-dark .diff-name { color: #f8fafc; }

.is-dark .form-card,
.is-dark .expect-card { background: #1e293b; border-color: #334155; box-shadow: none; }

.is-dark .picker-box { border-color: #334155; background: #0f172a; }
.is-dark .diff-card:not(.active) { border-color: #334155; background: #0f172a; }
.is-dark .mode-card:not(.active) { border-color: #334155; background: #0f172a; }
.is-dark .mode-name { color: #f8fafc; }

.is-dark .picker-val { color: #64748b; }

.is-dark .expect-icon { background: rgba(37, 99, 235, 0.2); }

.is-dark .sticky-cta {
  background: rgba(15, 23, 42, 0.92);
  border-color: #334155;
}

.is-dark .btn-disabled {
  background: #1e293b;
}
.is-dark .btn-disabled .btn-primary-label {
  color: #94a3b8;
}
</style>
