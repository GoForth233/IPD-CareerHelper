<template>
  <view class="login-page">

    <!-- ── 自定义 Toast ── -->
    <view class="snack-bar" :class="['snack-' + snack.type, snack.visible ? 'snack-show' : '']">
      <text class="snack-icon">{{ snack.type === 'success' ? '✓' : snack.type === 'error' ? '✕' : 'ℹ' }}</text>
      <text class="snack-msg">{{ snack.message }}</text>
    </view>

    <view class="hero">
      <view class="status-bar-spacer" :style="{ height: statusTopPx + 'px' }"></view>
      <text class="hero-kicker">CAREER LOOP</text>
      <text class="hero-title">Start Your Career Loop</text>
      <text class="hero-subtitle">Sign in to keep your assessments, resumes, and interview records connected across the product.</text>
    </view>

    <view class="form-sheet">
      <view class="segment-wrap">
        <view class="segment-bar">
          <view class="seg-item" :class="{ 'seg-active': mode === 'login' }" @click="switchMode('login')"><text>Sign In</text></view>
          <view class="seg-item" :class="{ 'seg-active': mode === 'register' }" @click="switchMode('register')"><text>Sign Up</text></view>
        </view>
      </view>

      <view class="sheet-head">
        <text class="sheet-title">{{ mode === 'login' ? 'Welcome Back' : 'Create Your Account' }}</text>
        <text class="sheet-subtitle">{{ mode === 'login' ? 'Pick up where you left off.' : 'Set up your account and unlock the full workflow.' }}</text>
      </view>

      <!-- Nickname -->
      <view class="field" v-if="mode === 'register'">
        <text class="field-label">Nickname</text>
        <input class="field-input" :class="{ 'input-error': nicknameError }" v-model="nickname"
          placeholder="Choose a display name" placeholder-class="ph" maxlength="20" @blur="validateNickname" />
        <text class="field-hint-error" v-if="nicknameError">{{ nicknameError }}</text>
      </view>

      <!-- Email -->
      <view class="field">
        <text class="field-label">Email</text>
        <input class="field-input" :class="{ 'input-error': emailError }" v-model="account"
          placeholder="Enter your email address" placeholder-class="ph" maxlength="60"
          @input="onEmailInput" @blur="onEmailBlur" />
        <text class="field-hint-error" v-if="emailError">{{ emailError }}</text>
        <text class="field-hint-checking" v-if="checkingEmail">Checking availability...</text>
      </view>

      <!-- Password -->
      <view class="field">
        <text class="field-label">Password</text>
        <input class="field-input" :class="{ 'input-error': passwordStrength && passwordStrength.level === 0 }"
          v-model="password" type="password" placeholder="Enter your password" placeholder-class="ph" maxlength="32" />
        <view class="strength-row" v-if="mode === 'register' && password.length > 0">
          <view class="strength-bars">
            <view v-for="i in 3" :key="i" class="strength-bar"
              :style="{ background: passwordStrength && passwordStrength.level >= i ? passwordStrength.color : '#e2e8f0' }" />
          </view>
          <text class="strength-label" :style="{ color: passwordStrength ? passwordStrength.color : '#94a3b8' }">
            {{ passwordStrength ? passwordStrength.label : '' }}
          </text>
        </view>
      </view>

      <!-- Confirm Password -->
      <view class="field" v-if="mode === 'register'">
        <text class="field-label">Confirm Password</text>
        <input class="field-input" :class="{ 'input-error': confirmPassword.length > 0 && confirmPassword !== password }"
          v-model="confirmPassword" type="password" placeholder="Enter your password again"
          placeholder-class="ph" maxlength="32" />
        <text class="field-hint-error" v-if="confirmPassword.length > 0 && confirmPassword !== password">Passwords do not match</text>
      </view>

      <!-- Verification Code -->
      <view class="field" v-if="mode === 'register'">
        <text class="field-label">Email Verification Code</text>
        <view class="code-row">
          <input class="field-input code-input" :class="{ 'input-error': codeError }"
            v-model="verifyCode" placeholder="6-digit code" placeholder-class="ph"
            maxlength="6" type="number" @blur="validateCode" />
          <view class="btn-send-code" :class="{ 'is-disabled': codeCooldown > 0 || sendingCode }" @click="sendRegisterCode">
            <text class="btn-send-text">{{ sendingCode ? '...' : (codeCooldown > 0 ? codeCooldown + 's' : 'Send Code') }}</text>
          </view>
        </view>
        <text class="field-hint-error" v-if="codeError">{{ codeError }}</text>
      </view>

      <!-- Forgot password -->
      <view class="forgot-row" v-if="mode === 'login'">
        <text class="forgot-link" @click="showForgotModal = true">Forgot password?</text>
      </view>

      <!-- Agreement -->
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
          <view class="wx-badge"><text class="wx-badge-text">W</text></view>
          <text class="wx-text">WeChat Sign In</text>
        </view>
        <view class="btn-guest" @click="guestLogin">
          <text class="guest-text">Guest Mode</text>
        </view>
      </view>
      <view class="bottom-safe"></view>
    </view>

    <!-- ── Terms / Privacy Modal ── -->
    <view class="modal-mask" v-if="showAgreementModal" @tap="showAgreementModal = false">
      <view class="modal-card agreement-modal" @tap.stop>
        <text class="modal-title">{{ agreementType === 'terms' ? 'Terms of Service' : 'Privacy Policy' }}</text>
        <scroll-view scroll-y class="agreement-scroll">
          <view v-if="agreementType === 'terms'">
            <text class="agreement-section-title">1. Acceptance of Terms</text>
            <text class="agreement-body">By registering and using Career Loop, you agree to be bound by these Terms of Service. If you do not agree, please do not use this application.</text>
            <text class="agreement-section-title">2. Use of Service</text>
            <text class="agreement-body">Career Loop provides AI-powered career guidance, resume analysis, interview practice, and related tools. You agree to use the service only for lawful purposes and in accordance with these terms.</text>
            <text class="agreement-section-title">3. Account Responsibility</text>
            <text class="agreement-body">You are responsible for maintaining the confidentiality of your account credentials. You agree to notify us immediately of any unauthorized use of your account.</text>
            <text class="agreement-section-title">4. User Content</text>
            <text class="agreement-body">You retain ownership of content you submit (such as resumes). By submitting content, you grant Career Loop a limited license to process it solely for providing the service to you.</text>
            <text class="agreement-section-title">5. Prohibited Conduct</text>
            <text class="agreement-body">You may not use Career Loop to: violate any laws, infringe intellectual property rights, transmit harmful or malicious content, or attempt to gain unauthorized access to our systems.</text>
            <text class="agreement-section-title">6. Disclaimer</text>
            <text class="agreement-body">Career Loop is provided "as is". AI-generated career advice is for reference only and does not constitute professional career counseling. We make no guarantees about employment outcomes.</text>
            <text class="agreement-section-title">7. Modifications</text>
            <text class="agreement-body">We reserve the right to modify these terms at any time. Continued use after changes constitutes acceptance of the revised terms.</text>
            <text class="agreement-section-title">8. Contact</text>
            <text class="agreement-body">For any questions, contact us at support@careerloop.ai</text>
          </view>
          <view v-else>
            <text class="agreement-section-title">1. Information We Collect</text>
            <text class="agreement-body">We collect information you provide directly: nickname, email address, and profile details (school, major, graduation year). We also collect resume files you upload and interaction data with our AI features.</text>
            <text class="agreement-section-title">2. How We Use Your Information</text>
            <text class="agreement-body">We use your information to: provide and improve our services, personalize your career guidance experience, analyze resume content with AI tools, and send service-related communications (including verification codes).</text>
            <text class="agreement-section-title">3. Data Storage</text>
            <text class="agreement-body">Your data is stored securely on servers located in China. Resume files are stored via Aliyun OSS with encrypted transmission. Passwords are stored using BCrypt encryption and are never stored in plain text.</text>
            <text class="agreement-section-title">4. Data Sharing</text>
            <text class="agreement-body">We do not sell your personal data. We may share data with trusted third-party service providers (e.g., Aliyun for storage, AI model providers) solely to operate the service. These providers are bound by confidentiality obligations.</text>
            <text class="agreement-section-title">5. Data Retention</text>
            <text class="agreement-body">We retain your account data for as long as your account is active. You may request deletion of your account and associated data at any time by contacting support@careerloop.ai.</text>
            <text class="agreement-section-title">6. Your Rights</text>
            <text class="agreement-body">You have the right to access, correct, or delete your personal data. You may also withdraw consent at any time. To exercise these rights, contact support@careerloop.ai.</text>
            <text class="agreement-section-title">7. Cookies & Analytics</text>
            <text class="agreement-body">We may use anonymized analytics to understand how users interact with the app. No personally identifiable information is used for analytics purposes.</text>
            <text class="agreement-section-title">8. Contact</text>
            <text class="agreement-body">For privacy concerns, contact our data protection team at support@careerloop.ai</text>
          </view>
        </scroll-view>
        <view class="modal-actions">
          <view class="modal-btn modal-btn-confirm" @click="showAgreementModal = false"><text>I Understand</text></view>
        </view>
      </view>
    </view>

    <!-- ── Forgot Password Modal ── -->
    <view class="modal-mask" v-if="showForgotModal" @tap="closeForgotModal">
      <view class="modal-card" @tap.stop>
        <text class="modal-title">Reset Password</text>

        <view v-if="resetStep === 1">
          <text class="modal-hint">Enter the email address of your account.</text>
          <view class="field" style="margin-top:14px;">
            <text class="field-label">Email</text>
            <input class="field-input" :class="{ 'input-error': resetEmailError }"
              v-model="resetEmail" placeholder="your@email.com" placeholder-class="ph"
              maxlength="60" @blur="validateResetEmail" />
            <text class="field-hint-error" v-if="resetEmailError">{{ resetEmailError }}</text>
          </view>
          <view class="code-row" style="margin-top:12px;">
            <input class="field-input code-input" v-model="resetCode"
              placeholder="6-digit code" placeholder-class="ph" maxlength="6" type="number" />
            <view class="btn-send-code" :class="{ 'is-disabled': resetCooldown > 0 || sendingReset }" @click="sendResetCode">
              <text class="btn-send-text">{{ sendingReset ? '...' : (resetCooldown > 0 ? resetCooldown + 's' : 'Send Code') }}</text>
            </view>
          </view>
          <view class="modal-actions">
            <view class="modal-btn modal-btn-cancel" @click="closeForgotModal"><text>Cancel</text></view>
            <view class="modal-btn modal-btn-confirm" @click="goResetStep2"><text>Next</text></view>
          </view>
        </view>

        <view v-if="resetStep === 2">
          <text class="modal-hint">Set a new password for <text style="color:#2457d6;">{{ resetEmail }}</text></text>
          <view class="field" style="margin-top:14px;">
            <text class="field-label">New Password</text>
            <input class="field-input" :class="{ 'input-error': resetNewPwd.length > 0 && resetNewPwd.length < 6 }"
              v-model="resetNewPwd" type="password" placeholder="At least 6 characters" placeholder-class="ph" maxlength="32" />
            <text class="field-hint-error" v-if="resetNewPwd.length > 0 && resetNewPwd.length < 6">At least 6 characters</text>
          </view>
          <view class="field" style="margin-top:12px;">
            <text class="field-label">Confirm New Password</text>
            <input class="field-input" :class="{ 'input-error': resetConfirmPwd.length > 0 && resetConfirmPwd !== resetNewPwd }"
              v-model="resetConfirmPwd" type="password" placeholder="Repeat new password" placeholder-class="ph" maxlength="32" />
            <text class="field-hint-error" v-if="resetConfirmPwd.length > 0 && resetConfirmPwd !== resetNewPwd">Passwords do not match</text>
          </view>
          <view class="modal-actions">
            <view class="modal-btn modal-btn-cancel" @click="resetStep = 1"><text>Back</text></view>
            <view class="modal-btn modal-btn-confirm" :class="{ 'is-loading': resetting }" @click="doResetPassword">
              <text>{{ resetting ? 'Resetting...' : 'Reset Password' }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { getTopSafeHeight } from '@/utils/safeArea';
import { sendCodeApi, resetPasswordApi, registerApi, loginApi, wechatLoginApi, checkEmailApi } from '@/api/user';
import { enterGuestMode } from '@/utils/auth';

const statusTopPx = ref(52);
onMounted(() => { statusTopPx.value = getTopSafeHeight(); });

// ─── 自定义 Toast ───────────────────────────────────────────────
const snack = reactive({ visible: false, message: '', type: 'info' as 'success' | 'error' | 'info' });
let snackTimer: ReturnType<typeof setTimeout> | null = null;
const showSnack = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
  if (snackTimer) clearTimeout(snackTimer);
  snack.message = message;
  snack.type = type;
  snack.visible = true;
  snackTimer = setTimeout(() => { snack.visible = false; }, 3000);
};

