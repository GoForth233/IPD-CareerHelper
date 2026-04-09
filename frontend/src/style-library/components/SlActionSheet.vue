<template>
  <view v-if="visible" class="sl-sheet">
    <view class="sl-sheet__mask" @click="onClose" />
    <view class="sl-sheet__panel">
      <view v-if="title" class="sl-sheet__title-wrap">
        <text class="sl-sheet__title">{{ title }}</text>
      </view>
      <view
        v-for="(item, idx) in options"
        :key="idx"
        class="sl-sheet__item"
        @click="onSelect(idx, item)"
      >
        <text>{{ item }}</text>
      </view>
      <view class="sl-sheet__cancel" @click="onClose">
        <text>取消</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean;
  title?: string;
  options: string[];
}>();

const emit = defineEmits<{
  (e: 'update:visible', v: boolean): void;
  (e: 'select', payload: { index: number; value: string }): void;
}>();

const onClose = () => emit('update:visible', false);

const onSelect = (index: number, value: string) => {
  emit('select', { index, value });
  emit('update:visible', false);
};
</script>

<style scoped>
.sl-sheet {
  position: fixed;
  inset: 0;
  z-index: 999;
}

.sl-sheet__mask {
  position: absolute;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
}

.sl-sheet__panel {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  background: #f8fafc;
  border-top-left-radius: 16px;
  border-top-right-radius: 16px;
  padding: 10px 12px calc(12px + env(safe-area-inset-bottom));
}

.sl-sheet__title-wrap {
  text-align: center;
  padding: 8px 0 10px;
}

.sl-sheet__title {
  font-size: 13px;
  color: #64748b;
}

.sl-sheet__item,
.sl-sheet__cancel {
  background: #fff;
  min-height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #0f172a;
}

.sl-sheet__item + .sl-sheet__item {
  margin-top: 8px;
}

.sl-sheet__cancel {
  margin-top: 10px;
  font-weight: 600;
}
</style>
