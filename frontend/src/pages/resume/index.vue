<template>
  <view class="resume-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <view class="page-header">
      <text class="page-title">Resume Hub</text>
      <text class="page-subtitle">Manage your career assets</text>
    </view>

    <!-- Section header (compact, replaces oversized hero) -->
    <view class="section-bar">
      <view class="section-titles">
        <text class="section-title">My Resumes</text>
        <text class="section-sub">{{ resumeList.length }} {{ resumeList.length === 1 ? 'file' : 'files' }} · stored privately</text>
      </view>
      <view class="section-action" @click="handleUploadClick">
        <text class="section-action-text">+ Add</text>
      </view>
    </view>

    <!-- Skeleton while the list is loading from the backend -->
    <view class="skeleton-list" v-if="isLoading && resumeList.length === 0">
      <view class="skel-card" v-for="i in 3" :key="i">
        <view class="skel-square"></view>
        <view class="skel-lines">
          <view class="skel-line skel-w70"></view>
          <view class="skel-line skel-w40"></view>
        </view>
      </view>
    </view>

    <view class="resume-list" v-else-if="resumeList.length > 0">
      <view class="resume-card" v-for="(item, idx) in resumeList" :key="idx">
        <view class="rc-icon-wrap">
          <view class="rc-icon" :class="'rc-icon-' + (idx % 2)">
            <text class="rc-icon-text">PDF</text>
          </view>
        </view>
        <view class="rc-body">
          <text class="rc-name">{{ item.name }}</text>
          <view class="rc-meta-row">
            <text class="rc-time">{{ item.date }}</text>
            <view class="rc-badge" :class="'badge-' + item.status">
              <text class="rc-badge-text">{{ item.statusLabel }}</text>
            </view>
          </view>
        </view>
        <view class="rc-actions">
          <view class="rc-action-btn" @click="handlePreview(item)">
            <text class="rc-action-text">Preview</text>
          </view>
          <view class="rc-more" @click="handleMore(idx)">
            <text class="rc-more-dots">···</text>
          </view>
        </view>
      </view>

      <!-- Add new resume (compact) -->
      <view class="add-card" @click="handleUploadClick">
        <view class="add-icon">
          <text class="add-plus">+</text>
        </view>
        <view class="add-info">
          <text class="add-title">New Resume</text>
          <text class="add-desc">Create from template or upload PDF</text>
        </view>
      </view>
    </view>

    <view class="empty-state" v-else-if="!isLoading">
      <text class="empty-icon">📄</text>
      <text class="empty-title">No resumes yet</text>
      <text class="empty-desc">Create one from the guided template or upload an existing PDF to start using diagnosis and interview features.</text>
      <view class="add-card" @click="handleUploadClick">
        <view class="add-icon">
          <text class="add-plus">+</text>
        </view>
        <view class="add-info">
          <text class="add-title">Add your first resume</text>
          <text class="add-desc">Template creation and PDF upload are both supported</text>
        </view>
      </view>
    </view>

    <view class="bottom-safe"></view>

    <!-- Action sheet -->
    <view class="sheet-mask" v-if="showSheet" @click="closeSheet"></view>
    <view class="sheet" :class="{ 'sheet-open': showSheet }">
      <view class="sheet-title-bar">
        <text class="sheet-title">Choose an option</text>
      </view>
      <view class="sheet-option" @click="selectAction('template')">
        <text class="sheet-option-icon">📝</text>
        <text class="sheet-option-text">Fill from Template</text>
      </view>
      <view class="sheet-option" @click="selectAction('upload')">
        <text class="sheet-option-icon">📎</text>
        <text class="sheet-option-text">Upload PDF Resume</text>
      </view>
      <view class="sheet-cancel" @click="closeSheet">
        <text class="sheet-cancel-text">Cancel</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getTopSafeHeight } from '@/utils/safeArea';
import {
  getUserResumesApi,
  createResumeApi,
  deleteResumeApi,
  updateResumeApi,
  uploadResumeFile,
  type Resume,
} from '@/api/resume';

interface ResumeItem {
  resumeId?: number;
  name: string;
  date: string;
  status: 'recent' | 'normal';
  statusLabel: string;
  fileUrl?: string;
}

const resumeList = ref<ResumeItem[]>([]);
const isLoading = ref(false);

/**
 * Render an absolute timestamp (e.g. "2026-04-30 01:00:21") as a friendly
 * relative label. We deliberately keep this self-contained -- it falls back
 * to a short date for anything older than a week. (HCI: match real world,
 * minimise cognitive load.)
 */
