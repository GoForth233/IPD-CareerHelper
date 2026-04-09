<template>
  <view class="login-page" :class="{ 'is-dark': darkPref }">
    <view class="status-bar-spacer"></view>

    <!-- Top hero area -->
    <view class="hero">
      <text class="hero-kicker">CAREER DEVELOPMENT PLATFORM</text>
      <text class="hero-title">Start Your Career Loop</text>
      <text class="hero-sub">Sign in to sync your assessments, resumes, and interview data</text>
    </view>

    <!-- Main form card -->
    <view class="form-sheet">
      <!-- Segment tabs -->
      <view class="segment-wrap">
        <view class="segment-bar">
          <view class="seg-item" :class="{ 'seg-active': mode === 'login' }" @click="mode = 'login'">
            <text>Sign In</text>
          </view>
          <view class="seg-item" :class="{ 'seg-active': mode === 'register' }" @click="mode = 'register'">
            <text>Sign Up</text>
          </view>
        </view>
      </view>

      <!-- Nickname (register only) -->
      <view class="field" v-if="mode === 'register'">
        <text class="field-label">Nickname</text>
        <input
          class="field-input"
          v-model="nickname"
          placeholder="Choose a display name"
          placeholder-class="ph"
        />
      </view>

      <!-- Phone / Account -->
      <view class="field">
        <text class="field-label">Phone / Account</text>
        <input
          class="field-input"
          v-model="account"
          placeholder="Enter your phone number or account ID"
          placeholder-class="ph"
        />
      </view>

      <!-- Password -->
      <view class="field">
        <text class="field-label">Password</text>
        <input
          class="field-input"
          v-model="password"
          type="password"
          placeholder="Enter your password"
          placeholder-class="ph"
        />
      </view>

      <!-- Forgot password (login only) -->
      <view class="forgot-row" v-if="mode === 'login'">
        <text class="forgot-link" @click="handleForgot">Forgot password?</text>
      </view>

      <!-- Main button -->
      <button class="btn-primary" :loading="loading" @click="handleSubmit">
        {{ mode === 'login' ? 'Sign In' : 'Create Account' }}
      </button>

      <!-- Divider -->
      <view class="divider-row">
        <view class="divider-line"></view>
        <text class="divider-text">or continue with</text>
        <view class="divider-line"></view>
      </view>

      <!-- Social buttons -->
      <view class="social-row">
        <button class="btn-wechat" @click="wxLogin">
          <text class="wx-icon">💬</text>
          <text class="wx-text">WeChat</text>
        </button>
        <button class="btn-guest" @click="guestLogin">
          <text class="guest-text">Guest Mode (No Login)</text>
        </button>
      </view>

      <!-- Agreement -->
      <view class="agreement-row">
        <view class="checkbox-wrap" @click="agreed = !agreed">
          <view class="checkbox" :class="{ 'checked': agreed }">
            <text v-if="agreed" class="check-mark">✓</text>
          </view>
        </view>
        <text class="agreement-text">
          I agree to the <text class="link">Terms of Service</text> and <text class="link">Privacy Policy</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const mode = ref<'login' | 'register'>('login');
const nickname = ref('');
const account = ref('');
const password = ref('');
const agreed = ref(false);
const loading = ref(false);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');

const handleForgot = () => {
  uni.showModal({
    title: 'Password Reset',
    content: 'Please contact support@careerloop.ai or use WeChat login to recover your account.',
    showCancel: false,
    confirmText: 'Got it'
  });
};

