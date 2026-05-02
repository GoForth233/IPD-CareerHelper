<template>
  <view class="memory-page">
    <view class="status-spacer" :style="{ height: topSafe + 'px' }"></view>

    <!-- Custom nav bar -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="nav-back-icon">‹</text>
        <text class="nav-back-text">Back</text>
      </view>
      <text class="nav-title">AI Memory</text>
      <view class="nav-right-placeholder"></view>
    </view>

    <!-- Header -->
    <view class="page-header">
      <text class="page-title">AI Memory</text>
      <text class="page-subtitle">Facts Career Loop has learned about you from your conversations. Tap a fact to delete it.</text>
    </view>

    <!-- Loading skeleton -->
    <view v-if="loading" class="skeleton-wrap">
      <view v-for="i in 5" :key="i" class="skeleton-card"></view>
    </view>

    <!-- Empty state -->
    <view v-else-if="facts.length === 0" class="empty-state">
      <text class="empty-icon">🧠</text>
      <text class="empty-title">No memories yet</text>
      <text class="empty-desc">Chat with the AI assistant to let it learn about your career goals, skills, and preferences.</text>
    </view>

    <!-- Facts grouped by category -->
    <view v-else>
      <view v-for="(group, cat) in grouped" :key="cat" class="group-section">
        <view class="group-header">
          <text class="group-icon">{{ categoryIcon(cat) }}</text>
          <text class="group-label">{{ categoryLabel(cat) }}</text>
          <text class="group-count">{{ group.length }}</text>
        </view>
        <view class="facts-card">
          <view
            v-for="(fact, idx) in group"
            :key="fact.id"
            class="fact-row"
            :class="{ 'fact-row-last': idx === group.length - 1 }"
          >
            <view class="fact-body">
              <text class="fact-key">{{ formatKey(fact.factKey) }}</text>
              <text class="fact-value">{{ fact.factValue }}</text>
            </view>
            <view class="fact-conf-badge" :class="confClass(fact.confidence)">
              <text class="fact-conf-text">{{ confLabel(fact.confidence) }}</text>
            </view>
            <view class="fact-delete-btn" @click="confirmDelete(fact)">
              <text class="fact-delete-icon">✕</text>
            </view>
          </view>
        </view>
      </view>

      <!-- Clear all -->
      <view class="clear-all-row">
        <view class="btn-clear-all" @click="confirmClearAll">
          <text class="btn-clear-all-text">Clear All Memories</text>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { getTopSafeHeight } from '@/utils/safeArea';
import request from '@/utils/request';

interface UserFact {
  id: number;
  category: string;
  factKey: string;
  factValue: string;
  confidence: number;
  source: string;
  updatedAt: string;
}

const topSafe = ref(44);
const loading = ref(true);
const facts = ref<UserFact[]>([]);

onMounted(async () => {
  topSafe.value = getTopSafeHeight();
  await loadFacts();
});

