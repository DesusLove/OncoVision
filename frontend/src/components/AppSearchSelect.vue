<template>
  <div class="app-search-select" :class="{ 'app-search-select--open': open }" ref="rootEl">
    <button
      type="button"
      class="app-search-select__trigger"
      :aria-haspopup="true"
      :aria-expanded="open"
      :aria-label="ariaLabel || placeholder"
      @click="toggle"
      @keydown.down.prevent="openAndFocus"
      @keydown.enter.prevent="openAndFocus"
    >
      <span v-if="selectedLabel" class="app-search-select__value">{{ selectedLabel }}</span>
      <span v-else class="app-search-select__placeholder">{{ placeholder }}</span>
      <svg class="app-search-select__chevron" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="6 9 12 15 18 9"/></svg>
    </button>
    <div v-if="open" class="app-search-select__panel" role="listbox">
      <div class="app-search-select__search">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <input
          ref="searchInput"
          v-model="query"
          type="text"
          :placeholder="searchPlaceholder"
          @keydown.down.prevent="moveCursor(1)"
          @keydown.up.prevent="moveCursor(-1)"
          @keydown.enter.prevent="commitCursor"
          @keydown.escape="close"
        />
      </div>
      <ul class="app-search-select__list">
        <li v-if="!filtered.length" class="app-search-select__empty">{{ t ? t('pat_empty_msg') : 'No matches' }}</li>
        <li
          v-for="(opt, i) in filtered"
          :key="opt[valueKey]"
          class="app-search-select__option"
          :class="{ 'app-search-select__option--active': i === cursor }"
          role="option"
          :aria-selected="i === cursor"
          @mousedown.prevent="select(opt)"
          @mouseenter="cursor = i"
        >
          {{ opt[labelKey] }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
/**
 * Searchable combobox. Wraps a button trigger + a popover panel with an
 * input and a filtered option list. Keyboard: ↑/↓ to move, Enter to
 * commit, Escape to close. Click-outside closes the panel.
 */
import { ref, computed, watch, nextTick, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  modelValue: { default: null },
  options: { type: Array, default: () => [] },
  valueKey: { type: String, default: 'id' },
  labelKey: { type: String, default: 'label' },
  placeholder: { type: String, default: 'Select…' },
  searchPlaceholder: { type: String, default: 'Search…' },
  ariaLabel: { type: String, default: '' },
  t: { type: Function, default: null },
});
const emit = defineEmits(['update:modelValue', 'change']);

const open = ref(false);
const query = ref('');
const cursor = ref(0);
const searchInput = ref(null);
const rootEl = ref(null);

const selectedLabel = computed(() => {
  const found = props.options.find((o) => o[props.valueKey] === props.modelValue);
  return found ? found[props.labelKey] : '';
});

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return props.options;
  return props.options.filter((o) => String(o[props.labelKey]).toLowerCase().includes(q));
});

function toggle() {
  open.value = !open.value;
  if (open.value) {
    query.value = '';
    cursor.value = 0;
    nextTick(() => searchInput.value?.focus());
  }
}
function openAndFocus() {
  if (!open.value) toggle();
}
function close() { open.value = false; }
function select(opt) {
  emit('update:modelValue', opt[props.valueKey]);
  emit('change', opt);
  close();
}
function moveCursor(delta) {
  if (!filtered.value.length) return;
  cursor.value = (cursor.value + delta + filtered.value.length) % filtered.value.length;
}
function commitCursor() {
  if (filtered.value[cursor.value]) select(filtered.value[cursor.value]);
}

// Reset the cursor when the filter changes so the highlight stays in range.
watch(query, () => { cursor.value = 0; });
// Close on outside click.
function onDocClick(e) {
  if (!open.value) return;
  if (rootEl.value && !rootEl.value.contains(e.target)) close();
}
onMounted(() => document.addEventListener('mousedown', onDocClick));
onUnmounted(() => document.removeEventListener('mousedown', onDocClick));
</script>

<style scoped>
.app-search-select { position: relative; width: 100%; }
.app-search-select__trigger {
  width: 100%;
  height: 40px;
  padding: 0 36px 0 14px;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font: inherit;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out;
}
.app-search-select__trigger:hover { border-color: var(--border-strong); }
.app-search-select--open .app-search-select__trigger,
.app-search-select__trigger:focus-visible {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px var(--color-brand-soft);
  outline: 0;
}
.app-search-select__value { color: var(--text-primary); font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.app-search-select__placeholder { color: var(--text-muted); }
.app-search-select__chevron {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--text-muted);
  transition: transform 200ms ease-out;
}
.app-search-select--open .app-search-select__chevron { transform: translateY(-50%) rotate(180deg); }

.app-search-select__panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  z-index: 50;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  animation: app-search-select-in 150ms ease-out;
}
@keyframes app-search-select-in {
  from { opacity: 0; transform: translateY(-4px); }
  to { opacity: 1; transform: translateY(0); }
}
.app-search-select__search {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-bottom: 1px solid var(--border);
  color: var(--text-muted);
}
.app-search-select__search svg { width: 14px; height: 14px; flex-shrink: 0; }
.app-search-select__search input {
  flex: 1;
  border: 0;
  background: transparent;
  font: inherit;
  font-size: 14px;
  color: var(--text-primary);
  outline: 0;
}
.app-search-select__list {
  list-style: none;
  margin: 0;
  padding: 4px;
  max-height: 240px;
  overflow-y: auto;
}
.app-search-select__option {
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  color: var(--text-primary);
  cursor: pointer;
}
.app-search-select__option--active {
  background: var(--color-brand-soft);
  color: var(--color-brand);
}
.app-search-select__empty {
  padding: 16px;
  font-size: 13px;
  color: var(--text-muted);
  text-align: center;
}
</style>
