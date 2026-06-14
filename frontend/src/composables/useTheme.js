import { ref, readonly } from 'vue';

/**
 * Theme state. The single source of truth for light/dark mode across
 * the app. Both the Navbar toggle button and the ⌘D command-palette
 * shortcut call `toggle()` — so they always stay in sync.
 *
 * The `.dark` class on <html> is the canonical CSS hook; `isDark`
 * mirrors whatever class is on the document. The pre-mount boot
 * script in index.html has already applied the saved class by the
 * time this composable runs, so we hydrate from the DOM rather than
 * re-reading localStorage here.
 */
const isDark = ref(typeof document !== 'undefined' && document.documentElement.classList.contains('dark'));

function apply(v) {
  document.documentElement.classList.toggle('dark', v);
}

export function useTheme() {
  return {
    isDark: readonly(isDark),
    /** Read the current state from the DOM. Useful on mount in case
     *  another component toggled the class before this one was ready. */
    syncFromDom() {
      isDark.value = document.documentElement.classList.contains('dark');
    },
    /** Flip the theme, apply the class, persist the choice. */
    toggle() {
      isDark.value = !isDark.value;
      apply(isDark.value);
      try { localStorage.setItem('oncovision-theme', isDark.value ? 'dark' : 'light'); } catch {}
    },
    /** Force a specific value (e.g. on first mount from a saved pref). */
    set(value) {
      isDark.value = !!value;
      apply(isDark.value);
      try { localStorage.setItem('oncovision-theme', isDark.value ? 'dark' : 'light'); } catch {}
    },
  };
}
