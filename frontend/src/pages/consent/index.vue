<template>
  <view class="consent-page" :class="[themeClass, fontClass]">
    <!-- App logo / brand header -->
    <view class="brand-header" v-if="!readonlyMode">
      <view class="brand-logo-wrap">
        <text class="brand-logo-text">CL</text>
      </view>
      <text class="brand-name">Career Loop</text>
      <text class="brand-slogan">{{ t('consent.slogan') }}</text>
    </view>

    <!-- Main consent card -->
    <view class="consent-card" v-if="!readonlyMode">
      <text class="consent-title">{{ t('consent.title') }}</text>
      <text class="consent-desc">{{ t('consent.desc') }}</text>

      <!-- Agreements list -->
      <view class="agree-list">
        <view class="agree-row" @click="openDoc('privacy')">
          <view class="agree-icon-wrap"><text class="agree-icon">🔒</text></view>
          <view class="agree-text-wrap">
            <text class="agree-title-text">{{ t('consent.privacyTitle') }}</text>
            <text class="agree-meta">{{ t('consent.privacyMeta') }}</text>
          </view>
          <text class="agree-arrow">›</text>
        </view>
        <view class="agree-divider"></view>
        <view class="agree-row" @click="openDoc('terms')">
          <view class="agree-icon-wrap"><text class="agree-icon">📋</text></view>
          <view class="agree-text-wrap">
            <text class="agree-title-text">{{ t('consent.termsTitle') }}</text>
            <text class="agree-meta">{{ t('consent.termsMeta') }}</text>
          </view>
          <text class="agree-arrow">›</text>
        </view>
      </view>

      <!-- Age confirmation (14+) -->
      <view class="check-row" @click="ageChecked = !ageChecked">
        <view class="checkbox" :class="{ checked: ageChecked }">
          <text v-if="ageChecked" class="check-mark">✓</text>
        </view>
        <text class="check-label">{{ t('consent.ageLabel') }}</text>
      </view>

      <!-- Agreement checkbox -->
      <view class="check-row" @click="termsChecked = !termsChecked">
        <view class="checkbox" :class="{ checked: termsChecked }">
          <text v-if="termsChecked" class="check-mark">✓</text>
        </view>
        <view class="check-label-wrap">
          <text class="check-label">{{ t('consent.agreeLabel') }} </text>
          <text class="check-link" @click.stop="openDoc('terms')">{{ t('consent.agreeTerms') }}</text>
          <text class="check-label"> {{ t('consent.agreeAnd') }} </text>
          <text class="check-link" @click.stop="openDoc('privacy')">{{ t('consent.agreePrivacy') }}</text>
        </view>
      </view>

      <!-- Action buttons -->
      <view class="btn-agree" :class="{ 'btn-disabled': !canAgree }" @click="onAgree">
        <text class="btn-agree-text">{{ t('consent.agreeBtn') }}</text>
      </view>
      <view class="btn-disagree" @click="onDisagree">
        <text class="btn-disagree-text">{{ t('consent.disagreeBtn') }}</text>
      </view>

      <text class="consent-footnote">
        {{ t('consent.footnote') }}
      </text>
    </view>

    <!-- Document viewer modal -->
    <view class="doc-modal-mask" v-if="showDoc" @tap="closeDoc">
      <view class="doc-modal" @tap.stop>
        <view class="doc-modal-header">
          <text class="doc-modal-title">{{ docType === 'terms' ? t('consent.termsTitle') : t('consent.privacyTitle') }}</text>
          <view class="doc-close-btn" @click="closeDoc"><text class="doc-close-icon">✕</text></view>
        </view>
        <scroll-view scroll-y class="doc-scroll">
          <!-- Privacy Policy content -->
          <view v-if="docType === 'privacy'">
            <text class="doc-section">1. Information We Collect</text>
            <text class="doc-body">We collect information you provide: nickname, email, profile details (school, major, graduation year), resume files, and interaction data with AI features. WeChat login collects your OpenID.</text>
            <text class="doc-body">我们会收集你主动提供的信息：昵称、邮箱、个人资料（学校、专业、毕业年份）、简历文件，以及你使用 AI 功能时产生的交互数据。微信登录会收集你的 OpenID。</text>
            <text class="doc-section">2. How We Use Your Information</text>
            <text class="doc-body">We use your data to provide career guidance, personalize your experience, analyze resumes with AI, send verification codes, and improve our services.</text>
            <text class="doc-body">我们使用这些信息为你提供职业指导、个性化体验、AI 简历分析、验证码发送以及服务改进。</text>
            <text class="doc-section">3. Data Storage &amp; Security</text>
            <text class="doc-body">Your data is stored on servers in mainland China. Resume files are encrypted via Aliyun OSS. Passwords are hashed with BCrypt and never stored in plain text.</text>
            <text class="doc-body">你的数据存储在中国大陆服务器。简历文件通过阿里云 OSS 加密传输与存储。密码使用 BCrypt 哈希处理，不会明文保存。</text>
            <text class="doc-section">4. Data Sharing</text>
            <text class="doc-body">We do not sell your data. We share data only with trusted providers (Aliyun storage, AI model providers) necessary to operate the service, under confidentiality obligations.</text>
            <text class="doc-body">我们不会出售你的个人数据。仅在服务运行必要时，与受信任的服务商（如阿里云存储、AI 模型服务商）共享必要数据，并要求其承担保密义务。</text>
            <text class="doc-section">5. Children's Privacy</text>
            <text class="doc-body">Career Loop is not intended for users under 14 years of age. By registering you confirm you are at least 14 years old. If we learn a user is under 14 we will delete their data promptly.</text>
            <text class="doc-body">Career Loop 不面向 14 周岁以下用户。注册或登录即表示你确认已满 14 周岁。如发现未满 14 周岁用户，我们将及时删除相关数据。</text>
            <text class="doc-section">6. Data Retention &amp; Deletion</text>
            <text class="doc-body">Your data is retained while your account is active. You may request account deletion in Profile → Settings → Delete Account. Personal data is fully removed within 30 days of your request.</text>
            <text class="doc-body">账号存续期间我们会保留必要数据。你可以在 Profile → 注销账号 提交删除申请，个人数据将在申请后 30 天内完成删除。</text>
            <text class="doc-section">7. Your Rights</text>
            <text class="doc-body">You have the right to access, correct, export, or delete your personal data. Contact support@careerloop.top to exercise these rights.</text>
            <text class="doc-body">你有权访问、更正、导出或删除个人数据。可通过 support@careerloop.top 联系我们行使相关权利。</text>
            <text class="doc-section">8. Updates to this Policy</text>
            <text class="doc-body">We may update this policy and will notify you via in-app notification. Continued use after updates constitutes acceptance.</text>
            <text class="doc-body">我们可能更新本政策，并通过站内通知等方式提示。更新后继续使用即表示你接受更新后的政策。</text>
          </view>
          <!-- Terms of Service content -->
          <view v-else>
            <text class="doc-section">1. Acceptance of Terms</text>
            <text class="doc-body">By using Career Loop you agree to these Terms of Service. If you do not agree, please do not use the application.</text>
            <text class="doc-body">使用 Career Loop 即表示你同意本用户协议。如不同意，请不要使用本应用。</text>
            <text class="doc-section">2. Age Requirement</text>
            <text class="doc-body">You must be at least 14 years of age to register and use Career Loop. By creating an account you confirm you meet this requirement.</text>
            <text class="doc-body">你必须年满 14 周岁才可注册和使用 Career Loop。创建账号即表示你确认符合该年龄要求。</text>
            <text class="doc-section">3. Use of Service</text>
            <text class="doc-body">Career Loop provides AI-powered career guidance, resume analysis, interview practice, and related tools. You agree to use the service only for lawful, personal purposes.</text>
            <text class="doc-body">Career Loop 提供 AI 职业指导、简历分析、模拟面试等工具。你同意仅为合法、个人目的使用本服务。</text>
            <text class="doc-section">4. Account Responsibility</text>
            <text class="doc-body">You are responsible for maintaining the confidentiality of your credentials. Notify us immediately of any unauthorized use at support@careerloop.top.</text>
            <text class="doc-body">你应妥善保管账号凭证。如发现未经授权使用，请立即通过 support@careerloop.top 联系我们。</text>
            <text class="doc-section">5. User Content</text>
            <text class="doc-body">You retain ownership of content you submit (e.g. resumes). By submitting content, you grant Career Loop a limited license to process it solely for providing the service to you.</text>
            <text class="doc-body">你保留所提交内容（如简历）的所有权。提交内容即表示你授权 Career Loop 仅为向你提供服务而进行必要处理。</text>
            <text class="doc-section">6. Prohibited Conduct</text>
            <text class="doc-body">You may not: violate any laws, infringe intellectual property rights, transmit harmful content, circumvent security measures, or access another user's account.</text>
            <text class="doc-body">你不得违反法律法规、侵犯知识产权、传播有害内容、绕过安全机制或访问他人账号。</text>
            <text class="doc-section">7. AI Disclaimer</text>
            <text class="doc-body">AI-generated career advice is for reference only and does not constitute professional career counseling. We make no guarantees about employment outcomes.</text>
            <text class="doc-body">AI 生成的职业建议仅供参考，不构成专业职业咨询。我们不对求职或就业结果作出保证。</text>
            <text class="doc-section">8. Modifications</text>
            <text class="doc-body">We may modify these terms at any time. Continued use after changes constitutes acceptance of the revised terms.</text>
            <text class="doc-body">我们可能随时修改本协议。修改后继续使用即表示你接受更新后的条款。</text>
            <text class="doc-section">9. Contact</text>
            <text class="doc-body">For questions: support@careerloop.top</text>
            <text class="doc-body">如有问题，请联系：support@careerloop.top</text>
          </view>
        </scroll-view>
        <view class="doc-modal-footer">
          <view class="doc-btn-close" @click="closeDoc"><text class="doc-btn-close-text">{{ t('consent.docCloseBtn') }}</text></view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useI18n } from '@/locales';
