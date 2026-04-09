<template>
  <view class="sl-list-item" @click="emit('click')">
    <view class="sl-list-item__left">
      <slot name="leading" />
      <view class="sl-list-item__text-wrap">
        <text class="sl-list-item__title">{{ title }}</text>
        <text v-if="subtitle" class="sl-list-item__subtitle">{{ subtitle }}</text>
      </view>
    </view>
    <view class="sl-list-item__right">
      <slot name="trailing" />
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  title: string;
  subtitle?: string;
}>();

const emit = defineEmits<{
  (e: 'click'): void;
}>();
</script>

<style scoped>
.sl-list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border: 1px solid var(--border-strong, #e2e8f0);
  border-radius: 16px;
  padding: 14px 16px;
}

.sl-list-item__left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.sl-list-item__text-wrap {
  min-width: 0;
}

.sl-list-item__title {
  font-size: 15px;
  color: #1e293b;
  font-weight: 600;
}

.sl-list-item__subtitle {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #94a3b8;
}

.sl-list-item__right {
  margin-left: 10px;
  flex-shrink: 0;
}
</style>
