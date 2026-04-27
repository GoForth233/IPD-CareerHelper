<template>
  <view class="login-page" :class="{ 'is-dark': darkPref }">
    <view class="hero">
      <view class="status-bar-spacer" :style="{ height: statusTopPx + 'px' }"></view>
      <text class="hero-kicker">CAREER LOOP</text>
      <text class="hero-title">Start Your Career Loop</text>
    </view>

    <view class="form-sheet">
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

      <view class="sheet-head">
        <text class="sheet-title">{{ mode === 'login' ? 'Welcome Back' : 'Create Your Account' }}</text>
      </view>

      <view class="field" v-if="mode === 'register'">
        <text class="field-label">Nickname</text>
        <input
          class="field-input"
          v-model="nickname"
          placeholder="Choose a display name"
          placeholder-class="ph"
          maxlength="20"
        />
      </view>

      <view class="field">
        <text class="field-label">{{ accountLabel }}</text>
        <input
          class="field-input"
          v-model="account"
          :placeholder="accountPlaceholder"
          placeholder-class="ph"
          maxlength="40"
        />
      </view>

      <view class="field">
        <text class="field-label">Password</text>
        <input
          class="field-input"
          v-model="password"
          type="password"
          placeholder="Enter your password"
          placeholder-class="ph"
          maxlength="32"
        />
      </view>

      <view class="field" v-if="mode === 'register'">
        <text class="field-label">Confirm Password</text>
        <input
          class="field-input"
          v-model="confirmPassword"
          type="password"
          placeholder="Enter your password again"
          placeholder-class="ph"
          maxlength="32"
        />
      </view>

      <view class="forgot-row" v-if="mode === 'login'">
        <text class="forgot-link" @click="handleForgot">Forgot password?</text>
      </view>

      <view class="agreement-row">
        <view class="checkbox" :class="{ 'checked': agreed }" @click="agreed = !agreed">
          <text v-if="agreed" class="check-mark">✓</text>
        </view>
        <view class="agreement-copy">
          <text class="agreement-text">I have read and agree to the</text>
          <text class="link" @click="openAgreement('terms')">Terms of Service</text>
          <text class="agreement-text">and</text>
          <text class="link" @click="openAgreement('privacy')">Privacy Policy</text>
        </view>
      </view>

      <view class="btn-primary" @click="handleSubmit" :class="{ 'is-loading': loading, 'is-disabled': !canSubmit }">
        <text class="btn-text">{{ loading ? 'Please wait...' : (mode === 'login' ? 'Sign In and Continue' : 'Create Account and Continue') }}</text>
      </view>

      <view class="divider-row">
        <view class="divider-line"></view>
        <text class="divider-text">Other methods</text>
        <view class="divider-line"></view>
      </view>

      <view class="social-row">
        <view class="btn-wechat" @click="wxLogin">
          <view class="wx-badge">
            <text class="wx-badge-text">W</text>
          </view>
          <text class="wx-text">WeChat Sign In</text>
        </view>
        <view class="btn-guest" @click="guestLogin">
          <text class="guest-text">Guest Mode</text>
        </view>
      </view>

      <view class="bottom-safe"></view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getTopSafeHeight } from '@/utils/safeArea';

/** Custom navigation: use real statusBarHeight (WeChat often has no --status-bar-height). */
const statusTopPx = ref(52);

onMounted(() => {
  statusTopPx.value = getTopSafeHeight();
});

const mode = ref<'login' | 'register'>('login');
const nickname = ref('');
const account = ref('');
const password = ref('');
const confirmPassword = ref('');
const agreed = ref(false);
const loading = ref(false);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');
const accountLabel = computed(() => 'Email');
const accountPlaceholder = computed(() => 'Enter your email address');
const canSubmit = computed(() => {
  if (loading.value || !agreed.value || !account.value || !password.value) {
    return false;
  }
  if (mode.value === 'register') {
    return !!nickname.value && !!confirmPassword.value;
  }
  return true;
});

const handleForgot = () => {
  uni.showModal({
    title: 'Password Recovery',
    content: 'Self-service recovery is not available in this demo yet. Please contact support@careerloop.ai or use WeChat sign in.',
    showCancel: false,
    confirmText: 'Got it'
  });
};

