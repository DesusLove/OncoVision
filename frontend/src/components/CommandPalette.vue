<template>
  <div v-if="visible" class="palette">
    <div class="palette-input">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
      <input ref="paletteInput" v-model="query" :placeholder="placeholder" @keydown.escape="$emit('close')" @keydown.enter="runFirst">
    </div>
    <div class="palette-actions">
      <button v-for="a in filtered" :key="a.id" class="palette-item" @click="run(a)">
        <span class="palette-label">{{ a.label }}</span>
        <span class="palette-shortcut mono">{{ a.shortcut }}</span>
      </button>
      <p v-if="!filtered.length" style="color:var(--muted);padding:14px;font-size:13px">No matches.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue';

const props = defineProps({
  visible: { type: Boolean, default: false },
  actions: { type: Array, required: true },
  placeholder: { type: String, default: 'Search actions...' },
});
const emit = defineEmits(['close', 'run']);

const query = ref('');
const paletteInput = ref(null);

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase();
  if (!q) return props.actions;
  return props.actions.filter((a) => a.label.toLowerCase().includes(q));
});

async function run(a) {
  emit('close');
  query.value = '';
  await nextTick();
  emit('run', a);
}

function runFirst() {
  if (filtered.value[0]) run(filtered.value[0]);
}

defineExpose({ query, paletteInput });
</script>