import { isLoggedIn, LOGIN_PAGE } from '@/utils/auth';
import request from '@/utils/request';
import { useTheme } from '@/utils/theme';

/** Bump this when a major policy update requires all users to re-consent. */
const AGREEMENT_VERSION = '1.0';

/** localStorage key includes the version so old keys are ignored on upgrade. */
const CONSENT_KEY = `consent_v${AGREEMENT_VERSION}`;

const ageChecked = ref(false);
const termsChecked = ref(false);
const showDoc = ref(false);
const docType = ref<'privacy' | 'terms'>('privacy');
const readonlyMode = ref(false);
const { t } = useI18n();
const { themeClass, fontClass, refresh: refreshTheme } = useTheme();

const canAgree = computed(() => ageChecked.value && termsChecked.value);

const openDoc = (type: 'privacy' | 'terms') => {
  docType.value = type;
  showDoc.value = true;
};

onMounted(() => {
  const pages = getCurrentPages();
  const current = pages[pages.length - 1];
  const viewParam = (current as any)?.options?.view as string | undefined;
  readonlyMode.value = (current as any)?.options?.readonly === '1';
  if (viewParam === 'privacy' || viewParam === 'terms') {
    openDoc(viewParam);
  }
});

const closeDoc = () => {
  showDoc.value = false;
  if (readonlyMode.value) {
    uni.navigateBack();
  }
};

