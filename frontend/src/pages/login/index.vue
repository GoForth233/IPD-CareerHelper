<template>
  <view class="login-page" :class="{ 'is-dark': darkPref }">
    <!-- Top hero area -->
    <view class="hero">
      <view class="status-bar-spacer" :style="{ height: statusTopPx + 'px' }"></view>
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
        <input class="field-input" v-model="nickname" placeholder="Choose a display name" placeholder-class="ph" />
      </view>

      <!-- Phone / Account -->
      <view class="field">
        <text class="field-label">Phone / Account</text>
        <input class="field-input" v-model="account" placeholder="Enter your account ID" placeholder-class="ph" />
      </view>

      <!-- Password -->
      <view class="field">
        <text class="field-label">Password</text>
        <input class="field-input" v-model="password" type="password" placeholder="Enter your password" placeholder-class="ph" />
      </view>

      <!-- Forgot password (login only) -->
      <view class="forgot-row" v-if="mode === 'login'">
        <text class="forgot-link" @click="handleForgot">Forgot password?</text>
      </view>

      <!-- Agreement -->
      <view class="agreement-row" @click="agreed = !agreed">
        <view class="checkbox" :class="{ 'checked': agreed }">
          <text v-if="agreed" class="check-mark">✓</text>
        </view>
        <text class="agreement-text">
          I agree to the <text class="link">Terms of Service</text> and <text class="link">Privacy Policy</text>
        </text>
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
          <text class="guest-text">Guest Mode</text>
        </button>
      </view>

      <view class="bottom-safe"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

/** Custom navigation: use real statusBarHeight (WeChat often has no --status-bar-height). */
const statusTopPx = ref(52);

onMounted(() => {
  try {
    const sys = uni.getSystemInfoSync() as UniApp.GetSystemInfoResult;
    const insetTop = Number(sys.safeAreaInsets?.top);
    const sb = Number(sys.statusBarHeight) || 44;
    const top = insetTop > 0 ? insetTop : sb;
    statusTopPx.value = top + 10;
  } catch {
    statusTopPx.value = 52;
  }
});

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
      uni.setStorageSync('token', res.token);
      uni.setStorageSync('userId', res.user.userId);
      uni.setStorageSync('userInfo', res.user);
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
  display: flex;
  flex-direction: column;
  background: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.login-page button { margin: 0; }
.login-page button::after { border: none !important; }

/* Hero */
.hero {
  padding: 0 24px 32px;
  background: linear-gradient(145deg, #2563eb 0%, #1e40af 100%);
}

.status-bar-spacer { width: 100%; }

.hero-kicker {
  font-size: 11px; font-weight: 600; color: rgba(255,255,255,0.55);
  letter-spacing: 2px; display: block; margin-bottom: 10px; margin-top: 12px;
}

.hero-title {
  font-size: 28px; font-weight: 800; color: #ffffff;
  letter-spacing: -0.5px; display: block; margin-bottom: 6px;
}

.hero-sub {
  font-size: 13px; color: rgba(255,255,255,0.72); line-height: 1.5; display: block;
}

/* Form card */
.form-sheet {
  margin-top: -20px;
  background: #ffffff;
  border-radius: 24px 24px 0 0;
  padding: 24px 20px 0;
  box-shadow: 0 -4px 20px rgba(0,0,0,0.06);
}

/* Segment */
.segment-wrap { margin-bottom: 20px; }

.segment-bar {
  display: flex; background: #f1f5f9; border-radius: 12px; padding: 3px;
}

.seg-item {
  flex: 1; text-align: center; height: 40px; line-height: 40px;
  border-radius: 10px; font-size: 15px; font-weight: 500; color: #94a3b8;
}

.seg-active {
  background: #ffffff; color: #1e293b; font-weight: 700;
  box-shadow: 0 1px 6px rgba(0,0,0,0.1);
}

/* Fields */
.field { margin-bottom: 16px; }

.field-label {
  font-size: 13px; font-weight: 600; color: #475569;
  display: block; margin-bottom: 6px;
}

.field-input {
  width: 100%; height: 50px; border: 1.5px solid #e2e8f0;
  border-radius: 12px; padding: 0 16px; font-size: 15px; color: #1e293b;
  background: #f8fafc; box-sizing: border-box;
}

.ph { color: #cbd5e1; }

.forgot-row { text-align: right; margin-top: -6px; margin-bottom: 20px; }
.forgot-link { font-size: 13px; color: #2563eb; font-weight: 500; }

/* Agreement */
.agreement-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 16px;
}

.checkbox {
  width: 20px; height: 20px; border-radius: 6px; flex-shrink: 0;
  border: 1.5px solid #cbd5e1;
  display: flex; align-items: center; justify-content: center;
  background: #ffffff;
}

.checked { background: #2563eb; border-color: #2563eb; }
.check-mark { font-size: 13px; color: #ffffff; font-weight: 700; }

.agreement-text { font-size: 12px; color: #94a3b8; line-height: 1.5; flex: 1; }
.link { color: #2563eb; font-weight: 500; }

/* Primary button */
.btn-primary {
  width: 100%; height: 52px; background: #2563eb; color: #ffffff;
  font-size: 16px; font-weight: 700; border-radius: 14px; line-height: 52px;
  border: none; margin-bottom: 20px;
  box-shadow: 0 6px 18px rgba(37,99,235,0.35);
}

.btn-primary:active { opacity: 0.88; }

/* Divider */
.divider-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 16px;
}

.divider-line { flex: 1; height: 1px; background: #e2e8f0; }
.divider-text { font-size: 12px; color: #cbd5e1; white-space: nowrap; }

/* Social */
.social-row { display: flex; gap: 12px; margin-bottom: 0; }

.btn-wechat {
  flex: 1; height: 48px; background: #07c160; color: #ffffff;
  font-size: 14px; font-weight: 600; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; gap: 6px; border: none;
}

.wx-icon { font-size: 16px; }

.btn-guest {
  flex: 1; height: 48px; background: #f1f5f9; color: #64748b;
  font-size: 14px; font-weight: 500; border-radius: 12px; border: none;
}

.btn-guest:active { background: #e2e8f0; }

.bottom-safe { height: calc(env(safe-area-inset-bottom, 0px) + 24px); }
</style>
