<template>
  <text class="sl-tag" :class="`sl-tag--${variant}`">
    <slot>{{ text }}</slot>
  </text>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    text?: string;
    variant?: 'default' | 'primary' | 'success' | 'warning' | 'danger';
  }>(),
  {
    text: '',
    variant: 'default',
  },
);
</script>

<style scoped>
.sl-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.sl-tag--default {
  background: #f1f5f9;
  color: #64748b;
}

.sl-tag--primary {
  background: #dbeafe;
  color: #1d4ed8;
}

.sl-tag--success {
  background: #dcfce7;
  color: #15803d;
}

.sl-tag--warning {
  background: #ffedd5;
  color: #c2410c;
}

.sl-tag--danger {
  background: #fee2e2;
  color: #b91c1c;
}
</style>