/** Fire-and-forget: persist consent to server after login. */
const recordConsentOnServer = () => {
  if (!isLoggedIn()) return;
  request({
    url: '/consents',
    method: 'POST',
    data: {
      agreementVersion: AGREEMENT_VERSION,
      platform: 'miniprogram',
    },
  }).catch(() => {/* best-effort, non-blocking */});
};

const onAgree = () => {
  if (!canAgree.value) {
    uni.showToast({ title: 'Please check both boxes above', icon: 'none' });
    return;
  }
  uni.setStorageSync(CONSENT_KEY, '1');
  recordConsentOnServer();
  if (isLoggedIn()) {
    uni.switchTab({ url: '/pages/home/index' });
  } else if (!uni.getStorageSync('onboarding_v1_seen')) {
    // F20: First-time users see onboarding after accepting consent,
    // before they reach the login page.
    uni.reLaunch({ url: '/pages/onboarding/index' });
  } else {
    uni.reLaunch({ url: LOGIN_PAGE });
  }
};

const onDisagree = () => {
  uni.showModal({
    title: 'Exit App',
    content: 'You must accept the Terms of Service and Privacy Policy to use Career Loop. Are you sure you want to exit?',
    confirmText: 'Exit',
    cancelText: 'Go Back',
    success: (res) => {
      if (res.confirm) {
        // #ifdef MP-WEIXIN
        uni.exitMiniProgram({});
        // #endif
        // #ifdef H5
        window.history.back();
        // #endif
      }
    }
  });
};
</script>

