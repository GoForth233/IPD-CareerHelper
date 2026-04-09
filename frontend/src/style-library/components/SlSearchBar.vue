<template>
  <view class="sl-search-bar">
    <view class="sl-search-bar__icon"><text>🔍</text></view>
    <input
      class="sl-search-bar__input"
      :value="modelValue"
      :placeholder="placeholder"
      @input="onInput"
      @confirm="emit('confirm')"
    />
    <view v-if="modelValue" class="sl-search-bar__clear" @click="clear">
      <text>×</text>
    </view>
  </view>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue?: string;
    placeholder?: string;
  }>(),
  {
    modelValue: '',
    placeholder: 'Search...'
  },
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'confirm'): void;
}>();

const onInput = (e: any) => emit('update:modelValue', e?.detail?.value ?? '');
const clear = () => emit('update:modelValue', '');
</script>

<style scoped>
.sl-search-bar {
  flex: 1;
  height: 38px;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 19px;
  padding: 0 12px;
  border: 1px solid #e2e8f0;
}

.sl-search-bar__icon {
  margin-right: 6px;
  font-size: 13px;
}

.sl-search-bar__input {
  flex: 1;
  height: 38px;
  font-size: 14px;
}

.sl-search-bar__clear {
  width: 20px;
  text-align: center;
  font-size: 18px;
  color: #94a3b8;
}
</style>
