<template>
  <view class="sl-segment">
    <view
      v-for="(item, idx) in options"
      :key="idx"
      class="sl-segment__item"
      :class="{ 'is-active': idx === modelValue }"
      @click="emit('update:modelValue', idx)"
    >
      <text>{{ item }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: number;
  options: string[];
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void;
}>();
</script>

<style scoped>
.sl-segment {
  display: flex;
  padding: 3px;
  background: #e2e8f0;
  border-radius: 10px;
}

.sl-segment__item {
  flex: 1;
  min-height: 32px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 13px;
  color: #475569;
}

.sl-segment__item.is-active {
  background: #fff;
  color: #0f172a;
  font-weight: 600;
}
</style>
