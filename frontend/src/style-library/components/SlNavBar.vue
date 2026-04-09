<template>
  <view class="sl-navbar" :style="{ paddingTop: safeTop + 'px' }">
    <view class="sl-navbar__inner">
      <view class="sl-navbar__left" @click="emit('back')">
        <slot name="left">
          <text v-if="showBack" class="sl-navbar__back">‹</text>
        </slot>
      </view>
      <view class="sl-navbar__center">
        <slot name="title">
          <text class="sl-navbar__title">{{ title }}</text>
        </slot>
      </view>
      <view class="sl-navbar__right">
        <slot name="right" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string;
    safeTop?: number;
    showBack?: boolean;
  }>(),
  {
    title: '',
    safeTop: 0,
    showBack: false,
  },
);

const emit = defineEmits<{
  (e: 'back'): void;
}>();
</script>

<style scoped>
.sl-navbar {
  width: 100%;
  background: transparent;
}

.sl-navbar__inner {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
}

.sl-navbar__left,
.sl-navbar__right {
  min-width: 44px;
  display: flex;
  align-items: center;
}

.sl-navbar__center {
  flex: 1;
  text-align: center;
  min-width: 0;
}

.sl-navbar__title {
  font-size: 17px;
  font-weight: 700;
  color: #0f172a;
}

.sl-navbar__back {
  font-size: 28px;
  line-height: 1;
  color: #0f172a;
}
</style>
