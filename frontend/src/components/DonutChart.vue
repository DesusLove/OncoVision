<template>
  <div class="card pad" style="max-width: 800px;">
    <h3 style="margin-bottom:14px">{{ t('dash_subtypes') }}</h3>
    <div v-if="loading" style="display:flex;gap:24px;align-items:center">
      <div class="skeleton" style="width:180px;height:180px;border-radius:50%"></div>
      <div style="flex:1;display:flex;flex-direction:column;gap:12px">
        <div v-for="i in 4" :key="i" class="skeleton" style="height:16px;width:60%"></div>
      </div>
    </div>
    <div v-else-if="hasData" class="chart-section">
      <div class="donut-wrap">
        <svg viewBox="0 0 180 180" class="donut">
          <circle cx="90" cy="90" r="70" fill="none" stroke="var(--line)" stroke-width="20"/>
          <circle v-for="(seg, idx) in segments" :key="idx"
            cx="90" cy="90" r="70" fill="none"
            :stroke="seg.color" stroke-width="20"
            :stroke-dasharray="seg.dash"
            :stroke-dashoffset="seg.offset"
            :transform="'rotate(-90 90 90)'"
            style="transition: stroke-dasharray .8s, stroke-dashoffset .8s">
            <title>{{ seg.label }}: {{ seg.pct }}</title>
          </circle>
          <text x="90" y="86" text-anchor="middle" class="donut-center">{{ center }}</text>
          <text x="90" y="104" text-anchor="middle" class="donut-sub" v-if="centerSub">{{ centerSub }}</text>
        </svg>
      </div>
      <div class="donut-legend">
        <div v-for="(seg, idx) in segments" :key="idx" class="legend-item">
          <span class="legend-dot" :style="{ background: seg.color }"></span>
          <span class="legend-label">{{ pretty(seg.label) }}</span>
          <span class="legend-val mono">{{ seg.count }}</span>
          <span class="legend-pct mono">{{ seg.pct }}</span>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">
      <svg viewBox="0 0 80 80" fill="none" class="empty-illustration"><circle cx="40" cy="40" r="36" stroke="var(--line)" stroke-width="2"/><path d="M28 40h24M40 28v24" stroke="var(--muted)" stroke-width="3" stroke-linecap="round"/></svg>
      <p>{{ t('dash_none') }}</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { pretty } from '../composables/format.js';

const props = defineProps({
  loading: { type: Boolean, default: false },
  distribution: { type: Object, default: () => ({}) },
  center: { type: String, default: '0' },
  centerSub: { type: String, default: '' },
  t: { type: Function, required: true },
});

const COLORS = ['#d6447a', '#f472b6', '#a78bfa', '#60a5fa', '#34d399', '#fbbf24', '#fb923c', '#94a3b8'];

const hasData = computed(() => Object.keys(props.distribution || {}).length > 0);

const segments = computed(() => {
  const dist = props.distribution || {};
  const total = Object.values(dist).reduce((a, b) => a + b, 0) || 1;
  const entries = Object.entries(dist).sort((a, b) => b[1] - a[1]);
  let offset = 0;
  const circumference = 2 * Math.PI * 70;
  return entries.map(([label, count], i) => {
    const fraction = count / total;
    const dash = fraction * circumference;
    const seg = {
      label, count,
      pct: (fraction * 100).toFixed(1) + '%',
      color: COLORS[i % COLORS.length],
      dash: dash + ' ' + (circumference - dash),
      offset: -offset,
    };
    offset += dash;
    return seg;
  });
});
</script>
