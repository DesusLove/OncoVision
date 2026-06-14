<template>
  <AppCard class="trends-card">
    <div class="trends-card__head">
      <div>
        <h3>{{ t('dash_trends_title') }}</h3>
        <p class="trends-card__sub">{{ t('dash_trends_subtitle') }}</p>
      </div>
      <div v-if="!loading && trends.length" class="trends-card__legend">
        <span class="trends-card__legend-item"><span class="dot dot--mal"></span> Malignant</span>
        <span class="trends-card__legend-item"><span class="dot dot--ben"></span> Benign</span>
      </div>
    </div>
    <div v-if="loading" class="trends-card__skel">
      <SkeletonLoader width="100%" height="160px" radius="var(--radius-md)" />
    </div>
    <div v-else-if="!trends.length" class="trends-card__empty">
      <p>{{ t('rec_none') }}</p>
    </div>
    <svg v-else class="trends-card__svg" viewBox="0 0 600 200" preserveAspectRatio="none" role="img" :aria-label="t('dash_trends_title')">
      <!-- Gridlines -->
      <line v-for="i in 4" :key="'g' + i" :x1="0" :x2="600" :y1="i * 40" :y2="i * 40" stroke="var(--border)" stroke-dasharray="2 4" />
      <!-- Malignant line -->
      <polyline
        v-if="trends.length > 1"
        fill="none"
        stroke="var(--color-malignant)"
        stroke-width="2"
        :points="malPoints"
      />
      <!-- Benign line -->
      <polyline
        v-if="trends.length > 1"
        fill="none"
        stroke="var(--color-benign)"
        stroke-width="2"
        :points="benPoints"
      />
      <!-- Dots -->
      <circle
        v-for="(t, i) in trends"
        :key="'m' + i"
        :cx="xFor(i)"
        :cy="yFor(t.malignant)"
        r="3"
        fill="var(--color-malignant)"
      />
      <circle
        v-for="(t, i) in trends"
        :key="'b' + i"
        :cx="xFor(i)"
        :cy="yFor(t.benign)"
        r="3"
        fill="var(--color-benign)"
      />
      <!-- X-axis labels (every other month to avoid crowding) -->
      <text
        v-for="(t, i) in trends"
        :key="'l' + i"
        :x="xFor(i)"
        y="195"
        text-anchor="middle"
        font-size="9"
        fill="var(--text-muted)"
      >{{ t.month.slice(5) }}</text>
    </svg>
  </AppCard>
</template>

<script setup>
import { computed } from 'vue';
import AppCard from './AppCard.vue';
import SkeletonLoader from './SkeletonLoader.vue';

const props = defineProps({
  trends: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  t: { type: Function, default: (k) => k },
});

const MAX = computed(() => Math.max(1, ...props.trends.flatMap((t) => [t.malignant, t.benign, t.total])));

function xFor(i) {
  const n = Math.max(1, props.trends.length - 1);
  return 10 + (i / n) * 580;
}
function yFor(v) {
  const max = MAX.value;
  return 180 - (v / max) * 150 - 10;
}
const malPoints = computed(() => props.trends.map((t, i) => `${xFor(i)},${yFor(t.malignant)}`).join(' '));
const benPoints = computed(() => props.trends.map((t, i) => `${xFor(i)},${yFor(t.benign)}`).join(' '));
</script>

<style scoped>
.trends-card { padding: 20px; margin-top: 18px; }
.trends-card__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 12px;
}
.trends-card__head h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}
.trends-card__sub { font-size: 12px; color: var(--text-muted); margin: 2px 0 0; }
.trends-card__legend { display: flex; gap: 12px; font-size: 12px; color: var(--text-secondary); }
.trends-card__legend-item { display: inline-flex; align-items: center; gap: 6px; }
.dot { width: 8px; height: 8px; border-radius: 9999px; }
.dot--mal { background: var(--color-malignant); }
.dot--ben { background: var(--color-benign); }

.trends-card__svg { width: 100%; height: 200px; display: block; }
.trends-card__skel { padding: 4px; }
.trends-card__empty { padding: 40px 20px; text-align: center; color: var(--text-muted); font-size: 14px; }
</style>
