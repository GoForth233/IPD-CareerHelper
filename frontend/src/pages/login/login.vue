<template>
  <view class="login-page" :class="{ 'is-dark': darkPref }">
    <view class="status-bar-spacer"></view>

    <view class="hero">
      <text class="hero-kicker">CAREER DEVELOPMENT PLATFORM</text>
      <text class="hero-title">开启你的求职闭环</text>
      <text class="hero-sub">登录后可同步你的测评、简历和面试记录</text>
    </view>

    <view class="form-sheet">
      <view class="segment-wrap">
        <view class="segment-bar">
          <view class="seg-item" :class="{ 'seg-active': mode === 'login' }" @click="mode = 'login'">
            <text>登录</text>
          </view>
          <view class="seg-item" :class="{ 'seg-active': mode === 'register' }" @click="mode = 'register'">
            <text>注册</text>
          </view>
        </view>
      </view>

      <view class="field" v-if="mode === 'register'">
        <text class="field-label">昵称</text>
        <input class="field-input" v-model="nickname" placeholder="请输入昵称" placeholder-class="ph" />
      </view>

      <view class="field">
        <text class="field-label">手机号 / 账号</text>
        <input class="field-input" v-model="account" placeholder="请输入手机号或账号" placeholder-class="ph" />
      </view>

      <view class="field">
        <text class="field-label">密码</text>
        <input class="field-input" v-model="password" type="password" placeholder="请输入密码" placeholder-class="ph" />
      </view>

      <button class="btn-primary" :loading="loading" @click="handleSubmit">
        {{ mode === 'login' ? '登录' : '创建账号' }}
      </button>

      <view class="divider-row">
        <view class="divider-line"></view>
        <text class="divider-text">或选择以下方式</text>
        <view class="divider-line"></view>
      </view>

      <view class="social-row">
        <button class="btn-wechat" @click="wxLogin">
          <text class="wx-icon">💬</text>
          <text class="wx-text">微信快捷登录</text>
        </button>
        <button class="btn-guest" @click="guestLogin">
          <text class="guest-text">游客体验</text>
        </button>
      </view>

      <view class="agreement-row">
        <view class="checkbox-wrap" @click="agreed = !agreed">
          <view class="checkbox" :class="{ checked: agreed }">
            <text v-if="agreed" class="check-mark">✓</text>
          </view>
        </view>
        <text class="agreement-text">
          我已阅读并同意 <text class="link">用户协议</text> 与 <text class="link">隐私政策</text>
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { loginApi, registerApi } from '@/api/user';

const mode = ref<'login' | 'register'>('login');
const nickname = ref('');
const account = ref('');
const password = ref('');
const agreed = ref(false);
const loading = ref(false);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');

const goHome = () => {
  setTimeout(() => {
    uni.switchTab({ url: '/pages/home/index' });
  }, 800);
};

const ensureAgreement = () => {
  if (!agreed.value) {
    uni.showToast({ title: '请先勾选用户协议', icon: 'none' });
    return false;
  }
  return true;
};

