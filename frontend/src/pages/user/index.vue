<template>
  <view class="user-page" :class="{ 'is-dark': darkPref }">
    <view class="status-spacer" :style="{ height: topSafeHeight + 'px' }"></view>

    <!-- Header card: logged in -->
    <view class="header-card" v-if="isLoggedIn" @click="handleAvatarClick">
      <view class="header-avatar">
        <image 
          class="avatar-img"
          :src="userInfo.avatarUrl || '/static/default-avatar.png'" 
          mode="aspectFill"
        />
      </view>
      <view class="header-info">
        <text class="header-name">{{ userInfo.nickname || 'User' }}</text>
        <text class="header-school" v-if="userInfo.school">{{ userInfo.school }}</text>
        <text class="header-school edit-btn" v-else @click.stop="showProfileEdit = true">Tap to complete your profile</text>
      </view>
    </view>

    <!-- Header card: not logged in -->
    <view class="header-card header-guest" v-else>
      <text class="guest-title">Not Signed In</text>
      <text class="guest-desc">Sign in to sync assessments, resumes, and interview records</text>
      <button class="btn-login" @click="goLogin">Sign In</button>
    </view>

    <!-- Stats bar -->
    <view class="stats-bar" v-if="isLoggedIn">
      <view class="stat-item">
        <text class="stat-val">2</text>
        <text class="stat-label">Assessments</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-val">5</text>
        <text class="stat-label">Interviews</text>
      </view>
      <view class="stat-divider"></view>
      <view class="stat-item">
        <text class="stat-val">3</text>
        <text class="stat-label">Resumes</text>
      </view>
    </view>

    <!-- Menu group 1: My Assets -->
    <text class="group-label">My Assets</text>
    <view class="menu-card">
      <view class="menu-item" @click="goResumes">
        <text class="menu-icon">📄</text>
        <text class="menu-text">Resume Hub</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/assessment/index')">
        <text class="menu-icon">📝</text>
        <text class="menu-text">My Assessments</text>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="navTo('/pages/interview/history')">
        <text class="menu-icon">💼</text>
        <text class="menu-text">Interview Records</text>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- Menu group 2: Appearance & Accessibility -->
    <text class="group-label">Appearance & Accessibility</text>
    <view class="menu-card">
      <view class="menu-item">
        <text class="menu-icon">🌙</text>
        <text class="menu-text">Dark Mode</text>
        <switch class="dark-switch" :checked="darkPref" color="#2563eb" @change="toggleDark" />
      </view>
      <view class="menu-item">
        <text class="menu-icon">🔤</text>
        <text class="menu-text">Font Size</text>
        <view class="font-pills">
          <text 
            class="pill" 
            :class="{ 'pill-active': fontPref === 'compact' }"
            @click="setFont('compact')"
          >Sm</text>
          <text 
            class="pill" 
            :class="{ 'pill-active': fontPref === 'standard' }"
            @click="setFont('standard')"
          >Md</text>
          <text 
            class="pill" 
            :class="{ 'pill-active': fontPref === 'large' }"
            @click="setFont('large')"
          >Lg</text>
        </view>
      </view>
    </view>

    <!-- Spacer to push content to bottom -->
    <view class="flex-spacer"></view>

    <view class="bottom-section">
      <!-- Logout -->
      <button class="btn-logout" v-if="isLoggedIn" @click="handleLogout">Sign Out</button>
      <view class="bottom-safe"></view>
    </view>

    <!-- Profile Edit Modal -->
    <view class="modal-overlay" v-if="showProfileEdit" @click="showProfileEdit = false">
      <view class="modal-content bottom-sheet" @click.stop>
        <view class="sheet-handle"></view>
        <text class="modal-title">Complete Profile</text>
        
        <view class="form-group">
          <text class="field-label">School / University</text>
          <input class="field-input" v-model="editForm.school" placeholder="E.g. Stanford University" />
        </view>
        <view class="form-group">
          <text class="field-label">Major</text>
          <input class="field-input" v-model="editForm.major" placeholder="E.g. Computer Science" />
        </view>
        <view class="form-group">
          <text class="field-label">Graduation Year</text>
          <input class="field-input" v-model="editForm.gradYear" type="number" placeholder="E.g. 2026" />
        </view>

        <view class="modal-actions">
          <button class="btn-secondary" @click="showProfileEdit = false">Cancel</button>
          <button class="btn-primary" @click="saveProfile">Save</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { clearAuthState, LOGIN_PAGE } from '@/utils/auth';

const userInfo = ref({ nickname: '', avatarUrl: '', school: '', major: '', gradYear: '' });
const userId = ref('');
const darkPref = ref(false);
const fontPref = ref('standard');
const topSafeHeight = ref(44);

