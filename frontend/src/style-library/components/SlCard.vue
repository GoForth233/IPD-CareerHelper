<template>
  <view class="sl-card" @click="emit('click')">
    <slot />
  </view>
</template>

<script setup lang="ts">
const emit = defineEmits<{
  (e: 'click'): void;
}>();
</script>

<style scoped>
.sl-card {
  background: #ffffff;
  border-radius: 16px;
}
</style>
