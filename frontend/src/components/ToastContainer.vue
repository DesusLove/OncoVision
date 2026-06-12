<template>
  <div class="toast-container" aria-live="polite">
    <transition-group name="toast">
      <div v-for="toast in toasts" :key="toast.id" class="toast" :class="toast.type">
        <svg v-if="toast.type === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
        <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
        <span>{{ toast.text }}</span>
        <button class="toast-close" @click="$emit('dismiss', toast.id)">&times;</button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
defineProps({ toasts: { type: Array, required: true } });
defineEmits(['dismiss']);
</script>
