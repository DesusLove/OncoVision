<template>
  <div class="conf">
    <div class="conf__track" :aria-valuenow="clamped" role="progressbar" aria-valuemin="0" aria-valuemax="100">
      <div class="conf__fill" :class="`conf__fill--${variant}`" :style="{ width: clamped + '%' }"></div>
    </div>
    <span class="conf__label">{{ clamped.toFixed(1) }}%</span>
  </div>
</template>

<script setup>
/**
 * Labeled horizontal confidence bar. Fill width animates from 0 to
 * `value` on mount (CSS width transition). Callers pass either a
 * 0..1 probability or a 0..100 percent — we auto-normalise.
 */
import { computed, onMounted, ref } from 'vue';

const props = defineProps({
  value: { type: Number, default: 0 },
  variant: { type: String, default: 'neutral', validator: (v) => ['malignant', 'benign', 'neutral'].includes(v) },
});

const mounted = ref(false);
const clamped = computed(() => {
  const v = props.value;
  if (v == null || Number.isNaN(v)) return 0;
  const pct = v <= 1 ? v * 100 : v;
  return Math.max(0, Math.min(100, pct));
});

onMounted(() => {
  // Defer one frame so the initial 0% width is committed before the
  // target width triggers the CSS transition.
  requestAnimationFrame(() => { mounted.value = true; });
});
</script>

<style scoped>
.conf {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}
.conf__track {
  flex: 1;
  height: 8px;
  background: var(--bg-subtle);
  border-radius: 9999px;
  overflow: hidden;
  position: relative;
  min-width: 60px;
}
.conf__fill {
  height: 100%;
  border-radius: 9999px;
  width: 0%;
  transition: width 600ms ease-out;
}
.conf__fill--malignant { background: var(--color-malignant); }
.conf__fill--benign    { background: var(--color-benign); }
.conf__fill--neutral   { background: var(--color-brand); }
.conf__label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  min-width: 48px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}
@media (prefers-reduced-motion: reduce) {
  .conf__fill { transition: none; }
}
</style>