const showProfileEdit = ref(false);
const editForm = ref({ school: '', major: '', gradYear: '' });

const isLoggedIn = computed(() => !!userId.value);

const goLogin = () => {
  uni.reLaunch({ url: LOGIN_PAGE });
};

const goResumes = () => {
  uni.switchTab({ url: '/pages/resume/index' });
};

const navTo = (url: string) => {
  uni.navigateTo({ url });
};

const saveProfile = () => {
  userInfo.value.school = editForm.value.school;
  userInfo.value.major = editForm.value.major;
  userInfo.value.gradYear = editForm.value.gradYear;
  
  uni.setStorageSync('userInfo', userInfo.value);
  showProfileEdit.value = false;
  uni.showToast({ title: 'Profile saved', icon: 'success' });
};

const handleAvatarClick = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      userInfo.value.avatarUrl = res.tempFilePaths[0];
      uni.setStorageSync('userInfo', userInfo.value);
      uni.showToast({ title: 'Avatar updated', icon: 'success' });
    }
  });
};

const toggleDark = (e: any) => {
  darkPref.value = e.detail.value;
  uni.setStorageSync('app_pref_dark', darkPref.value ? '1' : '0');
  uni.showToast({ title: darkPref.value ? 'Dark mode enabled' : 'Dark mode disabled', icon: 'none' });
};

const setFont = (size: string) => {
  fontPref.value = size;
  uni.setStorageSync('app_pref_font', size);
  uni.showToast({ title: 'Saved', icon: 'none' });
};

const handleLogout = () => {
  uni.showModal({
    title: 'Sign Out',
    content: 'Are you sure you want to sign out? Your local data will be cleared.',
    confirmColor: '#ef4444',
    success: (res) => {
      if (res.confirm) {
        clearAuthState();
        userId.value = '';
        userInfo.value = { nickname: '', avatarUrl: '', school: '', major: '', gradYear: '' };
        uni.showToast({ title: 'Signed out', icon: 'success' });
        setTimeout(() => {
          uni.reLaunch({ url: LOGIN_PAGE });
        }, 400);
      }
    },
  });
};

onMounted(() => {
  userId.value = uni.getStorageSync('userId') || '';
  const info = uni.getStorageSync('userInfo');
  if (info) {
    userInfo.value = { ...userInfo.value, ...info };
    editForm.value.school = userInfo.value.school || '';
    editForm.value.major = userInfo.value.major || '';
    editForm.value.gradYear = userInfo.value.gradYear || '';
  }

  darkPref.value = uni.getStorageSync('app_pref_dark') === '1';
  fontPref.value = uni.getStorageSync('app_pref_font') || 'standard';

  const sysInfo = uni.getSystemInfoSync();
  const menuButton = uni.getMenuButtonBoundingClientRect?.();
  if (menuButton && menuButton.top) {
    topSafeHeight.value = menuButton.top;
  } else {
    topSafeHeight.value = (sysInfo.statusBarHeight || 44) + 8;
  }
});
</script>

<style scoped>
.user-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--page-ios-gray);
  padding: 0 20px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 140px); /* 预留给底部登出按钮的空间，避免小屏幕重叠 */
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
  position: relative; /* 为底部绝对定位做参照 */
}

.bottom-section {
  position: absolute;
  bottom: 0;
  left: 20px;
  right: 20px;
}

.status-spacer { width: 100%; }

/* Header card */
.header-card {
  background: linear-gradient(135deg, #2563eb 0%, #1e40af 50%, #1e3a8a 100%);
  border-radius: 20px; padding: 24px 20px; margin: 8px 0 16px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.25);
}

.header-guest {
  flex-direction: column; align-items: flex-start; gap: 8px;
}

