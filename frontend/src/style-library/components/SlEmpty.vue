<template>
  <view class="sl-empty">
    <text class="sl-empty__icon">{{ icon }}</text>
    <text class="sl-empty__title">{{ title }}</text>
    <text v-if="desc" class="sl-empty__desc">{{ desc }}</text>
    <slot />
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    icon?: string;
    title: string;
    desc?: string;
  }>(),
  {
    icon: '📭',
    desc: '',
  },
);
</script>

<style scoped>
.sl-empty {
  width: 100%;
  padding: 40px 20px;
  text-align: center;
}

.sl-empty__icon {
  font-size: 40px;
  display: block;
  margin-bottom: 10px;
}

.sl-empty__title {
  font-size: 15px;
  color: #334155;
  font-weight: 600;
}

.sl-empty__desc {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: #94a3b8;
}
</style>