const handleSubmit = async () => {
  if (!ensureAgreement()) return;
  if (!account.value || !password.value) {
    uni.showToast({ title: '请填写完整账号和密码', icon: 'none' });
    return;
  }
  if (mode.value === 'register' && !nickname.value) {
    uni.showToast({ title: '请输入昵称', icon: 'none' });
    return;
  }

  loading.value = true;
  try {
    if (mode.value === 'register') {
      const res = await registerApi({
        nickname: nickname.value,
        identityType: 'PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      uni.setStorageSync('userId', res.userId);
      uni.setStorageSync('userInfo', { ...res, nickname: nickname.value });
      uni.showToast({ title: '注册成功', icon: 'success' });
    } else {
      const res = await loginApi({
        identityType: 'PASSWORD',
        identifier: account.value,
        credential: password.value,
      });
      uni.setStorageSync('userId', res.userId);
      uni.setStorageSync('userInfo', res || { nickname: account.value, avatarUrl: '' });
      uni.showToast({ title: '登录成功', icon: 'success' });
    }
    goHome();
  } catch (e: any) {
    uni.showToast({ title: e?.message || '请求失败，请稍后再试', icon: 'none' });
  } finally {
    loading.value = false;
  }
};

const wxLogin = () => {
  if (!ensureAgreement()) return;
  uni.showLoading({ title: '微信授权中' });
  setTimeout(() => {
    uni.hideLoading();
    uni.setStorageSync('userId', 'wx_guest_001');
    uni.setStorageSync('userInfo', { nickname: '微信用户', avatarUrl: '' });
    uni.showToast({ title: '欢迎回来', icon: 'success' });
    goHome();
  }, 1500);
};

const guestLogin = () => {
  if (!ensureAgreement()) return;
  uni.setStorageSync('userId', 'guest_temp_001');
  uni.setStorageSync('userInfo', { nickname: '体验用户', avatarUrl: '' });
  uni.showToast({ title: '已进入体验模式', icon: 'success' });
  goHome();
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #2563eb 0%, #1e40af 35%, #f5f5f7 35.1%);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}
.status-bar-spacer { height: calc(var(--status-bar-height, 44px) + 8px); }
.hero { padding: 0 28px 28px; display: flex; flex-direction: column; }
.hero-kicker { font-size: 11px; font-weight: 600; color: rgba(255, 255, 255, 0.6); letter-spacing: 2px; margin-bottom: 8px; }
.hero-title { font-size: 26px; font-weight: 800; color: #fff; letter-spacing: -0.5px; margin-bottom: 8px; }
.hero-sub { font-size: 13px; color: rgba(255, 255, 255, 0.78); line-height: 1.55; }
.form-sheet { background: #fff; border-radius: 24px 24px 0 0; padding: 28px 24px calc(32px + env(safe-area-inset-bottom)); min-height: calc(100vh - 200px); }
.segment-wrap { margin-bottom: 24px; }
.segment-bar { display: flex; background: #f1f5f9; border-radius: 12px; padding: 3px; gap: 2px; }
.seg-item { flex: 1; text-align: center; height: 38px; line-height: 38px; border-radius: 10px; font-size: 15px; font-weight: 500; color: #64748b; }
.seg-active { background: #fff; color: #0f172a; font-weight: 600; box-shadow: 0 1px 4px rgba(0,0,0,0.08); }
.field { margin-bottom: 18px; }
.field-label { font-size: 14px; font-weight: 600; color: #334155; display: block; margin-bottom: 8px; }
.field-input { width: 100%; height: 48px; border: 1px solid #e2e8f0; border-radius: 12px; padding: 0 16px; font-size: 15px; color: #0f172a; background: #f8fafc; box-sizing: border-box; }
.ph { color: #94a3b8; }
.btn-primary { width: 100%; height: 48px; background: #2563eb; color: #fff; font-size: 16px; font-weight: 600; border-radius: 12px; line-height: 48px; border: none; margin-bottom: 24px; }
.divider-row { display: flex; align-items: center; gap: 12px; margin-bottom: 20px; }
.divider-line { flex: 1; height: 1px; background: #e2e8f0; }
.divider-text { font-size: 12px; color: #94a3b8; }
.social-row { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }
.btn-wechat { width: 100%; height: 48px; background: #07c160; color: #fff; font-size: 15px; font-weight: 600; border-radius: 12px; display: flex; align-items: center; justify-content: center; gap: 8px; border: none; }
.btn-guest { width: 100%; height: 48px; background: #f1f5f9; color: #334155; font-size: 14px; font-weight: 500; border-radius: 12px; border: none; }
.agreement-row { display: flex; align-items: flex-start; gap: 8px; padding-top: 8px; }
.checkbox-wrap { flex-shrink: 0; padding-top: 2px; }
.checkbox { width: 18px; height: 18px; border-radius: 4px; border: 1.5px solid #cbd5e1; display: flex; align-items: center; justify-content: center; }
.checked { background: #2563eb; border-color: #2563eb; }
.check-mark { font-size: 12px; color: #fff; font-weight: 700; }
.agreement-text { font-size: 12px; color: #94a3b8; line-height: 1.6; }
.link { color: #2563eb; }
.is-dark { background: linear-gradient(180deg, #1e3a8a 0%, #1e3a8a 35%, #0f172a 35.1%); }
.is-dark .form-sheet { background: #1e293b; }
.is-dark .segment-bar { background: #0f172a; }
.is-dark .seg-item { color: #94a3b8; }
.is-dark .seg-active { background: #334155; color: #f8fafc; box-shadow: none; }
.is-dark .field-label { color: #e2e8f0; }
.is-dark .field-input { background: #0f172a; border-color: #334155; color: #f8fafc; }
</style>
