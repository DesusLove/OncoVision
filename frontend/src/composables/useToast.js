import { ref, onUnmounted } from 'vue';

/**
 * Global toast queue. Mounted once at the app root (see AppToast.vue);
 * any component can call useToast() to push a notification without
 * importing the container.
 *
 * Auto-dismiss after 4s (per the design brief). Tracks pending timers
 * in a Set so HMR / route changes don't leak setTimeout firings.
 */
const toasts = ref([]);
const pending = new Set();
const TTL_MS = 4000;
let idCounter = 1;

function dismiss(id) {
  toasts.value = toasts.value.filter((t) => t.id !== id);
}

function push(text, variant = 'info') {
  const id = idCounter++;
  toasts.value = [...toasts.value, { id, text, variant }];
  const handle = setTimeout(() => {
    pending.delete(handle);
    dismiss(id);
  }, TTL_MS);
  pending.add(handle);
  return id;
}

function clearAll() {
  pending.forEach((h) => clearTimeout(h));
  pending.clear();
  toasts.value = [];
}

export function useToast() {
  return {
    success: (text) => push(text, 'success'),
    error:   (text) => push(text, 'error'),
    info:    (text) => push(text, 'info'),
    push,
    dismiss,
    clearAll,
  };
}

// Single global instance. Components that call useToast() share the
// same queue; AppToast.vue renders it.
export { toasts, dismiss, clearAll };

// Don't leak pending timers on HMR / unmount in setups that use it.
onUnmounted(clearAll);