// ─── 表单字段 ────────────────────────────────────────────────────
const mode = ref<'login' | 'register'>('login');
const nickname = ref('');
const account = ref('');
const password = ref('');
const confirmPassword = ref('');
const verifyCode = ref('');
const agreed = ref(false);
const loading = ref(false);
const darkPref = ref(uni.getStorageSync('app_pref_dark') === '1');

// ─── 校验状态 ────────────────────────────────────────────────────
const nicknameError = ref('');
const emailError = ref('');
const codeError = ref('');
const resetEmailError = ref('');

const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const validateNickname = () => {
  if (!nickname.value) { nicknameError.value = ''; return; }
  nicknameError.value = nickname.value.length < 2 ? 'At least 2 characters' : '';
};

const onEmailInput = () => {
  if (!account.value) { emailError.value = ''; return; }
  emailError.value = emailRegex.test(account.value) ? '' : 'Please enter a valid email address';
};

const emailExists = ref(false);
const checkingEmail = ref(false);
const onEmailBlur = async () => {
  if (!account.value) { emailError.value = ''; return; }
  if (!emailRegex.test(account.value)) {
    emailError.value = 'Please enter a valid email address';
    return;
  }
  emailError.value = '';
  if (mode.value !== 'register') return;
  checkingEmail.value = true;
  try {
    const exists = await checkEmailApi(account.value);
    emailExists.value = !!exists;
    emailError.value = exists ? 'This email is already registered' : '';
  } catch { emailExists.value = false; }
  finally { checkingEmail.value = false; }
};

