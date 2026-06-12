<template>
  <div class="stat card" :style="{ animationDelay: (0.1 * index) + 's' }">
    <template v-if="loading">
      <div class="skeleton skeleton-stat-icon"></div>
      <div class="skeleton skeleton-stat-value"></div>
      <div class="skeleton skeleton-stat-label"></div>
    </template>
    <template v-else>
      <div class="stat-header">
        <div class="label">{{ label }}</div>
        <div class="stat-icon-box" :class="css">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" v-html="icon"></svg>
        </div>
      </div>
      <div class="value" :class="css">
        <AnimatedCount :to="value" :duration="800" />
      </div>
    </template>
  </div>
</template>

<script setup>
import AnimatedCount from './AnimatedCount.vue';

defineProps({
  index: { type: Number, default: 0 },
  label: { type: String, required: true },
  value: { type: Number, default: 0 },
  icon: { type: String, required: true },
  css: { type: String, default: '' },
  loading: { type: Boolean, default: false },
});
</script>
