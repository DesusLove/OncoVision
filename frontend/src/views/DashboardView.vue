<template>
  <section :class="{ active: active }">
    <div class="head">
      <h2>{{ t('dash_title') }}</h2>
      <p>{{ t('dash_sub') }}</p>
    </div>

    <!-- [A] STAT CARDS — 4 navigable metric tiles -->
    <div class="dash-stats">
      <component
        :is="stat.to ? 'router-link' : 'div'"
        v-for="(stat, i) in cards"
        :key="i"
        :to="stat.to || undefined"
        class="dash-stat"
        :class="[stat.cssClass, { 'dash-stat--clickable': !!stat.to }]"
        :role="stat.to ? 'link' : undefined"
        :tabindex="stat.to ? 0 : undefined"
      >
        <div class="dash-stat__head">
          <span class="dash-stat__icon" :class="stat.cssClass" aria-hidden="true">
            <!-- Total Patients: ti-users -->
            <svg v-if="stat.icon === 'users'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
            <!-- Total Diagnoses: ti-microscope -->
            <svg v-else-if="stat.icon === 'microscope'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 18h8"/><path d="M3 22h18"/><path d="M14 22a7 7 0 1 0 0-14h-1"/><path d="M9 14h2"/><path d="M9 12a2 2 0 0 1-2-2V6h6v4a2 2 0 0 1-2 2Z"/><path d="M12 6V3a1 1 0 0 0-1-1H9a1 1 0 0 0-1 1v3"/></svg>
            <!-- Malignant: ti-alert-circle -->
            <svg v-else-if="stat.icon === 'alert-circle'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
            <!-- Benign: ti-shield-check -->
            <svg v-else-if="stat.icon === 'shield-check'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/><polyline points="9 12 11 14 15 10"/></svg>
          </span>
        </div>
        <div class="dash-stat__value" :class="stat.cssClass">
          <SkeletonLoader v-if="loading" width="60%" height="32px" radius="var(--radius-sm)" />
          <AnimatedCount v-else :to="stat.raw" :duration="800" />
        </div>
        <div class="dash-stat__label">{{ stat.label }}</div>
        <span v-if="stat.to" class="dash-stat__chevron" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"/></svg>
        </span>
      </component>
    </div>

    <!-- [B] RECENT ACTIVITY + DONUT, side by side -->
    <div class="dash-grid">
      <AppCard class="dash-recent">
        <div class="dash-recent__head">
          <h3>{{ t('dash_recent') }}</h3>
          <span v-if="recent && recent.length" class="dash-recent__meta">{{ recent.length }} {{ t('dash_recent_meta') }}</span>
        </div>
        <div v-if="loading" class="dash-recent__skel">
          <SkeletonLoader v-for="i in 5" :key="i" width="100%" height="44px" radius="var(--radius-sm)" />
        </div>
        <table v-else-if="recent && recent.length" class="dash-recent__table">
          <thead>
            <tr>
              <th>{{ t('th_patient') }}</th>
              <th>{{ t('th_date') }}</th>
              <th>{{ t('th_result') }}</th>
              <th>{{ t('th_subtype') }}</th>
              <th>{{ t('th_confidence') }}</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in recent" :key="r.id">
              <td>
                <div class="dash-recent__patient">
                  <AvatarInitials :name="r.patient?.fullName || r.patientName || r.patientPatientId || '–'" :size="28" />
                  <span class="dash-recent__patient-id">{{ r.patientPatientId || (r.patient ? r.patient.patientId : '–') }}</span>
                </div>
              </td>
              <td class="dash-recent__date mono">{{ r.testDate }}</td>
              <td>
                <AppBadge :variant="r.binaryLabel === 'malignant' ? 'malignant' : 'benign'">
                  {{ r.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}
                </AppBadge>
              </td>
              <td>{{ pretty(r.subtypeLabel) }}</td>
              <td style="min-width: 100px;">
                <ConfidenceBar :value="r.binaryProbability" :variant="r.binaryLabel === 'malignant' ? 'malignant' : 'benign'" />
              </td>
              <td class="dash-recent__action">
                <button class="dash-recent__view" type="button" @click="$emit('view-record', r)">
                  {{ t('dash_view_link') }} →
                </button>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="dash-recent__empty">
          <svg viewBox="0 0 80 80" fill="none" class="dash-recent__empty-icon" aria-hidden="true">
            <rect x="16" y="8" width="48" height="64" rx="4" stroke="currentColor" stroke-width="2.5"/>
            <line x1="28" y1="28" x2="52" y2="28" stroke="currentColor" stroke-width="2"/>
            <line x1="28" y1="38" x2="52" y2="38" stroke="currentColor" stroke-width="2"/>
            <line x1="28" y1="48" x2="42" y2="48" stroke="currentColor" stroke-width="2"/>
          </svg>
          <p>{{ t('dash_no_recent') }}</p>
        </div>
      </AppCard>

      <DonutChart
        :loading="loading"
        :distribution="stats.subtypeDistribution || {}"
        :center="donutCenter"
        :center-sub="donutCenterSub"
        :t="t"
      />
    </div>

    <!-- [C] MODEL STRIP — single-row banner at the bottom of the page -->
    <div class="dash-model-strip">
      <span class="dash-model-strip__chip">AI Engine</span>
      <span class="dash-model-strip__text">
        <span class="dash-model-strip__cap">Powered by</span>
        <strong>ResNet-18</strong>
        <span class="dash-model-strip__sep">·</span>
        <span>Binary accuracy <strong>95.5%</strong></span>
        <span class="dash-model-strip__sep">·</span>
        <span>Malignant recall <strong>100%</strong></span>
        <span class="dash-model-strip__sep">·</span>
        <span>Trained on BreakHis <strong>200×</strong></span>
      </span>
      <span class="dash-model-strip__status" aria-hidden="true">
        <span class="dash-model-strip__dot"></span>
        <span>ML service live</span>
      </span>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue';
import AppCard from '../components/AppCard.vue';
import AppBadge from '../components/AppBadge.vue';
import AvatarInitials from '../components/AvatarInitials.vue';
import ConfidenceBar from '../components/ConfidenceBar.vue';
import SkeletonLoader from '../components/SkeletonLoader.vue';
import AnimatedCount from '../components/AnimatedCount.vue';
import DonutChart from '../components/DonutChart.vue';
import { pretty } from '../composables/format.js';

const props = defineProps({
  active: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  stats: { type: Object, default: () => ({}) },
  recent: { type: Array, default: () => [] },
  lastUpdated: { type: String, default: '' },
  t: { type: Function, required: true },
});
defineEmits(['view-record']);

const cards = computed(() => [
  { label: props.t('stat_patients'),  raw: props.stats.totalPatients  ?? 0, icon: 'users',         cssClass: 'brand',  to: { name: 'patients' } },
  { label: props.t('stat_diagnoses'), raw: props.stats.totalDiagnoses ?? 0, icon: 'microscope',    cssClass: 'ai',     to: { name: 'records' } },
  { label: props.t('stat_malignant'), raw: props.stats.malignant      ?? 0, icon: 'alert-circle',  cssClass: 'mal',    to: { name: 'records', query: { label: 'malignant' } } },
  { label: props.t('stat_benign'),    raw: props.stats.benign         ?? 0, icon: 'shield-check',  cssClass: 'ben',    to: { name: 'records', query: { label: 'benign' } } },
]);

const donutCenter = computed(() => {
  const d = props.stats;
  return d.totalDiagnoses ? String(d.totalDiagnoses) : '0';
});

const donutCenterSub = computed(() => {
  if (!props.stats.malignant && !props.stats.benign) return '';
  return props.stats.malignant + ' mal';
});
</script>

<style scoped>
/* === STAT CARDS === */
.dash-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.dash-stat {
  position: relative;
  display: block;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow);
  color: inherit;
  text-decoration: none;
  transition: transform 150ms ease-out, box-shadow 150ms ease-out, border-color 150ms ease-out;
}
.dash-stat--clickable { cursor: pointer; }
.dash-stat--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.04), 0 12px 24px rgba(0, 0, 0, 0.06);
  border-color: var(--border-strong);
}
.dash-stat--clickable:focus-visible {
  outline: 2px solid var(--color-brand);
  outline-offset: 2px;
}
.dash-stat__head {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
.dash-stat__icon {
  width: 38px;
  height: 38px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: var(--bg-subtle);
  color: var(--text-secondary);
}
.dash-stat__icon svg { width: 20px; height: 20px; }
.dash-stat__icon.brand { background: var(--color-brand-soft); color: var(--color-brand); }
.dash-stat__icon.ai    { background: var(--color-ai-soft);    color: var(--color-ai); }
.dash-stat__icon.mal   { background: var(--color-malignant-soft); color: var(--color-malignant); }
.dash-stat__icon.ben   { background: var(--color-benign-soft); color: var(--color-benign); }

.dash-stat__value {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.02em;
  line-height: 1.2;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-variant-numeric: tabular-nums;
}
.dash-stat__value.brand { color: var(--color-brand); }
.dash-stat__value.ai    { color: var(--color-ai); }
.dash-stat__value.mal   { color: var(--color-malignant); }
.dash-stat__value.ben   { color: var(--color-benign); }
.dash-stat__label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
}
.dash-stat__chevron {
  position: absolute;
  right: 14px;
  bottom: 14px;
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-sm);
  background: var(--bg-subtle);
  color: var(--text-muted);
  opacity: 0;
  transform: translateX(-4px);
  transition: opacity 150ms ease-out, transform 150ms ease-out, background 150ms ease-out, color 150ms ease-out;
}
.dash-stat__chevron svg { width: 14px; height: 14px; }
.dash-stat--clickable:hover .dash-stat__chevron,
.dash-stat--clickable:focus-visible .dash-stat__chevron {
  opacity: 1;
  transform: translateX(0);
  background: var(--color-brand-soft);
  color: var(--color-brand);
}

