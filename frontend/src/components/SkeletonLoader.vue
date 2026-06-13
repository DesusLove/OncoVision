<template>
  <span
    class="skeleton"
    :style="{ width, height, borderRadius: radius, display: 'inline-block' }"
    aria-hidden="true"
  ></span>
</template>

<script setup>
/**
 * Pulsing placeholder bar. Used wherever data is loading. The opacity
 * keyframe pulses 0.4 → 1 → 0.4 to feel like a breath.
 */
defineProps({
  width: { type: String, default: '100%' },
  height: { type: String, default: '14px' },
  radius: { type: String, default: 'var(--radius-sm)' },
});
</script>

<style scoped>
.skeleton {
  background: var(--bg-subtle);
  position: relative;
  overflow: hidden;
  animation: skeleton-pulse 1.4s ease-in-out infinite;
}
.skeleton::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--text-secondary) 6%, transparent), transparent);
  transform: translateX(-100%);
  animation: skeleton-shine 1.6s ease-in-out infinite;
}
@keyframes skeleton-pulse {
  0%, 100% { opacity: 0.55; }
  50%      { opacity: 1; }
}
@keyframes skeleton-shine {
  100% { transform: translateX(100%); }
}
@media (prefers-reduced-motion: reduce) {
  .skeleton { animation: none; }
  .skeleton::after { display: none; }
}
</style>
