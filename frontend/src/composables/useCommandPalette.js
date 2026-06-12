import { ref, nextTick, computed } from 'vue';

/**
 * ⌘K-style command palette. Callers pass a factory for the action list so
 * the palette can include up-to-date translations and reactive dark-mode
 * labels (e.g. "Switch to light mode" / "Switch to dark mode").
 */
export function useCommandPalette(actionFactory) {
  const showPalette = ref(false);
  const paletteQuery = ref('');
  const paletteInput = ref(null);

  const actions = computed(() => actionFactory());

  const filtered = computed(() => {
    const q = paletteQuery.value.trim().toLowerCase();
    if (!q) return actions.value;
    return actions.value.filter((a) => a.label.toLowerCase().includes(q));
  });

  async function run(a) {
    showPalette.value = false;
    paletteQuery.value = '';
    await nextTick();
    a.action();
  }

  function runFirst() {
    const first = filtered.value[0];
    if (first) run(first);
  }

  async function open() {
    showPalette.value = true;
    await nextTick();
    paletteInput.value?.focus();
  }

  function close() {
    showPalette.value = false;
  }

  function toggle() {
    if (showPalette.value) close();
    else open();
  }

  return {
    showPalette, paletteQuery, paletteInput,
    filtered, run, runFirst, open, close, toggle,
  };
}
