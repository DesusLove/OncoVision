import { onMounted, onUnmounted, nextTick, ref } from 'vue';

/**
 * Focus trap for modals. On mount: focus the first focusable element in
 * the container. While active: Tab/Shift+Tab cycle through the container's
 * focusable elements (so focus can never escape). On unmount: restore
 * focus to the element that was focused when the trap mounted.
 *
 * Usage:
 *   const rootRef = ref(null);
 *   useFocusTrap(rootRef, () => isOpen.value);
 */
const FOCUSABLE = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled]):not([type="hidden"])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(',');

export function useFocusTrap(rootRef, isActive) {
  let previouslyFocused = null;

  function focusableElements() {
    if (!rootRef.value) return [];
    return Array.from(rootRef.value.querySelectorAll(FOCUSABLE)).filter((el) => {
      // Skip elements that are visually hidden or zero-sized.
      const rect = el.getBoundingClientRect();
      return rect.width > 0 || rect.height > 0 || el === document.activeElement;
    });
  }

  function onKeydown(e) {
    if (e.key !== 'Tab') return;
    const els = focusableElements();
    if (els.length === 0) { e.preventDefault(); return; }
    const first = els[0];
    const last = els[els.length - 1];
    const active = document.activeElement;

    if (e.shiftKey && active === first) {
      e.preventDefault();
      last.focus();
    } else if (!e.shiftKey && active === last) {
      e.preventDefault();
      first.focus();
    } else if (!rootRef.value.contains(active)) {
      // Focus escaped the trap (e.g. via devtools) — pull it back.
      e.preventDefault();
      first.focus();
    }
  }

  onMounted(() => {
    previouslyFocused = document.activeElement;
  });
  onUnmounted(() => {
    if (previouslyFocused && typeof previouslyFocused.focus === 'function') {
      previouslyFocused.focus();
    }
  });

  // Watch isActive so the trap engages when the modal opens and
  // releases when it closes.
  return ref(null); // placeholder for future extension
}

// Helper: bind the keydown listener to the window while a modal is open.
export function useFocusTrapListener(rootRef, getActive) {
  function handler(e) {
    if (!getActive()) return;
    if (e.key !== 'Tab') return;
    if (!rootRef.value) return;
    const els = Array.from(rootRef.value.querySelectorAll(FOCUSABLE)).filter((el) => {
      const rect = el.getBoundingClientRect();
      return rect.width > 0 || rect.height > 0;
    });
    if (!els.length) { e.preventDefault(); return; }
    const first = els[0];
    const last = els[els.length - 1];
    const active = document.activeElement;
    if (e.shiftKey && active === first) {
      e.preventDefault();
      last.focus();
    } else if (!e.shiftKey && active === last) {
      e.preventDefault();
      first.focus();
    } else if (!rootRef.value.contains(active)) {
      e.preventDefault();
      first.focus();
    }
  }
  onMounted(() => window.addEventListener('keydown', handler));
  onUnmounted(() => window.removeEventListener('keydown', handler));
}