.guest-title { font-size: 20px; font-weight: 700; color: #ffffff; }

.guest-desc { font-size: 13px; color: rgba(255, 255, 255, 0.7); margin-bottom: 4px; }

.btn-login {
  background: rgba(255, 255, 255, 0.2);
  color: #ffffff; border: 1px solid rgba(255, 255, 255, 0.3);
  font-size: 14px; font-weight: 600; border-radius: 12px;
  padding: 0 20px; height: 36px; line-height: 36px;
}

.header-avatar { flex-shrink: 0; }

.avatar-img {
  width: 56px; height: 56px; border-radius: 28px;
  border: 2px solid rgba(255, 255, 255, 0.4);
}

.header-info { flex: 1; }

.header-name {
  font-size: 20px; font-weight: 700; color: #ffffff;
  display: block; margin-bottom: 4px;
}

.header-school { font-size: 13px; color: rgba(255, 255, 255, 0.9); }

.edit-btn { background: rgba(255,255,255,0.2); padding: 4px 10px; border-radius: 8px; display: inline-block; margin-top: 4px; }

/* Stats */
.stats-bar {
  display: flex; align-items: center;
  background: #ffffff; border-radius: 16px;
  padding: 16px 0; margin-bottom: 24px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
}

.stat-item {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; gap: 4px;
}

.stat-val { font-size: 20px; font-weight: 800; color: #0f172a; }

.stat-label { font-size: 11px; color: #94a3b8; font-weight: 500; }

.stat-divider { width: 1px; height: 24px; background: var(--border-color); }

/* Menu */
.group-label {
  font-size: var(--font-caption);
  font-weight: 500;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  display: block;
  margin-bottom: 8px;
  padding-left: 4px;
}

.menu-card {
  background: #ffffff; border-radius: 16px;
  overflow: hidden; margin-bottom: 24px;
  border: 1px solid var(--border-color);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
}

.menu-item {
  display: flex; align-items: center; padding: 15px 16px;
  position: relative;
}

.menu-item:not(:last-child)::after {
  content: ''; position: absolute;
  bottom: 0; left: 48px; right: 0;
  height: 1px; background: var(--border-color);
}

.menu-icon { font-size: 20px; margin-right: 14px; }

.menu-text {
  flex: 1;
  font-size: 15px;
  color: #1e293b;
  font-weight: 500;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-arrow { font-size: 20px; color: #c7c7cc; flex-shrink: 0; }

.dark-switch { transform: scale(0.85); }

.font-pills { display: flex; gap: 6px; }

.pill {
  padding: 4px 14px; border-radius: 8px;
  font-size: 13px; font-weight: 500; color: #64748b;
  background: #f1f5f9;
}

.pill-active { background: #2563eb; color: #ffffff; }

/* Logout */
.btn-logout {
  width: 100%; height: 48px; background: #ffffff;
  color: #ef4444; font-size: 15px; font-weight: 600;
  border-radius: 16px; line-height: 48px; border: 1px solid var(--border-color);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.05);
}

.btn-logout:active { background: #fef2f2; }

.bottom-safe {
  height: calc(var(--tab-bar-height, 50px) + 16px);
  flex-shrink: 0;
}

/* Modals */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.4); z-index: 1000;
  display: flex; align-items: flex-end;
}

.modal-content.bottom-sheet {
  width: 100%; background: #ffffff;
  border-radius: 24px 24px 0 0; padding: 16px 20px 32px;
  box-sizing: border-box;
  border-top: 1px solid var(--border-color);
}

.sheet-handle {
  width: 36px; height: 5px; border-radius: 3px; background: #e2e8f0;
  margin: 0 auto 16px;
}

.modal-title { font-size: 18px; font-weight: 700; color: #0f172a; display: block; margin-bottom: 24px; text-align: center; }

.form-group { margin-bottom: 16px; }
.field-label { font-size: 14px; font-weight: 600; color: #334155; margin-bottom: 8px; display: block; }
.field-input { width: 100%; height: 48px; border: 1px solid #e2e8f0; border-radius: 12px; padding: 0 16px; font-size: 15px; box-sizing: border-box; background: #f8fafc; }

.modal-actions { display: flex; gap: 12px; margin-top: 32px; }
.btn-secondary { flex: 1; height: 48px; background: #f1f5f9; color: #475569; font-weight: 600; border-radius: 12px; border: none; line-height: 48px; }
.btn-primary { flex: 2; height: 48px; background: #2563eb; color: #fff; font-weight: 600; border-radius: 12px; border: none; line-height: 48px; }

/* Dark mode */
.is-dark { background: #0f172a; }

.is-dark .stats-bar,
.is-dark .menu-card,
.is-dark .btn-logout { background: #1e293b; box-shadow: none; }

.is-dark .stat-val,
.is-dark .menu-text { color: #f8fafc; }

.is-dark .group-label,
.is-dark .stat-label { color: #64748b; }

.is-dark .btn-logout { color: #f87171; }

.is-dark .pill { background: #334155; color: #94a3b8; }

.is-dark .pill-active { background: #2563eb; color: #ffffff; }

.is-dark .modal-content { background: #1e293b; }
.is-dark .sheet-handle { background: #334155; }
.is-dark .modal-title { color: #f8fafc; }
.is-dark .field-label { color: #e2e8f0; }
.is-dark .field-input { background: #0f172a; border-color: #334155; color: #f8fafc; }
.is-dark .btn-secondary { background: #334155; color: #e2e8f0; }
</style>