<style scoped>
.consent-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #1e3a8a 0%, #1d4ed8 40%, #3b82f6 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16px 40px;
  box-sizing: border-box;
}

.brand-header {
  padding-top: 72px;
  padding-bottom: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.brand-logo-wrap {
  width: 64px;
  height: 64px;
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.brand-logo-text {
  font-size: 24px;
  font-weight: 900;
  color: #ffffff;
  letter-spacing: -1px;
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  color: #ffffff;
  letter-spacing: 0.3px;
}

.brand-slogan {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.72);
}

.consent-card {
  width: 100%;
  max-width: 480px;
  background: #ffffff;
  border-radius: 28px;
  padding: 28px 20px 24px;
  box-sizing: border-box;
  box-shadow: 0 20px 60px rgba(15, 23, 42, 0.22);
}

.consent-title {
  display: block;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
  margin-bottom: 10px;
}

.consent-desc {
  display: block;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
  margin-bottom: 20px;
}

/* Agreement list */
.agree-list {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 20px;
}

.agree-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
}

.agree-icon-wrap {
  width: 36px;
  height: 36px;
  background: #eff6ff;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.agree-icon { font-size: 17px; }

.agree-text-wrap { flex: 1; }

.agree-title-text {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
}

.agree-meta {
  display: block;
  font-size: 12px;
  color: #64748b;
  margin-top: 2px;
}

.agree-arrow {
  font-size: 20px;
  color: #94a3b8;
  font-weight: 300;
}

.agree-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 0 16px;
}

/* Checkboxes */
.check-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 14px;
}

.checkbox {
  width: 22px;
  height: 22px;
  min-width: 22px;
  border-radius: 7px;
  border: 2px solid #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  margin-top: 1px;
  transition: all 0.2s;
}

.checkbox.checked {
  background: #2563eb;
  border-color: #2563eb;
}

.check-mark {
  font-size: 13px;
  color: #ffffff;
  font-weight: 800;
}

.check-label {
  font-size: 13px;
  color: #334155;
  line-height: 1.55;
  flex: 1;
}

.check-label-wrap {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  row-gap: 1px;
  column-gap: 2px;
}

.check-highlight {
  color: #1d4ed8;
  font-weight: 700;
}

.check-link {
  color: #2563eb;
  font-weight: 600;
  font-size: 13px;
}

/* Buttons */
.btn-agree {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #3568e8 0%, #1d4ed8 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 8px;
  margin-bottom: 10px;
  box-shadow: 0 8px 20px rgba(29, 78, 216, 0.28);
  transition: opacity 0.2s;
}

.btn-agree.btn-disabled {
  background: #94a3b8;
  box-shadow: none;
  pointer-events: none;
}

.btn-agree-text {
  color: #ffffff;
  font-size: 16px;
  font-weight: 700;
}

.btn-disagree {
  width: 100%;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-disagree-text {
  font-size: 13px;
  color: #94a3b8;
}

.consent-footnote {
  display: block;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.5;
  text-align: center;
  margin-top: 14px;
}

/* Document modal */
.doc-modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: flex-end;
  z-index: 9999;
}

.doc-modal {
  width: 100%;
  height: 80vh;
  background: #ffffff;
  border-radius: 28px 28px 0 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.doc-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 20px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.doc-modal-title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.doc-close-btn {
  width: 32px;
  height: 32px;
  background: #f1f5f9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.doc-close-icon {
  font-size: 14px;
  color: #64748b;
  font-weight: 700;
}

.doc-scroll {
  flex: 1;
  padding: 0 20px;
  overflow-y: auto;
}

.doc-section {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: #1e293b;
  margin-top: 20px;
  margin-bottom: 6px;
}

.doc-body {
  display: block;
  font-size: 13px;
  line-height: 1.6;
  color: #475569;
}

.doc-modal-footer {
  padding: 16px 20px 24px;
  border-top: 1px solid #f1f5f9;
}

.doc-btn-close {
  width: 100%;
  height: 50px;
  background: #2563eb;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.doc-btn-close-text {
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
}
</style>
