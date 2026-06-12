<template>
  <span>{{ display }}</span>
</template>

<script setup>
/**
 * Animated counter that eases to a target value over `duration` ms.
 * Cubic ease-out (1 - (1-t)^3) — feels punchy without overshooting.
 * Cancels the in-flight rAF on every props.to change so rapid updates
 * (e.g. a polling dashboard) don't pile up frames.
 */
import { ref, watch, onUnmounted, h, defineComponent } from 'vue';

const AnimatedCount = defineComponent({
  name: 'AnimatedCount',
  props: { to: { type: Number, default: 0 }, duration: { type: Number, default: 800 } },
  setup(props) {
    const display = ref(0);
    let raf = 0;
    let cancelled = false;

    const animate = () => {
      if (cancelled) return;
      const start = display.value;
      const target = props.to;
      const startTime = performance.now();
      const step = (now) => {
        if (cancelled) return;
        const t = Math.min((now - startTime) / props.duration, 1);
        const eased = 1 - Math.pow(1 - t, 3);
        display.value = Math.round(start + (target - start) * eased);
        if (t < 1) raf = requestAnimationFrame(step);
      };
      raf = requestAnimationFrame(step);
    };

    watch(() => props.to, () => {
      cancelAnimationFrame(raf);
      animate();
    }, { immediate: true });

    onUnmounted(() => {
      cancelled = true;
      cancelAnimationFrame(raf);
    });

    return () => h('span', String(display.value));
  },
});
</script>
