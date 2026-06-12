<template>
  <section :class="{ active: active }">
    <div class="head">
      <h2>{{ t('dash_title') }}</h2>
      <p>{{ t('dash_sub') }} <span class="updated" v-if="lastUpdated">{{ t('last_updated').replace('{t}', lastUpdated) }}</span></p>
    </div>
    <div class="stats">
      <StatCard
        v-for="(s, i) in cards"
        :key="i"
        :index="i"
        :label="s.label"
        :value="s.raw"
        :icon="s.icon"
        :css="s.css"
        :loading="loading"
      />
    </div>
    <DonutChart
      :loading="loading"
      :distribution="stats.subtypeDistribution || {}"
      :center="donutCenter"
      :center-sub="donutCenterSub"
      :t="t"
    />
  </section>
</template>

<script setup>
import { computed } from 'vue';
import StatCard from '../components/StatCard.vue';
import DonutChart from '../components/DonutChart.vue';

const props = defineProps({
  active: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  stats: { type: Object, default: () => ({}) },
  lastUpdated: { type: String, default: '' },
  t: { type: Function, required: true },
});

const cards = computed(() => {
  const d = props.stats;
  return [
    { label: props.t('stat_patients'), raw: d.totalPatients ?? 0, icon: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>', css: '' },
    { label: props.t('stat_diagnoses'), raw: d.totalDiagnoses ?? 0, icon: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>', css: '' },
    { label: props.t('stat_malignant'), raw: d.malignant ?? 0, icon: '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>', css: 'mal' },
    { label: props.t('stat_benign'), raw: d.benign ?? 0, icon: '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/>', css: 'ben' },
  ];
});

const donutCenter = computed(() => {
  const d = props.stats;
  return d.totalDiagnoses ? String(d.totalDiagnoses) : '0';
});

const donutCenterSub = computed(() => {
  if (!props.stats.malignant && !props.stats.benign) return '';
  return props.stats.malignant + ' mal';
});
</script>