const handleSubmit = async () => {
  if (!agreed.value) {
    uni.showToast({ title: 'Please agree to the Terms of Service', icon: 'none' });
    return;
  }
  if (!account.value || !password.value) {
    uni.showToast({ title: 'Please fill in all required fields', icon: 'none' });
    return;
  }
  if (mode.value === 'register' && !nickname.value) {
    uni.showToast({ title: 'Please enter a nickname', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    if (mode.value === 'register') {
      const { registerApi } = await import('@/api/user');
      const res = await registerApi({
        nickname: nickname.value,
        identityType: 'PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      uni.setStorageSync('userId', res.userId);
      uni.setStorageSync('userInfo', { nickname: nickname.value, avatarUrl: '' });
      uni.showToast({ title: 'Account created!', icon: 'success' });
    } else {
      const { loginApi } = await import('@/api/user');
      const res = await loginApi({
        identityType: 'PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      // Merge with any existing complete user info returned from API if needed, 
      // but the mock might just return a User object directly
      uni.setStorageSync('userId', res.userId);
      uni.setStorageSync('userInfo', res || { nickname: account.value, avatarUrl: '' });
      uni.showToast({ title: 'Welcome back!', icon: 'success' });
    }
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' });
    }, 800);
  } catch (e: any) {
    uni.showToast({ title: e?.message || 'Request failed. Please try again.', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const wxLogin = () => {
  if (!agreed.value) {
    uni.showToast({ title: 'Please agree to the Terms of Service', icon: 'none' });
    return;
  }
  uni.showLoading({ title: 'Authorizing via WeChat...' });
  setTimeout(() => {
    uni.hideLoading();
    uni.setStorageSync('userId', 'wx_guest_001');
    uni.setStorageSync('userInfo', { nickname: 'WeChat User', avatarUrl: '' });
    uni.showToast({ title: 'Welcome!', icon: 'success' });
    setTimeout(() => {
      uni.switchTab({ url: '/pages/home/index' });
    }, 800);
  }, 1500);
};

const guestLogin = () => {
  if (!agreed.value) {
    uni.showToast({ title: 'Please agree to the Terms of Service', icon: 'none' });
    return;
  }
  uni.setStorageSync('userId', 'guest_temp_001');
  uni.setStorageSync('userInfo', { nickname: 'Guest', avatarUrl: '' });
  uni.showToast({ title: 'Welcome, Guest!', icon: 'success' });
  setTimeout(() => {
    uni.switchTab({ url: '/pages/home/index' });
  }, 800);
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #2563eb 0%, #1e40af 35%, #f5f5f7 35.1%);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
  box-sizing: border-box;
}

.status-bar-spacer {
  height: calc(var(--status-bar-height, 44px) + 8px);
}

/* Hero */
.hero {
  padding: 0 28px 28px;
  display: flex; flex-direction: column;
}

.hero-kicker {
  font-size: 11px; font-weight: 600; color: rgba(255, 255, 255, 0.6);
  letter-spacing: 2px; margin-bottom: 8px;
}

.hero-title {
  font-size: 26px; font-weight: 800; color: #ffffff;
  letter-spacing: -0.5px; margin-bottom: 8px;
}

.hero-sub { font-size: 13px; color: rgba(255, 255, 255, 0.78); line-height: 1.55; max-width: 95%; }

/* Form sheet */
.form-sheet {
  background: #ffffff; border-radius: 24px 24px 0 0;
  padding: 28px 24px calc(32px + env(safe-area-inset-bottom));
  min-height: calc(100vh - 200px);
}

/* Segment */
.segment-wrap { margin-bottom: 24px; }

.segment-bar {
  display: flex; background: #f1f5f9; border-radius: 12px;
  padding: 3px; gap: 2px;
}

.seg-item {
  flex: 1; text-align: center; height: 38px; line-height: 38px;
  border-radius: 10px; font-size: 15px; font-weight: 500; color: #64748b;
  transition: all 0.25s;
}

.seg-active {
  background: #ffffff; color: #0f172a; font-weight: 600;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

/* Fields */
.field { margin-bottom: 18px; }

.field-label {
  font-size: 14px; font-weight: 600; color: #334155;
  display: block; margin-bottom: 8px;
}

.field-input {
  width: 100%; height: 48px; border: 1px solid #e2e8f0;
  border-radius: 12px; padding: 0 16px; font-size: 15px; color: #0f172a;
  background: #f8fafc; box-sizing: border-box;
}

.ph { color: #94a3b8; }

.forgot-row { text-align: right; margin-bottom: 20px; margin-top: -8px; }

.forgot-link { font-size: 13px; color: #2563eb; }

.btn-primary {
  width: 100%;
  height: var(--btn-height-lg);
  background: var(--primary-color);
  color: #ffffff;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--btn-radius);
  line-height: var(--btn-height-lg);
  border: none;
  margin-bottom: 24px;
  box-shadow: var(--shadow-card);
}

.btn-primary:active { background: var(--primary-hover); }

/* Divider */
.divider-row {
  display: flex; align-items: center; gap: 12px; margin-bottom: 20px;
}

.divider-line { flex: 1; height: 1px; background: #e2e8f0; }

.divider-text { font-size: 12px; color: #94a3b8; }

/* Social */
.social-row { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }

.btn-wechat {
  width: 100%; height: 48px; background: #07c160; color: #ffffff;
  font-size: 15px; font-weight: 600; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; gap: 8px; border: none;
}

.wx-icon { font-size: 18px; }

.btn-guest {
  width: 100%; height: 48px; background: #f1f5f9; color: #334155;
  font-size: 14px; font-weight: 500; border-radius: 12px; border: none;
}

/* Agreement */
.agreement-row {
  display: flex; align-items: flex-start; gap: 8px; padding-top: 8px;
}

.checkbox-wrap { flex-shrink: 0; padding-top: 2px; }

.checkbox {
  width: 18px; height: 18px; border-radius: 4px;
  border: 1.5px solid #cbd5e1; display: flex;
  align-items: center; justify-content: center;
}

.checked { background: #2563eb; border-color: #2563eb; }

.check-mark { font-size: 12px; color: #ffffff; font-weight: 700; }

.agreement-text { font-size: 12px; color: #94a3b8; line-height: 1.6; }

.link { color: #2563eb; }

/* Dark mode */
.is-dark { background: linear-gradient(180deg, #1e3a8a 0%, #1e3a8a 35%, #0f172a 35.1%); }

.is-dark .form-sheet { background: #1e293b; }

.is-dark .seg-item { color: #94a3b8; }

.is-dark .seg-active { background: #334155; color: #f8fafc; box-shadow: none; }

.is-dark .segment-bar { background: #0f172a; }

.is-dark .field-label { color: #e2e8f0; }

.is-dark .field-input { background: #0f172a; border-color: #334155; color: #f8fafc; }
</style>