const validateResetEmail = () => {
  if (!resetEmail.value) { resetEmailError.value = ''; return; }
  resetEmailError.value = emailRegex.test(resetEmail.value) ? '' : 'Please enter a valid email address';
};

const validateCode = () => {
  if (!verifyCode.value) { codeError.value = ''; return; }
  codeError.value = /^\d{6}$/.test(verifyCode.value) ? '' : 'Code must be 6 digits';
};

// ─── 密码强度 ────────────────────────────────────────────────────
const passwordStrength = computed(() => {
  const p = password.value;
  if (!p) return null;
  if (p.length < 6) return { level: 0, label: 'Too short', color: '#ef4444' };
  const types = [/[A-Z]/.test(p), /[a-z]/.test(p), /[0-9]/.test(p), /[^A-Za-z0-9]/.test(p)].filter(Boolean).length;
  if (p.length >= 10 && types >= 3) return { level: 3, label: 'Strong', color: '#22c55e' };
  if (p.length >= 8 && types >= 2) return { level: 2, label: 'Medium', color: '#f59e0b' };
  return { level: 1, label: 'Weak', color: '#ef4444' };
});

// ─── 验证码冷却 ──────────────────────────────────────────────────
const codeCooldown = ref(0);
const sendingCode = ref(false);
let cooldownTimer: ReturnType<typeof setInterval> | null = null;
const resetCooldown = ref(0);
const sendingReset = ref(false);
let resetCooldownTimer: ReturnType<typeof setInterval> | null = null;

