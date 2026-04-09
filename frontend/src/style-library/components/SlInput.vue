<template>
  <view class="sl-input-wrap">
    <text v-if="label" class="sl-input__label">{{ label }}</text>
    <input
      class="sl-input"
      :value="modelValue"
      :placeholder="placeholder"
      :password="password"
      @input="onInput"
      @confirm="emit('confirm')"
    />
  </view>
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    modelValue?: string;
    label?: string;
    placeholder?: string;
    password?: boolean;
  }>(),
  {
    modelValue: '',
    label: '',
    placeholder: '',
    password: false,
  },
);

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'confirm'): void;
}>();

const onInput = (e: any) => {
  emit('update:modelValue', e?.detail?.value ?? '');
};
</script>

<style scoped>
.sl-input-wrap {
  width: 100%;
}

.sl-input__label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #64748b;
}

.sl-input {
  height: 40px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #dbe2ea;
  padding: 0 12px;
  font-size: 14px;
  color: #0f172a;
}
</style>
