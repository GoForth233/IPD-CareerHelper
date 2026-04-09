<template>
  <button class="sl-button" :class="[`sl-button--${variant}`]" :disabled="disabled" @click="emit('click')">
    <slot>{{ text }}</slot>
  </button>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    text?: string;
    variant?: 'primary' | 'secondary' | 'ghost' | 'danger';
    disabled?: boolean;
  }>(),
  {
    text: '',
    variant: 'primary',
    disabled: false,
  },
);

const emit = defineEmits<{
  (e: 'click'): void;
}>();
</script>

<style scoped>
.sl-button {
  border: none;
  border-radius: 12px;
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  font-weight: 600;
  padding: 0 16px;
}

.sl-button--primary {
  background: #2563eb;
  color: #fff;
}

.sl-button--secondary {
  background: #e2e8f0;
  color: #1e293b;
}

.sl-button--ghost {
  background: transparent;
  color: #2563eb;
  border: 1px solid #93c5fd;
}

.sl-button--danger {
  background: #ef4444;
  color: #fff;
}

.sl-button[disabled] {
  opacity: 0.6;
}
</style>