const startCooldown = (cdRef: typeof codeCooldown, timerRef: { get: () => ReturnType<typeof setInterval> | null; set: (v: any) => void }) => {
  cdRef.value = 60;
  const t = setInterval(() => {
    cdRef.value--;
    if (cdRef.value <= 0) { clearInterval(t); timerRef.set(null); }
  }, 1000);
  timerRef.set(t);
};

// ─── 找回密码弹窗 ────────────────────────────────────────────────
const showForgotModal = ref(false);
const resetStep = ref(1);
const resetEmail = ref('');
const resetCode = ref('');
const resetNewPwd = ref('');
const resetConfirmPwd = ref('');
const resetting = ref(false);

const canSubmit = computed(() => {
  if (loading.value || !agreed.value || !account.value || !password.value) return false;
  if (mode.value === 'register') return !!nickname.value && !!confirmPassword.value && !!verifyCode.value;
  return true;
});

const switchMode = (m: 'login' | 'register') => {
  mode.value = m;
  verifyCode.value = '';
  emailExists.value = false;
  emailError.value = '';
  nicknameError.value = '';
  codeError.value = '';
  codeCooldown.value = 0;
  if (cooldownTimer) { clearInterval(cooldownTimer); cooldownTimer = null; }
};

const sendRegisterCode = async () => {
  if (codeCooldown.value > 0 || sendingCode.value) return;
  if (!agreed.value) { showSnack('Please agree to the Terms of Service first', 'error'); return; }
  if (!account.value || !emailRegex.test(account.value)) { showSnack('Please enter a valid email first', 'error'); return; }
  if (emailExists.value) { showSnack('This email is already registered', 'error'); return; }
  sendingCode.value = true;
  try {
    await sendCodeApi({ email: account.value, purpose: 'REGISTER' });
    showSnack('Verification code sent to your email', 'success');
    startCooldown(codeCooldown, { get: () => cooldownTimer, set: (v) => { cooldownTimer = v; } });
  } catch (e: any) {
    showSnack(e?.message || 'Failed to send code', 'error');
  } finally { sendingCode.value = false; }
};

