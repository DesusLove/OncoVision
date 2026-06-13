<template>
  <div class="app-card" :class="{ 'app-card--padded': padded, 'app-card--interactive': interactive }">
    <slot />
  </div>
</template>

<script setup>
/**
 * Generic surface card. Wraps content with --bg-card, border, radius
 * and the light-mode shadow. No opinions about internal layout.
 *
 *   padded       — adds 24px padding on all sides
 *   interactive  — adds hover lift + pointer cursor (use for clickable cards)
 *   as           — render as a different element (e.g. 'router-link', 'a', 'button')
 *   to           — router location; renders as <router-link> when as is unset
 */
defineProps({
  padded: { type: Boolean, default: false },
  interactive: { type: Boolean, default: false },
});
</script>

<style scoped>
.app-card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  /* Light-mode shadow only — dark mode keeps the border alone for
     a flatter look that matches GitHub/Linear-style dark UIs. */
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: transform 150ms ease-out, box-shadow 150ms ease-out, border-color 150ms ease-out;
}
.app-card--padded { padding: 24px; }
.app-card--interactive { cursor: pointer; }
.app-card--interactive:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.06), 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: var(--border-strong);
}
.app-card--interactive:focus-visible {
  outline: 2px solid var(--color-brand);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .app-card,
  .app-card--interactive:hover {
    transform: none;
    transition: none;
  }
}
</style>