const loadFacts = async () => {
  loading.value = true;
  try {
    const res = await request<UserFact[]>({ url: '/facts/me', method: 'GET' });
    facts.value = Array.isArray(res) ? res : [];
  } catch (e: any) {
    uni.showToast({ title: e?.message || 'Failed to load memories', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const grouped = computed(() => {
  const map: Record<string, UserFact[]> = {};
  for (const f of facts.value) {
    const cat = f.category || 'GENERAL';
    if (!map[cat]) map[cat] = [];
    map[cat].push(f);
  }
  return map;
});

const categoryLabel = (cat: string) => {
  const labels: Record<string, string> = {
    PERSONALITY: 'Personality',
    CAREER_GOAL: 'Career Goals',
    SKILL: 'Skills',
    PREFERENCE: 'Preferences',
    EXPERIENCE: 'Experience',
    GENERAL: 'General',
  };
  return labels[cat] || cat;
};

const categoryIcon = (cat: string) => {
  const icons: Record<string, string> = {
    PERSONALITY: '🧬',
    CAREER_GOAL: '🎯',
    SKILL: '⚡',
    PREFERENCE: '💡',
    EXPERIENCE: '🗂️',
    GENERAL: '📌',
  };
  return icons[cat] || '📌';
};

const formatKey = (key: string) =>
  key.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());

const confLabel = (conf: number) => {
  if (conf >= 0.9) return 'High';
  if (conf >= 0.6) return 'Med';
  return 'Low';
};

const confClass = (conf: number) => {
  if (conf >= 0.9) return 'conf-high';
  if (conf >= 0.6) return 'conf-med';
  return 'conf-low';
};

const confirmDelete = (fact: UserFact) => {
  uni.showModal({
    title: 'Delete Memory',
    content: `Remove "${formatKey(fact.factKey)}: ${fact.factValue}"?`,
    confirmText: 'Delete',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        await request({ url: `/facts/me/${fact.id}`, method: 'DELETE' });
        facts.value = facts.value.filter((f) => f.id !== fact.id);
        uni.showToast({ title: 'Deleted', icon: 'success' });
      } catch (e: any) {
        uni.showToast({ title: e?.message || 'Failed', icon: 'none' });
      }
    },
  });
};

const confirmClearAll = () => {
  uni.showModal({
    title: 'Clear All Memories',
    content: 'This will permanently delete all AI-extracted facts about you. This cannot be undone.',
    confirmText: 'Clear All',
    confirmColor: '#ef4444',
    success: async (res) => {
      if (!res.confirm) return;
      try {
        const ids = facts.value.map((f) => f.id);
        await Promise.all(ids.map((id) => request({ url: `/facts/me/${id}`, method: 'DELETE' })));
        facts.value = [];
        uni.showToast({ title: 'All memories cleared', icon: 'success' });
      } catch {
        uni.showToast({ title: 'Some items could not be deleted', icon: 'none' });
      }
    },
  });
};

const goBack = () => uni.navigateBack();
</script>

<style scoped>
.memory-page {
  min-height: 100vh;
  background: var(--page-ios-gray, #f5f5f7);
  padding: 0 16px 0;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.status-spacer { width: 100%; }

/* Nav bar */
.nav-bar {
  display: flex; align-items: center; justify-content: space-between;
  height: 44px; padding: 0 4px;
}

.nav-back {
  display: flex; align-items: center; gap: 2px;
  padding: 8px; min-width: 64px;
}

.nav-back-icon { font-size: 22px; color: #2563eb; line-height: 1; }
.nav-back-text { font-size: 16px; color: #2563eb; font-weight: 500; }
.nav-title { font-size: 17px; font-weight: 700; color: #0f172a; }
.nav-right-placeholder { min-width: 64px; }

/* Header */
.page-header { padding: 8px 4px 20px; }
.page-title { display: block; font-size: 26px; font-weight: 800; color: var(--text-primary, #0f172a); }
.page-subtitle { display: block; margin-top: 6px; font-size: 13px; line-height: 1.55; color: var(--text-secondary, #64748b); }

/* Skeleton */
.skeleton-wrap { display: flex; flex-direction: column; gap: 12px; margin-top: 8px; }
.skeleton-card { height: 72px; border-radius: 14px; background: #e2e8f0; animation: pulse 1.5s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }

/* Empty */
.empty-state { padding: 60px 20px 40px; text-align: center; }
.empty-icon { font-size: 52px; display: block; margin-bottom: 12px; }
.empty-title { display: block; font-size: 17px; font-weight: 700; color: #1e293b; margin-bottom: 8px; }
.empty-desc { display: block; font-size: 13px; line-height: 1.55; color: #64748b; max-width: 280px; margin: 0 auto; }

/* Group */
.group-section { margin-bottom: 20px; }
.group-header {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 8px; padding: 0 4px;
}
.group-icon { font-size: 16px; }
.group-label { font-size: 12px; font-weight: 700; color: #475569; text-transform: uppercase; letter-spacing: 0.5px; flex: 1; }
.group-count {
  font-size: 11px; color: #94a3b8; background: #e2e8f0;
  border-radius: 10px; padding: 2px 7px; font-weight: 600;
}

/* Facts card */
.facts-card {
  background: #ffffff; border-radius: 16px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 2px 8px rgba(15,23,42,0.05);
  overflow: hidden;
}

.fact-row {
  display: flex; align-items: center; gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.fact-row-last { border-bottom: none; }
.fact-row:active { background: #f8fafc; }

.fact-body { flex: 1; min-width: 0; }
.fact-key { display: block; font-size: 12px; font-weight: 600; color: #64748b; margin-bottom: 2px; }
.fact-value { display: block; font-size: 14px; font-weight: 500; color: #1e293b; word-break: break-word; }

/* Confidence badges */
.fact-conf-badge {
  flex-shrink: 0; border-radius: 8px; padding: 3px 8px;
}
.conf-high { background: #dcfce7; }
.conf-med { background: #fef9c3; }
.conf-low { background: #fee2e2; }
.fact-conf-text { font-size: 11px; font-weight: 700; }
.conf-high .fact-conf-text { color: #166534; }
.conf-med .fact-conf-text { color: #854d0e; }
.conf-low .fact-conf-text { color: #991b1b; }

/* Delete button */
.fact-delete-btn {
  flex-shrink: 0; width: 28px; height: 28px;
  border-radius: 50%; background: #f1f5f9;
  display: flex; align-items: center; justify-content: center;
}
.fact-delete-btn:active { background: #fee2e2; }
.fact-delete-icon { font-size: 12px; color: #94a3b8; font-weight: 700; }

/* Clear all */
.clear-all-row { padding: 8px 0 16px; display: flex; justify-content: center; }
.btn-clear-all {
  padding: 10px 24px; border-radius: 12px;
  border: 1px solid #fca5a5;
}
.btn-clear-all-text { font-size: 13px; color: #ef4444; font-weight: 600; }

.bottom-safe { height: 80px; }
</style>