const formatRelative = (ts?: string): string => {
  if (!ts) return '';
  const date = new Date(ts.replace(' ', 'T'));
  if (isNaN(date.getTime())) return '';
  const diffMs = Date.now() - date.getTime();
  const sec = Math.max(0, Math.floor(diffMs / 1000));
  if (sec < 60) return 'Just now';
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min}m ago`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `${hr}h ago`;
  const day = Math.floor(hr / 24);
  if (day === 1) return 'Yesterday';
  if (day < 7) return `${day}d ago`;
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
};

const loadResumes = async () => {
  const userId = uni.getStorageSync('userId');
  const numericId = Number(userId);
  if (!userId || isNaN(numericId) || numericId <= 0) {
    // Guest or not logged in — show empty list gracefully
    resumeList.value = [];
    return;
  }
  isLoading.value = true;
  try {
    const resumes = await getUserResumesApi(numericId);
    resumeList.value = (resumes || []).map((r: Resume) => ({
      resumeId: r.resumeId,
      name: r.title || r.fileUrl?.split('/').pop() || 'Untitled.pdf',
      date: formatRelative(r.updatedAt || r.createdAt),
      status: 'recent' as const,
      statusLabel: r.status || 'Active',
      fileUrl: r.fileUrl,
    }));
  } catch (e: any) {
    // Surface the actual error instead of silently showing an empty hub.
    // Common causes: stale token after a rebuild (signature mismatch),
    // userId in storage doesn't match the JWT subject, or backend down.
    resumeList.value = [];
    const msg = e?.message || 'Failed to load resumes';
    if (msg.toLowerCase().includes('forbidden') || msg.includes('未登录') || msg.toLowerCase().includes('unauthor')) {
      uni.showToast({ title: 'Session expired, please log in again', icon: 'none' });
      uni.removeStorageSync('token');
      uni.removeStorageSync('userId');
    } else {
      uni.showToast({ title: msg, icon: 'none' });
    }
  } finally {
    isLoading.value = false;
  }
};

const showSheet = ref(false);
const topSafeHeight = ref(88);
const darkPref = ref(false);

const handleUploadClick = () => {
  showSheet.value = true;
};

const closeSheet = () => {
  showSheet.value = false;
};

const selectAction = (type: string) => {
  closeSheet();
  if (type === 'upload') {
    const userId = Number(uni.getStorageSync('userId'));
    if (!userId || isNaN(userId) || userId <= 0) {
      uni.showToast({ title: 'Please log in first', icon: 'none' });
      return;
    }
    uni.chooseMessageFile({
      count: 1,
      type: 'file',
      success: async (fileRes) => {
        const file = fileRes.tempFiles?.[0];
        const fileName = file?.name || 'Untitled_Resume.pdf';
        const filePath = file?.path;
        if (!filePath) {
          uni.showToast({ title: 'No file selected', icon: 'none' });
          return;
        }
        if (!/\.pdf$/i.test(fileName)) {
          uni.showToast({ title: 'Please select a PDF file', icon: 'none' });
          return;
        }

        uni.showLoading({ title: 'Uploading...' });
        try {
          const fileUrl = await uploadResumeFile(filePath, 'resumes');
          const created = await createResumeApi({
            userId,
            title: fileName.replace(/\.pdf$/i, ''),
            targetJob: '',
            fileUrl,
            status: 'ACTIVE',
          });
          uni.hideLoading();
          resumeList.value.unshift({
            resumeId: created.resumeId,
            name: created.title || fileName,
            date: 'Just now',
            status: 'recent',
            statusLabel: 'Active',
            fileUrl: created.fileUrl,
          });
          uni.showToast({ title: 'Upload successful', icon: 'success' });
        } catch (e: any) {
          uni.hideLoading();
          uni.showToast({ title: e?.message || 'Upload failed', icon: 'none' });
        }
      },
      fail: () => {
        uni.showToast({ title: 'No file selected', icon: 'none' });
      }
    });
  } else if (type === 'template') {
    uni.navigateTo({ url: '/pages/resume/template' });
  }
};

const handlePreview = (item: ResumeItem) => {
  if (!item.resumeId) {
    uni.showToast({ title: 'File not available yet', icon: 'none' });
    return;
  }
  // Use authenticated backend proxy instead of the raw OSS URL.
  // This avoids the WeChat mini-program domain whitelist requirement
  // and enforces owner-only access on the server side.
  const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
  const token = uni.getStorageSync('token');
  uni.showLoading({ title: 'Opening...' });
  uni.downloadFile({
    url: `${BASE_URL}/api/resumes/${item.resumeId}/download`,
    header: token ? { Authorization: `Bearer ${token}` } : {},
    success: (dl) => {
      if (dl.statusCode === 200) {
        uni.openDocument({
          filePath: dl.tempFilePath,
          fileType: 'pdf',
          showMenu: true,
          success: () => uni.hideLoading(),
          fail: () => {
            uni.hideLoading();
            // openDocument is unreliable in the WeChat DevTools simulator;
            // works on real devices. Hint the user instead of being mysterious.
            uni.showModal({
              title: 'Cannot preview here',
              content: 'PDF preview is not supported in the DevTools simulator. Please use the "Preview" button at the top of WeChat DevTools to scan and test on a real phone.',
              showCancel: false,
              confirmText: 'OK',
            });
          },
        });
      } else {
        uni.hideLoading();
        uni.showToast({ title: `Download failed (${dl.statusCode})`, icon: 'none' });
      }
    },
    fail: () => {
      uni.hideLoading();
      uni.showToast({ title: 'Network error', icon: 'none' });
    },
  });
};

const handleMore = (idx: number) => {
  if (!resumeList.value[idx]) return;

  uni.showActionSheet({
    itemList: ['Rename', 'Share', 'Delete'],
    success: (res) => {
      if (res.tapIndex === 0) {
        const item = resumeList.value[idx];
        const oldName = item.name;
        const dotIndex = oldName.lastIndexOf('.pdf');
        const base = dotIndex > 0 ? oldName.slice(0, dotIndex) : oldName;
        uni.showModal({
          title: 'Rename Resume',
          editable: true,
          placeholderText: 'New name',
          content: base,
          success: async (mr) => {
            if (!mr.confirm) return;
            const newBase = (mr.content || '').trim();
            if (!newBase) return;
            const newName = newBase.endsWith('.pdf') ? newBase : `${newBase}.pdf`;
            if (item.resumeId) {
              try {
                await updateResumeApi(item.resumeId, { title: newBase });
              } catch (e: any) {
                uni.showToast({ title: e?.message || 'Rename failed', icon: 'none' });
                return;
              }
            }
            item.name = newName;
            uni.showToast({ title: 'Renamed', icon: 'success' });
          },
        });
      } else if (res.tapIndex === 1) {
        const item = resumeList.value[idx];
        if (!item.fileUrl) {
          uni.showToast({ title: 'File URL not available', icon: 'none' });
          return;
        }
        uni.setClipboardData({
          data: item.fileUrl,
          success: () => uni.showToast({ title: 'Share link copied', icon: 'none' }),
          fail: () => uni.showToast({ title: 'Copy failed, please retry', icon: 'none' })
        });
      } else if (res.tapIndex === 2) {
        uni.showModal({
          title: 'Confirm Delete',
          content: 'Are you sure you want to delete this resume?',
          success: async (r) => {
            if (!r.confirm) return;
            const item = resumeList.value[idx];
            if (item?.resumeId) {
              try {
                await deleteResumeApi(item.resumeId);
              } catch (e: any) {
                uni.showToast({ title: e?.message || 'Delete failed', icon: 'none' });
                return;
              }
            }
            resumeList.value.splice(idx, 1);
            uni.showToast({ title: 'Deleted', icon: 'success' });
          },
        });
      }
    },
  });
};

onMounted(() => {
  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  topSafeHeight.value = getTopSafeHeight();
});

// Tab pages are kept alive across navigation; onShow re-fires every time
// the page becomes visible (including after login -> switchTab back).
onShow(() => {
  loadResumes();
});
</script>

<style scoped>
.resume-page {
  min-height: 100vh;
  background: var(--page-ios-gray);
  padding: 0 20px;
  padding-bottom: env(safe-area-inset-bottom, 0px);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.status-spacer { width: 100%; }

.page-header { padding: 8px 0 6px; }

.page-title {
  display: block;
  font-size: var(--font-title);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.35px;
}

.page-subtitle {
  display: block;
  font-size: var(--font-caption);
  color: var(--text-tertiary);
  margin-top: 4px;
  line-height: var(--line-height-caption);
}

.section-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 16px 0 14px;
}
.section-titles { display: flex; flex-direction: column; gap: 2px; }
.section-title {
  font-size: var(--font-section);
  font-weight: 700;
  color: var(--text-primary);
}
.section-sub { font-size: 12px; color: var(--text-tertiary); }

.section-action {
  padding: 6px 12px;
  background: #eff6ff;
  border-radius: 999px;
  border: 1px solid #dbeafe;
}
.section-action:active { background: #dbeafe; }
.section-action-text { font-size: 13px; color: #2563eb; font-weight: 600; }

/* Skeleton placeholders shown during initial load.
   HCI: visibility of system status -- a shimmering layout previews
   what's coming, which feels much faster than a centered spinner. */
.skeleton-list { display: flex; flex-direction: column; gap: 10px; }
.skel-card {
  background: #ffffff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex; align-items: center; gap: 14px;
}
.skel-square {
  width: 44px; height: 44px; border-radius: 12px;
  background: linear-gradient(90deg, #eef2f7 0%, #f7fafc 50%, #eef2f7 100%);
  background-size: 200% 100%;
  animation: skel-shimmer 1.4s infinite;
  flex-shrink: 0;
}
.skel-lines { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.skel-line {
  height: 12px; border-radius: 6px;
  background: linear-gradient(90deg, #eef2f7 0%, #f7fafc 50%, #eef2f7 100%);
  background-size: 200% 100%;
  animation: skel-shimmer 1.4s infinite;
}
.skel-w40 { width: 40%; }
.skel-w70 { width: 70%; }
@keyframes skel-shimmer {
  0%   { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

.resume-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 28px;
}

.resume-card {
  display: flex;
  align-items: center;
  background: #ffffff;
  border-radius: var(--radius-md);
  padding: 16px;
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.rc-icon-wrap { margin-right: 14px; flex-shrink: 0; }

.rc-icon {
  width: 44px;
  height: 52px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.rc-icon-0 { background: linear-gradient(135deg, #dbeafe, #bfdbfe); }
.rc-icon-1 { background: linear-gradient(135deg, #e0e7ff, #c7d2fe); }

.rc-icon-text {
  font-size: 11px;
  font-weight: 800;
  color: #2563eb;
  letter-spacing: 0.5px;
}

.rc-body { flex: 1; min-width: 0; }

.rc-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.rc-meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.rc-time { font-size: 12px; color: #94a3b8; }

.rc-badge { padding: 2px 8px; border-radius: 6px; flex-shrink: 0; }

.badge-recent { background: #dbeafe; }
.badge-recent .rc-badge-text { color: #2563eb; }

.badge-normal { background: #eef2ff; }
.badge-normal .rc-badge-text { color: #4f46e5; }

.rc-badge-text { font-size: 11px; font-weight: 600; }

.rc-actions { display: flex; align-items: center; gap: 8px; margin-left: 10px; }

.rc-action-btn { padding: 6px 14px; border-radius: 10px; background: #eff6ff; }

.rc-action-btn:active { background: #dbeafe; }

.rc-action-text { font-size: 13px; font-weight: 500; color: #2563eb; }

.rc-more { padding: 6px 4px; }

.rc-more-dots { font-size: 16px; color: #94a3b8; font-weight: 700; letter-spacing: 1px; }

.add-card {
  display: flex;
  align-items: center;
  border: 1.5px dashed #93c5fd;
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  gap: 14px;
  box-shadow: var(--shadow-xs);
  transition: all 0.2s;
}

.add-card:active {
  border-color: #93c5fd;
  background: #eff6ff;
  transform: scale(0.98);
}

.add-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.add-plus { font-size: 22px; color: #2563eb; font-weight: 300; }

.add-info { display: flex; flex-direction: column; }

.add-title { font-size: 15px; font-weight: 600; color: #1e3a8a; margin-bottom: 3px; }

.add-desc { font-size: 12px; color: #64748b; }

.bottom-safe {
  height: calc(var(--tab-bar-height, 50px) + 20px);
}

.empty-state {
  padding: 24px 0 28px;
}

.empty-icon {
  display: block;
  font-size: 40px;
  margin-bottom: 12px;
}

.empty-title {
  display: block;
  font-size: 17px;
  font-weight: 700;
  color: var(--text-primary);
}

.empty-desc {
  display: block;
  margin: 8px 0 18px;
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-secondary);
}

/* ---- Action sheet ---- */
.sheet-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.35);
  z-index: 998;
}

.sheet {
  position: fixed;
  left: 12px; right: 12px;
  bottom: -320px;
  z-index: 999;
  transition: bottom 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.sheet-open { bottom: 30px; }

.sheet-title-bar {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  text-align: center;
  padding: 16px;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  border-bottom: 0.5px solid rgba(60, 60, 67, 0.1);
}

.sheet-title { font-size: 13px; color: #8e8e93; font-weight: 500; }

.sheet-option {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px;
  border-bottom: 0.5px solid rgba(60, 60, 67, 0.1);
}

.sheet-option:last-of-type {
  border-bottom-left-radius: 16px;
  border-bottom-right-radius: 16px;
  border-bottom: none;
}

.sheet-option-icon { font-size: 18px; }

.sheet-option-text { font-size: 17px; color: #007aff; }

.sheet-cancel {
  background: #ffffff;
  text-align: center;
  padding: 16px;
  border-radius: 16px;
  margin-top: 10px;
}

.sheet-cancel-text { font-size: 17px; font-weight: 600; color: #007aff; }

.is-dark { background: #0f172a; }

.is-dark .page-title,
.is-dark .section-title,
.is-dark .rc-name,
.is-dark .add-title { color: #f8fafc; }

.is-dark .page-subtitle,
.is-dark .section-count,
.is-dark .rc-time,
.is-dark .add-desc { color: #94a3b8; }

.is-dark .resume-card,
.is-dark .add-card,
.is-dark .sheet-option,
.is-dark .sheet-title-bar,
.is-dark .sheet-cancel { background: #1e293b; border-color: #334155; }
</style>
