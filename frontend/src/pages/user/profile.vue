<template>
  <view class="legacy-bridge">
    <text class="bridge-title">Redirecting to Profile</text>
    <text class="bridge-copy">This page has moved into the main profile tab.</text>
  </view>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

onMounted(() => {
  setTimeout(() => {
    uni.switchTab({ url: '/pages/user/index' });
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
