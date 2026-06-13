<template>
  <span class="app-badge" :class="`app-badge--${variant}`">
    <span class="app-badge__dot" aria-hidden="true"></span>
    <span class="app-badge__label"><slot /></span>
  </span>
</template>

<script setup>
/**
 * Pill badge with an 8px status dot + label. Variants pick the dot +
 * text colour; the background uses a 10% alpha tint so dark mode
 * reads correctly without a separate dark-theme override.
 */
defineProps({
  variant: {
    type: String,
    default: 'info',
    validator: (v) => ['malignant', 'benign', 'info', 'warning', 'neutral'].includes(v),
  },
});
</script>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.01em;
  line-height: 1.4;
  white-space: nowrap;
}
.app-badge__dot {
  width: 8px;
  height: 8px;
  border-radius: 9999px;
  flex-shrink: 0;
}
.app-badge__label {
  font-weight: 500;
}

/* Malignant — red */
.app-badge--malignant {
  background: color-mix(in srgb, var(--color-malignant) 12%, transparent);
  color: var(--color-malignant);
}
.app-badge--malignant .app-badge__dot { background: var(--color-malignant); }

/* Benign — green */
.app-badge--benign {
  background: color-mix(in srgb, var(--color-benign) 12%, transparent);
  color: var(--color-benign);
}
.app-badge--benign .app-badge__dot { background: var(--color-benign); }

/* Info — brand blue */
.app-badge--info {
  background: color-mix(in srgb, var(--color-brand) 12%, transparent);
  color: var(--color-brand);
}
.app-badge--info .app-badge__dot { background: var(--color-brand); }

/* Warning — amber */
.app-badge--warning {
  background: color-mix(in srgb, var(--color-warning) 14%, transparent);
  color: var(--color-warning);
}
.app-badge--warning .app-badge__dot { background: var(--color-warning); }

/* Neutral — muted */
.app-badge--neutral {
  background: var(--bg-subtle);
  color: var(--text-secondary);
}
.app-badge--neutral .app-badge__dot { background: var(--text-muted); }
</style>
