import { ref, onUnmounted } from 'vue';

/**
 * Slide-in toast notifications. Each toast auto-dismisses after 3.5s.
 * Tracks pending timeouts in a Set so `onUnmounted` (or HMR teardown)
 * can clear them all and avoid stray setTimeout firings on stale toasts.
 */
export function useToasts() {
  const toasts = ref([]);
  const pending = new Set();
  const TTL_MS = 3500;

  function addToast(text, type = 'success') {
    const id = (toasts.value.at(-1)?.id ?? 0) + 1;
    toasts.value.push({ id, text, type });
    const handle = setTimeout(() => {
      pending.delete(handle);
      dismissToast(id);
    }, TTL_MS);
    pending.add(handle);
  }

  function dismissToast(id) {
    toasts.value = toasts.value.filter((toast) => toast.id !== id);
  }

  function clearAll() {
    pending.forEach((h) => clearTimeout(h));
    pending.clear();
    toasts.value = [];
  }

  // Don't leak pending timers across HMR / route changes.
  onUnmounted(clearAll);

  return { toasts, addToast, dismissToast, clearAll };
}
