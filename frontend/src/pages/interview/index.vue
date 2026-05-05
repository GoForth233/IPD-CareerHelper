<template>
  <view class="legacy-bridge">
    <text class="bridge-title">{{ t('common.loading') }}</text>
    <text class="bridge-copy"></text>
  </view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

onMounted(() => {
  setTimeout(() => {
    uni.redirectTo({ url: '/pages/interview/start' });
  }, 80);
});
</script>

<style scoped>
.legacy-bridge {
  min-height: 100vh;
  background: var(--page-ios-gray);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  text-align: center;
}

.bridge-title {
  display: block;
  font-size: 22px;
  font-weight: 800;
  color: var(--text-primary);
}

.bridge-copy {
  display: block;
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-secondary);
}
</style>
