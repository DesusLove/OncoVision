<template>
  <span class="avatar" :style="{ background: bg, width: sizePx, height: sizePx, fontSize: textSize }" :title="name" aria-hidden="true">
    {{ initials }}
  </span>
</template>

<script setup>
/**
 * Circular initials avatar. Colour is deterministic from the first
 * letter (A-Z → one of 6 preset hues) so the same patient always
 * gets the same colour across page loads — never random.
 */
import { computed } from 'vue';

const props = defineProps({
  name: { type: String, default: '' },
  size: { type: Number, default: 36 }, // px
});

const PALETTE = [
  ['#1A56DB', '#3B82F6'], // brand blue
  ['#7C3AED', '#A78BFA'], // AI purple
  ['#059669', '#34D399'], // safe green
  ['#D97706', '#FBBF24'], // warning amber
  ['#DC2626', '#F87171'], // malignant red
  ['#0891B2', '#22D3EE'], // cyan
];

const seed = computed(() => (props.name || '').trim());
const initials = computed(() => {
  const s = seed.value;
  if (!s) return '?';
  const parts = s.split(/\s+/);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
});
const bg = computed(() => {
  if (!seed.value) return `linear-gradient(135deg, ${PALETTE[0][0]}, ${PALETTE[0][1]})`;
  let h = 0;
  for (let i = 0; i < seed.value.length; i++) h = (h * 31 + seed.value.charCodeAt(i)) >>> 0;
  const [a, b] = PALETTE[h % PALETTE.length];
  return `linear-gradient(135deg, ${a}, ${b})`;
});
const sizePx = computed(() => `${props.size}px`);
const textSize = computed(() => `${Math.max(11, Math.round(props.size * 0.36))}px`);
</script>

<style scoped>
.avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  color: #fff;
  font-weight: 600;
  letter-spacing: 0.02em;
  flex-shrink: 0;
  user-select: none;
}
</style>