/* === MODEL STRIP === */
.dash-model-strip {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  padding: 10px 16px;
  margin-top: 18px;
  background: var(--bg-subtle);
  border: 1px dashed var(--border-strong);
  border-radius: var(--radius-md);
  font-size: 11px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.dash-model-strip__chip {
  background: var(--color-ai);
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 9999px;
  letter-spacing: 0.04em;
  text-transform: none;
  flex-shrink: 0;
}
.dash-model-strip__text {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
  flex-wrap: wrap;
}
.dash-model-strip__text strong {
  color: var(--text-primary);
  font-weight: 600;
}
.dash-model-strip__cap { font-weight: 600; color: var(--text-muted); }
.dash-model-strip__sep { color: var(--border-strong); margin: 0 2px; }
.dash-model-strip__status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  text-transform: none;
  letter-spacing: 0.02em;
  font-size: 11px;
  color: var(--text-secondary);
  flex-shrink: 0;
}
.dash-model-strip__dot {
  width: 8px;
  height: 8px;
  border-radius: 9999px;
  background: var(--color-benign);
  position: relative;
  flex-shrink: 0;
}
.dash-model-strip__dot::after {
  content: '';
  position: absolute;
  inset: -3px;
  border-radius: 9999px;
  background: var(--color-benign);
  opacity: 0.35;
  animation: status-pulse 2s ease-out infinite;
}
@keyframes status-pulse {
  0%   { transform: scale(0.6); opacity: 0.5; }
  100% { transform: scale(2.4); opacity: 0; }
}