const sendResetCode = async () => {
  if (resetCooldown.value > 0 || sendingReset.value) return;
  if (!resetEmail.value || !emailRegex.test(resetEmail.value)) { showSnack('Please enter a valid email first', 'error'); return; }
  sendingReset.value = true;
  try {
    await sendCodeApi({ email: resetEmail.value, purpose: 'RESET' });
    showSnack('Verification code sent to your email', 'success');
    startCooldown(resetCooldown, { get: () => resetCooldownTimer, set: (v) => { resetCooldownTimer = v; } });
  } catch (e: any) {
    showSnack(e?.message || 'Failed to send code', 'error');
  } finally { sendingReset.value = false; }
};

const goResetStep2 = () => {
  if (!resetEmail.value || !emailRegex.test(resetEmail.value)) { showSnack('Please enter a valid email', 'error'); return; }
  if (!resetCode.value || !/^\d{6}$/.test(resetCode.value)) { showSnack('Please enter the 6-digit verification code', 'error'); return; }
  resetStep.value = 2;
};

const doResetPassword = async () => {
  if (resetNewPwd.value.length < 6) { showSnack('Password must be at least 6 characters', 'error'); return; }
  if (resetNewPwd.value !== resetConfirmPwd.value) { showSnack('Passwords do not match', 'error'); return; }
  resetting.value = true;
  try {
    await resetPasswordApi({ email: resetEmail.value, code: resetCode.value, newPassword: resetNewPwd.value });
    showSnack('Password reset successfully! Please sign in.', 'success');
    closeForgotModal();
  } catch (e: any) {
    showSnack(e?.message || 'Reset failed, please try again', 'error');
  } finally { resetting.value = false; }
};

const closeForgotModal = () => {
  showForgotModal.value = false;
  resetStep.value = 1;
  resetEmail.value = '';
  resetCode.value = '';
  resetNewPwd.value = '';
  resetConfirmPwd.value = '';
  resetEmailError.value = '';
  resetCooldown.value = 0;
  if (resetCooldownTimer) { clearInterval(resetCooldownTimer); resetCooldownTimer = null; }
};

const showAgreementModal = ref(false);
const agreementType = ref<'terms' | 'privacy'>('terms');
const openAgreement = (type: 'terms' | 'privacy') => { agreementType.value = type; showAgreementModal.value = true; };

const handleSubmit = async () => {
  if (!agreed.value) { showSnack('Please agree to the Terms of Service first', 'error'); return; }
  if (!account.value || !emailRegex.test(account.value)) { showSnack('Please enter a valid email address', 'error'); return; }
  if (!password.value) { showSnack('Please enter your password', 'error'); return; }
  if (mode.value === 'register') {
    if (!nickname.value || nickname.value.length < 2) { showSnack('Nickname must be at least 2 characters', 'error'); return; }
    if (emailExists.value) { showSnack('This email is already registered', 'error'); return; }
    if (password.value.length < 6) { showSnack('Password must be at least 6 characters', 'error'); return; }
    if (confirmPassword.value !== password.value) { showSnack('The two passwords do not match', 'error'); return; }
    if (!verifyCode.value || !/^\d{6}$/.test(verifyCode.value)) { showSnack('Please enter the 6-digit verification code', 'error'); return; }
  }
  loading.value = true;
  try {
    if (mode.value === 'register') {
      await registerApi({ nickname: nickname.value, identityType: 'EMAIL_PASSWORD', identifier: account.value, credential: password.value, code: verifyCode.value });
      const loginRes = await loginApi({ identityType: 'EMAIL_PASSWORD', identifier: account.value, credential: password.value });
      uni.setStorageSync('token', loginRes.token);
      uni.setStorageSync('userId', loginRes.user.userId);
      uni.setStorageSync('userInfo', loginRes.user);
      showSnack('Account created! Welcome 🎉', 'success');
    } else {
      const res = await loginApi({ identityType: 'EMAIL_PASSWORD', identifier: account.value, credential: password.value });
      uni.setStorageSync('token', res.token);
      uni.setStorageSync('userId', res.user.userId);
      uni.setStorageSync('userInfo', res.user);
      showSnack('Signed in successfully', 'success');
    }
    setTimeout(() => { uni.switchTab({ url: '/pages/home/index' }); }, 1000);
  } catch (e: any) {
    showSnack(e?.message || 'Request failed. Please try again.', 'error');
  } finally { loading.value = false; }
};