const openAgreement = (type: 'terms' | 'privacy') => {
  const title = type === 'terms' ? 'Terms of Service' : 'Privacy Policy';
  const content = type === 'terms'
    ? 'This demo does not have the final terms page wired in yet. Add the full agreement content and a dedicated detail page before release.'
    : 'This demo does not have the final privacy policy page wired in yet. Add clear details about data collection, usage, and storage before release.';

  uni.showModal({
    title,
    content,
    showCancel: false,
    confirmText: 'Close'
  });
};

const handleSubmit = async () => {
  if (!agreed.value) {
    uni.showToast({ title: 'Please agree to the terms first', icon: 'none' });
    return;
  }
  if (!account.value || !password.value) {
    uni.showToast({ title: 'Please complete the account and password fields', icon: 'none' });
    return;
  }
  if (mode.value === 'register' && !nickname.value) {
    uni.showToast({ title: 'Please enter a nickname', icon: 'none' });
    return;
  }
  if (mode.value === 'register' && !confirmPassword.value) {
    uni.showToast({ title: 'Please confirm your password', icon: 'none' });
    return;
  }
  if (mode.value === 'register' && confirmPassword.value !== password.value) {
    uni.showToast({ title: 'The two passwords do not match', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    if (mode.value === 'register') {
      const { registerApi, loginApi } = await import('@/api/user');
      await registerApi({
        nickname: nickname.value,
        identityType: 'EMAIL_PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      const loginRes = await loginApi({
        identityType: 'EMAIL_PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      uni.setStorageSync('token', loginRes.token);
      uni.setStorageSync('userId', loginRes.user.userId);
      uni.setStorageSync('userInfo', loginRes.user);
      uni.showToast({ title: 'Account created', icon: 'success' });
    } else {
      const { loginApi } = await import('@/api/user');
      const res = await loginApi({
        identityType: 'EMAIL_PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      uni.setStorageSync('token', res.token);
      uni.setStorageSync('userId', res.user.userId);
      uni.setStorageSync('userInfo', res.user);
      uni.showToast({ title: 'Signed in', icon: 'success' });
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
    uni.showToast({ title: 'Please agree to the terms first', icon: 'none' });
    return;
  }
  uni.showLoading({ title: 'Signing in...' });
  
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      if (loginRes.code) {
        try {
          const { wechatLoginApi } = await import('@/api/user');
          const res = await wechatLoginApi({ code: loginRes.code });
          
          uni.setStorageSync('token', res.token);
          uni.setStorageSync('userId', res.user.userId);
          uni.setStorageSync('userInfo', res.user);
          
          uni.hideLoading();
          uni.showToast({ title: 'Signed in', icon: 'success' });
          setTimeout(() => {
            uni.switchTab({ url: '/pages/home/index' });
          }, 800);
        } catch (e: any) {
          uni.hideLoading();
          // Error toast is already handled by request.ts
        }
      } else {
        uni.hideLoading();
        uni.showToast({ title: 'WeChat sign in failed', icon: 'none' });
      }
    },
    fail: () => {
      uni.hideLoading();
      uni.showToast({ title: 'WeChat sign in was canceled or failed', icon: 'none' });
    }
  });
};

const guestLogin = () => {
  if (!agreed.value) {
    uni.showToast({ title: 'Please agree to the terms first', icon: 'none' });
    return;
  }
  // Guest mode: do NOT set a string userId — it breaks Number() checks everywhere.
  // Pages guard with: const numericId = Number(userId); if (!isNaN(numericId) && numericId > 0)
  // A missing userId correctly skips all auth-required API calls.
  uni.removeStorageSync('userId');
  uni.removeStorageSync('token');
  uni.setStorageSync('userInfo', { nickname: 'Guest', avatarUrl: '' });
  uni.showToast({ title: 'Guest mode enabled', icon: 'success' });
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
  background:
    radial-gradient(circle at top right, rgba(59, 130, 246, 0.18), transparent 30%),
    linear-gradient(180deg, #eef4ff 0%, #f8fbff 28%, #ffffff 100%);
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif;
}

.login-page button { margin: 0; }
.login-page button::after { border: none !important; }

/* Hero */
.hero {
  padding: 0 24px 40px;
  background:
    radial-gradient(circle at right top, rgba(255,255,255,0.2), transparent 30%),
    linear-gradient(160deg, #2457d6 0%, #173ea6 70%, #102f85 100%);
}

.status-bar-spacer { width: 100%; }

.hero-kicker {
  font-size: 11px; font-weight: 700; color: rgba(255,255,255,0.68);
  letter-spacing: 2px; display: block; margin-bottom: 12px; margin-top: 12px;
}

.hero-title {
  font-size: 30px; font-weight: 800; color: #ffffff;
  display: block; line-height: 1.2;
}

.form-sheet {
  margin-top: -18px;
  background: rgba(255,255,255,0.94);
  border-radius: 28px 28px 0 0;
  padding: 24px 20px 0;
  box-shadow: 0 -8px 24px rgba(15, 23, 42, 0.04);
  backdrop-filter: blur(8px);
}

.segment-wrap { margin-bottom: 20px; }

.segment-bar {
  display: flex; background: #edf2fb; border-radius: 14px; padding: 4px;
}

.seg-item {
  flex: 1; text-align: center; height: 40px; line-height: 40px;
  border-radius: 12px; font-size: 15px; font-weight: 600; color: #8c99af;
}

.seg-active {
  background: #ffffff; color: #1e293b; font-weight: 700;
  box-shadow: 0 6px 16px rgba(37, 99, 235, 0.12);
}

.sheet-head {
  margin-bottom: 20px;
}

.sheet-title {
  display: block;
  font-size: 22px;
  font-weight: 800;
  color: #172033;
}

.field { margin-bottom: 16px; }

.field-label {
  font-size: 13px; font-weight: 700; color: #475569;
  display: block; margin-bottom: 8px;
}

.field-input {
  width: 100%; height: 52px; border: 1.5px solid #d7deea;
  border-radius: 14px; padding: 0 16px; font-size: 15px; color: #1e293b;
  background: #fdfefe; box-sizing: border-box;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.7);
}

.ph { color: #9aa8bc; }

.forgot-row { text-align: right; margin-top: -4px; margin-bottom: 20px; }
.forgot-link { font-size: 13px; color: #2457d6; font-weight: 600; }

.agreement-row {
  display: flex; align-items: flex-start; gap: 10px; margin-bottom: 18px;
}

.checkbox {
  width: 20px; height: 20px; border-radius: 6px; flex-shrink: 0;
  border: 1.5px solid #b8c5d8;
  display: flex; align-items: center; justify-content: center;
  background: #ffffff;
  margin-top: 1px;
}

.checked { background: #2457d6; border-color: #2457d6; }
.check-mark { font-size: 13px; color: #ffffff; font-weight: 700; }

.agreement-copy {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  row-gap: 2px;
  column-gap: 2px;
  flex: 1;
}

.agreement-text { font-size: 12px; color: #64748b; line-height: 1.6; }
.link { color: #2457d6; font-weight: 600; }

.btn-primary {
  width: 100%; height: 54px; background: linear-gradient(135deg, #3568e8 0%, #2457d6 100%);
  border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 10px 24px rgba(37,99,235,0.28);
  transition: all 0.2s ease;
}

.btn-text {
  color: #ffffff;
  font-size: 16px; 
  font-weight: 700;
}

.is-loading { opacity: 0.7; pointer-events: none; }
.is-disabled { opacity: 0.55; }
.btn-primary:active { opacity: 0.88; transform: scale(0.98); }

.divider-row {
  display: flex; align-items: center; gap: 10px; margin-bottom: 16px;
}

.divider-line { flex: 1; height: 1px; background: #dde5f0; }
.divider-text { font-size: 12px; color: #94a3b8; white-space: nowrap; }

.social-row { display: flex; gap: 12px; margin-bottom: 0; }

.btn-wechat {
  flex: 1; height: 48px; background: #07c160;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
  transition: all 0.2s ease;
  box-shadow: 0 8px 18px rgba(7, 193, 96, 0.18);
}

.wx-badge {
  width: 20px;
  height: 20px;
  border-radius: 999px;
  background: rgba(255,255,255,0.92);
  display: flex;
  align-items: center;
  justify-content: center;
}

.wx-badge-text {
  font-size: 11px;
  color: #07c160;
  font-weight: 800;
}

.wx-text { color: #ffffff; font-size: 14px; font-weight: 700; }

.btn-guest {
  flex: 1; height: 48px; background: #eef2f8;
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  transition: all 0.2s ease;
}
.guest-text { color: #516176; font-size: 14px; font-weight: 700; }

.btn-guest:active { background: #e2e8f0; transform: scale(0.98); }
.btn-wechat:active { opacity: 0.88; transform: scale(0.98); }

.bottom-safe { height: calc(env(safe-area-inset-bottom, 0px) + 24px); }
</style>
