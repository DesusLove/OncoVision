<template>
  <Teleport to="body">
    <div class="toast-stack" aria-live="polite" aria-atomic="false">
      <transition-group name="toast">
        <div
          v-for="t in toasts"
          :key="t.id"
          class="toast"
          :class="`toast--${t.variant}`"
          role="status"
        >
          <span class="toast__icon" aria-hidden="true">
            <svg v-if="t.variant === 'success'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
            <svg v-else-if="t.variant === 'error'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
          </span>
          <span class="toast__text">{{ t.text }}</span>
          <button class="toast__close" type="button" :aria-label="'Dismiss'" @click="dismiss(t.id)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </transition-group>
    </div>
  </Teleport>
</template>

<script setup>
import { toasts, dismiss } from '../composables/useToast.js';
</script>

<style scoped>
.toast-stack {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}
.toast {
  pointer-events: auto;
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 280px;
  max-width: 420px;
  padding: 12px 14px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08), 0 16px 32px rgba(0, 0, 0, 0.08);
  color: var(--text-primary);
  font-size: 14px;
}
.toast__icon {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.toast__icon svg { width: 18px; height: 18px; }
.toast__text { flex: 1; line-height: 1.4; }
.toast__close {
  background: transparent;
  border: 0;
  width: 24px;
  height: 24px;
  border-radius: var(--radius-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  cursor: pointer;
  transition: background 150ms ease-out, color 150ms ease-out;
}
.toast__close:hover { background: var(--bg-subtle); color: var(--text-primary); }
.toast__close svg { width: 14px; height: 14px; }

.toast--success { border-left: 3px solid var(--color-benign); }
.toast--success .toast__icon { color: var(--color-benign); }
.toast--error   { border-left: 3px solid var(--color-malignant); }
.toast--error   .toast__icon { color: var(--color-malignant); }
.toast--info    { border-left: 3px solid var(--color-brand); }
.toast--info    .toast__icon { color: var(--color-brand); }

.toast-enter-active, .toast-leave-active { transition: all 200ms ease-out; }
.toast-enter-from { opacity: 0; transform: translateX(20px) translateY(8px); }
.toast-leave-to   { opacity: 0; transform: translateX(20px); }

@media (prefers-reduced-motion: reduce) {
  .toast-enter-active, .toast-leave-active { transition: none; }
}
</style>