/**
 * WeChat MP one-tap sign-in:
 *   uni.login({ provider:'weixin' })  →  short-lived `code` (WeChat side)
 *   POST /auth/wechat-login { code }  →  backend exchanges via jscode2session,
 *                                        returns { token, user }
 *
 * The very first time a WeChat openid is seen the backend creates a user with
 * a placeholder nickname ("WeChat User"); the Profile page will hand the user
 * a chance to personalize. We keep that two-step flow because requesting a
 * nickname/avatar via wx.getUserProfile requires a button-tap and we don't
 * want to gate first-run behind an extra modal.
 */
const wxLogin = () => {
  if (!agreed.value) { showSnack('Please agree to the Terms of Service first', 'error'); return; }
  uni.showLoading({ title: 'Signing in...' });
  uni.login({
    provider: 'weixin',
    success: async (loginRes) => {
      if (!loginRes.code) {
        uni.hideLoading();
        showSnack('WeChat did not return a login code', 'error');
        return;
      }
      try {
        const res = await wechatLoginApi({ code: loginRes.code });
        uni.setStorageSync('token', res.token);
        uni.setStorageSync('userId', res.user.userId);
        uni.setStorageSync('userInfo', res.user);
        uni.hideLoading();
        showSnack('Signed in successfully', 'success');
        setTimeout(() => { uni.switchTab({ url: '/pages/home/index' }); }, 1000);
      } catch (e: any) {
        uni.hideLoading();
        // Surface the actual reason -- usually a misconfigured appId/secret on
        // the server, or the user's tenant hasn't whitelisted our backend.
        showSnack(e?.message || 'WeChat sign in failed on the server', 'error');
      }
    },
    fail: () => { uni.hideLoading(); showSnack('WeChat sign in was canceled', 'error'); }
  });
};

const guestLogin = () => {
  if (!agreed.value) { showSnack('Please agree to the Terms of Service first', 'error'); return; }
  // Guest mode now stores a sentinel userId (-1) plus an `isGuest` flag so
  // the App.vue cold-start gate doesn't treat the guest as "no session" and
  // kick them back here every relaunch.
  enterGuestMode();
  showSnack('Guest mode enabled — limited features available', 'info');
  setTimeout(() => { uni.switchTab({ url: '/pages/home/index' }); }, 800);
};
</script>

