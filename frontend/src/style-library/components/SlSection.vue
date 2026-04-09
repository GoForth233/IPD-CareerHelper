<template>
  <view class="sl-section" :style="sectionStyle">
    <slot />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    pt?: string;
    pr?: string;
    pb?: string;
    pl?: string;
  }>(),
  {
    pt: '0',
    pr: '20px',
    pb: '0',
    pl: '20px',
  },
);

const sectionStyle = computed(() => ({
  paddingTop: props.pt,
  paddingRight: props.pr,
  paddingBottom: props.pb,
  paddingLeft: props.pl,
}));
</script>

<style scoped>
.sl-section {
  width: 100%;
  box-sizing: border-box;
}
</style>
