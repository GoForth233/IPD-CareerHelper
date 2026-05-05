<template>
  <view class="webview-container">
    <web-view v-if="url" :src="url" @error="handleError"></web-view>
    <view class="fallback" v-if="showFallback">
      <text class="fb-icon">🔗</text>
      <text class="fb-text">{{ t('webview.fallbackText') }}</text>
      <text class="fb-url">{{ url }}</text>
      <button class="btn-copy" @click="copyUrl">{{ t('webview.copyBtn') }}</button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { onLoad } from '@dcloudio/uni-app';

const { t } = useI18n();
const url = ref('');
const title = ref('');
const showFallback = ref(false);

onLoad((options: any) => {
  if (options.url) {
    url.value = decodeURIComponent(options.url);
  }
  if (options.title) {
    title.value = decodeURIComponent(options.title);
    uni.setNavigationBarTitle({ title: title.value });
  }
});

const handleError = () => {
  showFallback.value = true;
};

const copyUrl = () => {
  uni.setClipboardData({
    data: url.value,
    success: () => {
      uni.showToast({ title: t('webview.copied'), icon: 'success' });
    }
  });
};
</script>

<style scoped>
.webview-container {
  width: 100vw;
  height: 100vh;
  background: #f8fafc;
}

.fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  height: 100%;
  box-sizing: border-box;
}

.fb-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.fb-text {
  font-size: 16px;
  color: #1e293b;
  margin-bottom: 24px;
  text-align: center;
}

.fb-url {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 32px;
  text-align: center;
  word-break: break-all;
  background: #f1f5f9;
  padding: 12px;
  border-radius: 8px;
  width: 100%;
}

.btn-copy {
  background: #2563eb;
  color: #ffffff;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 600;
  width: 100%;
  height: 48px;
  line-height: 48px;
  border: none;
}
</style>