<style scoped>
/* ── Snack Bar ── */
.snack-bar {
  position: fixed;
  top: 60px;
  left: 16px;
  right: 16px;
  z-index: 9999;
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 8px 24px rgba(15,23,42,0.16);
  opacity: 0;
  transform: translateY(-12px);
  transition: opacity 0.25s ease, transform 0.25s ease;
  pointer-events: none;
}
.snack-show { opacity: 1; transform: translateY(0); pointer-events: auto; }
.snack-success { background: #f0fdf4; border: 1.5px solid #86efac; }
.snack-error   { background: #fff1f2; border: 1.5px solid #fca5a5; }
.snack-info    { background: #eff6ff; border: 1.5px solid #93c5fd; }
.snack-icon { font-size: 15px; font-weight: 800; flex-shrink: 0; }
.snack-success .snack-icon { color: #16a34a; }
.snack-error   .snack-icon { color: #dc2626; }
.snack-info    .snack-icon { color: #2563eb; }
.snack-msg { font-size: 13px; font-weight: 600; line-height: 1.4; }
.snack-success .snack-msg { color: #166534; }
.snack-error   .snack-msg { color: #991b1b; }
.snack-info    .snack-msg { color: #1e40af; }

/* ── Page ── */
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg,#eef4ff 0%,#f6f9ff 24%,#ffffff 100%);
  font-family: -apple-system,BlinkMacSystemFont,"SF Pro Text","Helvetica Neue",sans-serif;
  box-sizing: border-box;
}
.login-page button { margin: 0; }
.login-page button::after { border: none !important; }

.hero {
  padding: 0 20px 42px;
  background: linear-gradient(160deg,#2457d6 0%,#173ea6 70%,#102f85 100%);
}
/* #ifdef H5 */
.hero { padding-top: 20px; }
/* #endif */
.status-bar-spacer { width: 100%; }
.hero-kicker { font-size: 11px; font-weight: 700; color: rgba(255,255,255,0.68); letter-spacing: 2px; display: block; margin-bottom: 12px; margin-top: 12px; }
.hero-title { font-size: 30px; font-weight: 800; color: #ffffff; display: block; line-height: 1.2; }
.hero-subtitle {
  display: block;
  margin-top: 12px;
  max-width: 520px;
  font-size: 14px;
  line-height: 1.55;
  color: rgba(255,255,255,0.82);
}

.form-sheet {
  width: calc(100% - 32px);
  max-width: var(--content-max-width);
  flex: 1;
  margin-top: -18px;
  margin-left: auto;
  margin-right: auto;
  background: rgba(255,255,255,0.96);
  border: 1px solid rgba(214,225,241,0.9);
  border-radius: 28px 28px 0 0;
  padding: 24px var(--page-gutter-tight) 0;
  box-shadow: 0 -8px 24px rgba(15,23,42,0.04);
  backdrop-filter: blur(8px);
  box-sizing: border-box;
}
.segment-wrap { margin-bottom: 20px; }
.segment-bar { display: flex; background: #edf2fb; border: 1px solid #dbe4f0; border-radius: 14px; padding: 4px; }
.seg-item { flex: 1; text-align: center; height: 40px; line-height: 40px; border-radius: 12px; font-size: 15px; font-weight: 600; color: #8c99af; }
.seg-active { background: #ffffff; color: #1e293b; font-weight: 700; box-shadow: 0 4px 10px rgba(37,99,235,0.08); }
.sheet-head { margin-bottom: 20px; }
.sheet-title { display: block; font-size: 22px; font-weight: 800; color: #172033; }
.sheet-subtitle {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.5;
  color: #64748b;
}

.field { margin-bottom: 16px; }
.field-label { font-size: 13px; font-weight: 700; color: #475569; display: block; margin-bottom: 8px; }
.field-input { width: 100%; height: 52px; border: 1.5px solid #d7deea; border-radius: 14px; padding: 0 16px; font-size: 15px; color: #1e293b; background: #ffffff; box-sizing: border-box; box-shadow: none; }
.ph { color: #9aa8bc; }
.input-error { border-color: #ef4444 !important; background: #fff8f8 !important; }
.field-hint-error { display: block; font-size: 12px; color: #ef4444; margin-top: 5px; font-weight: 500; }
.field-hint-checking { display: block; font-size: 12px; color: #94a3b8; margin-top: 5px; }

.strength-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; }
.strength-bars { display: flex; gap: 4px; flex: 1; }
.strength-bar { flex: 1; height: 4px; border-radius: 2px; transition: background 0.3s; }
.strength-label { font-size: 12px; font-weight: 600; white-space: nowrap; }

.code-row { display: flex; gap: 10px; align-items: center; }
.code-input { flex: 1; }
.btn-send-code { flex-shrink: 0; height: 52px; padding: 0 14px; background: #2457d6; border-radius: 14px; display: flex; align-items: center; justify-content: center; white-space: nowrap; box-shadow: var(--shadow-xs); }
.btn-send-code.is-disabled { background: #94a3b8; pointer-events: none; }
.btn-send-text { color: #ffffff; font-size: 13px; font-weight: 700; }

.forgot-row { text-align: right; margin-top: -4px; margin-bottom: 20px; }
.forgot-link { font-size: 13px; color: #2457d6; font-weight: 600; }

.agreement-row { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 18px; }
.checkbox { width: 20px; height: 20px; border-radius: 6px; flex-shrink: 0; border: 1.5px solid #b8c5d8; display: flex; align-items: center; justify-content: center; background: #ffffff; margin-top: 1px; }
.checked { background: #2457d6; border-color: #2457d6; }
.check-mark { font-size: 13px; color: #ffffff; font-weight: 700; }
.agreement-copy { display: flex; flex-wrap: wrap; align-items: center; row-gap: 2px; column-gap: 2px; flex: 1; }
.agreement-text { font-size: 12px; color: #64748b; line-height: 1.6; }
.link { color: #2457d6; font-weight: 600; font-size: 12px; }

.btn-primary { width: 100%; height: 54px; background: linear-gradient(135deg,#3568e8 0%,#2457d6 100%); border-radius: 16px; display: flex; align-items: center; justify-content: center; margin-bottom: 20px; box-shadow: 0 8px 18px rgba(37,99,235,0.22); transition: all 0.2s ease; }
.btn-text { color: #ffffff; font-size: 16px; font-weight: 700; }
.is-loading { opacity: 0.7; pointer-events: none; }
.is-disabled { opacity: 0.55; }
.btn-primary:active { opacity: 0.88; transform: scale(0.98); }

.divider-row { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; }
.divider-line { flex: 1; height: 1px; background: #dde5f0; }
.divider-text { font-size: 12px; color: #94a3b8; white-space: nowrap; }

.social-row { display: flex; gap: 12px; margin-bottom: 0; }
.btn-wechat { flex: 1; height: 48px; background: #07c160; border-radius: 14px; display: flex; align-items: center; justify-content: center; gap: 6px; box-shadow: var(--shadow-sm); }
.wx-badge { width: 20px; height: 20px; border-radius: 999px; background: rgba(255,255,255,0.92); display: flex; align-items: center; justify-content: center; }
.wx-badge-text { font-size: 11px; color: #07c160; font-weight: 800; }
.wx-text { color: #ffffff; font-size: 14px; font-weight: 700; }
.btn-guest { flex: 1; height: 48px; background: #eef2f8; border: 1px solid #dbe4f0; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.guest-text { color: #516176; font-size: 14px; font-weight: 700; }
.btn-guest:active { background: #e2e8f0; }
.btn-wechat:active { opacity: 0.88; }
.bottom-safe { height: calc(env(safe-area-inset-bottom, 0px) + 24px); }

/* ── Modals ── */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(15,23,42,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; padding: 0 var(--page-gutter); box-sizing: border-box; }
.modal-card { background: #ffffff; border-radius: 20px; padding: 24px 20px; width: 100%; max-width: 400px; border: 1px solid #dbe4f0; box-shadow: 0 20px 48px rgba(15,23,42,0.18); box-sizing: border-box; }
.modal-title { display: block; font-size: 18px; font-weight: 800; color: #1e293b; margin-bottom: 4px; }
.modal-hint { display: block; font-size: 13px; color: #64748b; line-height: 1.5; }
.modal-actions { display: flex; gap: 10px; margin-top: 20px; }
.modal-btn { flex: 1; height: 46px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; }
.modal-btn-cancel { background: #eef2f8; color: #475569; }
.modal-btn-confirm { background: linear-gradient(135deg,#3568e8 0%,#2457d6 100%); color: #ffffff; }
.modal-btn-confirm.is-loading { opacity: 0.7; pointer-events: none; }

.agreement-modal { max-height: 80vh; display: flex; flex-direction: column; }
.agreement-scroll { flex: 1; max-height: 55vh; margin: 12px 0; }
.agreement-section-title { display: block; font-size: 13px; font-weight: 700; color: #1e293b; margin-top: 14px; margin-bottom: 4px; }
.agreement-body { display: block; font-size: 12px; color: #64748b; line-height: 1.7; }

@media (max-width: 375px) {
  .hero {
    padding-left: var(--page-gutter-tight);
    padding-right: var(--page-gutter-tight);
    padding-bottom: 36px;
  }

  .hero-title {
    font-size: 27px;
  }

  .form-sheet {
    width: calc(100% - 24px);
    padding-left: 16px;
    padding-right: 16px;
  }

  .code-row,
  .social-row,
  .modal-actions {
    flex-direction: column;
  }

  .btn-send-code,
  .btn-wechat,
  .btn-guest,
  .modal-btn {
    width: 100%;
  }
}
</style>
