<template>
  <view class="fb-page" :class="[themeClass, fontClass]">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <!-- Nav bar -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">‹</text>
        <text class="nav-back-text">{{ t('common.back') }}</text>
      </view>
      <text class="nav-title">{{ t('feedback.title') }}</text>
      <view class="nav-placeholder"></view>
    </view>

    <scroll-view class="content" scroll-y>
      <view class="hero-block">
        <view class="hero-icon">💬</view>
        <text class="hero-title">{{ t('feedback.heroTitle') }}</text>
        <text class="hero-desc">{{ t('feedback.heroDesc') }}</text>
      </view>

      <!-- Category picker -->
      <text class="section-label">{{ t('feedback.categoryLabel') }}</text>
      <view class="category-row">
        <view
          v-for="cat in CATEGORIES"
          :key="cat.value"
          class="cat-chip"
          :class="{ 'cat-active': form.category === cat.value }"
          @click="form.category = cat.value"
        >
          <text class="cat-emoji">{{ cat.emoji }}</text>
          <text class="cat-label">{{ cat.label }}</text>
        </view>
      </view>

      <!-- Content textarea -->
      <text class="section-label">{{ t('feedback.contentLabel') }} <text class="req">*</text></text>
      <textarea
        class="fb-textarea"
        v-model="form.content"
        :placeholder="t('feedback.contentPlaceholder')"
        placeholder-class="ph"
        maxlength="2000"
        auto-height
      />
      <text class="char-count">{{ form.content.length }} / 2000</text>

      <!-- Contact (optional) -->
      <text class="section-label">{{ t('feedback.contactLabel') }}</text>
      <input
        class="fb-input"
        v-model="form.contact"
        :placeholder="t('feedback.contactPlaceholder')"
        placeholder-class="ph"
      />

      <button class="btn-submit" :disabled="submitting || !form.content.trim()" @click="doSubmit">
        <text v-if="submitting">{{ t('feedback.submitting') }}</text>
        <text v-else>{{ t('feedback.submit') }}</text>
      </button>

      <view class="bottom-pad"></view>
    </scroll-view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { onShow } from '@dcloudio/uni-app';
import { getTopSafeHeight } from '@/utils/safeArea';
import { submitFeedbackApi, type FeedbackCategory } from '@/api/feedback';
import { useTheme } from '@/utils/theme';

const { t } = useI18n();
const { themeClass, fontClass, refresh: refreshTheme } = useTheme();
const topSafeHeight = ref(44);
const submitting = ref(false);

const CATEGORIES = computed<{ value: FeedbackCategory; emoji: string; label: string }[]>(() => [
  { value: 'FUNCTION_BUG',    emoji: '🐛', label: 'Bug' },
  { value: 'SUGGESTION',      emoji: '💡', label: t('feedback.catSuggestion') },
  { value: 'CONTENT_REPORT',  emoji: '🚩', label: t('feedback.catReport') },
  { value: 'OTHER',           emoji: '💬', label: t('feedback.catOther') },
]);

const form = ref({
  category: 'SUGGESTION' as FeedbackCategory,
  content: '',
  contact: '',
});

const goBack = () => uni.navigateBack();

const doSubmit = async () => {
  const text = form.value.content.trim();
  if (!text) {
    uni.showToast({ title: t('feedback.contentRequired'), icon: 'none' });
    return;
  }
  submitting.value = true;
  try {
    await submitFeedbackApi({
      category: form.value.category,
      content: text,
      contact: form.value.contact.trim() || undefined,
    });
    uni.showToast({ title: t('feedback.submitSuccess'), icon: 'success' });
    setTimeout(() => uni.navigateBack(), 1500);
  } catch (e: any) {
    uni.showToast({ title: e?.message || t('common.failed'), icon: 'none' });
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  refreshTheme();
  topSafeHeight.value = getTopSafeHeight();
});

onShow(() => {
  refreshTheme();
});
</script>

<style scoped>
.fb-page {
  min-height: 100vh;
  background: var(--page-ios-gray, #f2f2f7);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  display: flex;
  flex-direction: column;
}

.status-spacer { width: 100%; flex-shrink: 0; }

/* ---- Nav ---- */
.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 12px;
  background: rgba(242, 242, 247, 0.92);
  backdrop-filter: blur(20px);
  border-bottom: 0.5px solid rgba(60, 60, 67, 0.1);
}
.nav-back { display: flex; align-items: center; min-width: 60px; }
.nav-back-icon { font-size: 24px; color: #2563eb; margin-right: 2px; line-height: 1; }
.nav-back-text { font-size: 16px; color: #2563eb; }
.nav-title { font-size: 17px; font-weight: 700; color: #1c1c1e; }
.nav-placeholder { min-width: 60px; }

/* ---- Scroll content ---- */
.content { flex: 1; padding: 0 16px; }

/* ---- Hero ---- */
.hero-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 28px 0 22px;
  text-align: center;
}
.hero-icon { font-size: 44px; margin-bottom: 12px; }
.hero-title { font-size: 20px; font-weight: 800; color: #1c1c1e; margin-bottom: 8px; }
.hero-desc { font-size: 13px; color: #6b7280; line-height: 1.6; max-width: 280px; }

/* ---- Labels ---- */
.section-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin: 18px 0 8px;
}
.req { color: #ef4444; }

/* ---- Category chips ---- */
.category-row {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.cat-chip {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 14px;
  background: #ffffff;
  border: 1.5px solid #e5e7eb;
  border-radius: 20px;
  transition: all 0.15s;
}
.cat-chip:active { opacity: 0.75; }
.cat-active {
  background: #eff6ff;
  border-color: #2563eb;
}
.cat-emoji { font-size: 15px; }
.cat-label { font-size: 13px; font-weight: 600; color: #374151; }
.cat-active .cat-label { color: #2563eb; }

/* ---- Textarea ---- */
.fb-textarea {
  width: 100%;
  min-height: 120px;
  padding: 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  font-size: 15px;
  color: #1c1c1e;
  line-height: 1.55;
  box-sizing: border-box;
}
.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

/* ---- Input ---- */
.fb-input {
  width: 100%;
  height: 46px;
  padding: 0 14px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  font-size: 15px;
  color: #1c1c1e;
  box-sizing: border-box;
}

.ph { color: #9ca3af; }

/* ---- Submit button ---- */
.btn-submit {
  width: 100%;
  height: 50px;
  background: #2563eb;
  color: #ffffff;
  border-radius: 14px;
  font-size: 16px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 24px;
  border: none;
}
.btn-submit[disabled] { background: #93c5fd; }

.bottom-pad { height: 40px; }

/* ---- Dark mode ---- */
.is-dark { background: #0f172a; }
.is-dark .nav-bar { background: rgba(15,23,42,0.88); border-color: #334155; }
.is-dark .nav-title { color: #f8fafc; }
.is-dark .hero-title { color: #f8fafc; }
.is-dark .fb-textarea,
.is-dark .fb-input { background: #1e293b; border-color: #334155; color: #f8fafc; }
.is-dark .cat-chip { background: #1e293b; border-color: #334155; }
.is-dark .cat-label { color: #cbd5e1; }
</style>
