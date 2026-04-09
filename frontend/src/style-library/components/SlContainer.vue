<template>
  <view class="sl-container" :style="styleObj">
    <slot />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    px?: string;
    py?: string;
  }>(),
  {
    px: '20px',
    py: '0',
  },
);

const styleObj = computed(() => ({
  paddingLeft: props.px,
  paddingRight: props.px,
  paddingTop: props.py,
  paddingBottom: props.py,
}));
</script>

<style scoped>
.sl-container {
  width: 100%;
  box-sizing: border-box;
}
</style>
