<template>
  <view class="sl-page" :class="[{ 'is-dark': dark }, customClass]">
    <slot />
  </view>
</template>

<script setup lang="ts">
defineProps<{
  dark?: boolean;
  customClass?: string;
}>();
</script>

<style scoped>
.sl-page {
  min-height: 100vh;
  background-color: var(--page-ios-gray, #f5f5f7);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Helvetica Neue', sans-serif;
}

.sl-page.is-dark {
  background-color: #0f172a;
}
</style>