/* === GRID === */
.dash-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

.dash-recent { padding: 20px; }
.dash-recent__head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 14px;
}
.dash-recent__head h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
.dash-recent__meta { font-size: 12px; color: var(--text-muted); }
.dash-recent__skel { display: flex; flex-direction: column; gap: 8px; }
.dash-recent__table { font-size: 13px; }
.dash-recent__table th { padding: 10px 12px; }
.dash-recent__table td { padding: 12px; }
.dash-recent__patient {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 500;
}
.dash-recent__patient-id { font-size: 12px; color: var(--text-secondary); font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
.dash-recent__date { font-size: 12px; color: var(--text-secondary); }
.dash-recent__action { text-align: right; }
.dash-recent__view {
  background: transparent;
  border: 0;
  color: var(--color-brand);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: background 150ms ease-out;
}
.dash-recent__view:hover { background: var(--color-brand-soft); }
.dash-recent__view:focus-visible { outline: 2px solid var(--color-brand); outline-offset: 2px; }

.dash-recent__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  text-align: center;
  color: var(--text-muted);
  font-size: 14px;
}
.dash-recent__empty-icon { width: 64px; height: 64px; opacity: 0.5; }

@media (max-width: 900px) {
  .dash-stats { grid-template-columns: repeat(2, 1fr); }
  .dash-grid { grid-template-columns: 1fr; }
}
@media (max-width: 480px) {
  .dash-stats { grid-template-columns: 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .dash-stat, .dash-stat--clickable:hover, .nav__logo { transform: none; animation: none; }
  .dash-model-strip__dot::after { animation: none; }
  @keyframes status-pulse { 0%, 100% { transform: scale(1); opacity: 0.5; } }
}
</style>
