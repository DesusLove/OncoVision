<template>
  <section :class="{ active: active }">
    <div class="head">
      <h2>{{ t('rec_title') }}</h2>
      <p>{{ t('rec_sub') }}</p>
    </div>

    <!-- Filter bar -->
    <AppCard class="rec-filter">
      <div class="rec-filter__row">
        <div class="rec-filter__field">
          <label class="rec-filter__label">{{ t('th_result') }}</label>
          <select class="rec-filter__input" :value="filters.label" @change="(e) => $emit('update:filters', { ...filters, label: e.target.value })">
            <option value="">{{ t('all_labels') }}</option>
            <option value="malignant">{{ t('opt_malignant') }}</option>
            <option value="benign">{{ t('opt_benign') }}</option>
          </select>
        </div>
        <div class="rec-filter__field">
          <label class="rec-filter__label">{{ t('rec_from') }}</label>
          <input class="rec-filter__input" type="date" :value="filters.from" @input="(e) => $emit('update:filters', { ...filters, from: e.target.value })">
        </div>
        <div class="rec-filter__field">
          <label class="rec-filter__label">{{ t('rec_to') }}</label>
          <input class="rec-filter__input" type="date" :value="filters.to" @input="(e) => $emit('update:filters', { ...filters, to: e.target.value })">
        </div>
        <div class="rec-filter__actions">
          <button type="button" class="rec-filter__btn" @click="$emit('filter')">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3"/></svg>
            {{ t('btn_filter') }}
          </button>
          <button type="button" class="rec-filter__reset" @click="resetFilters">{{ t('btn_reset') }}</button>
        </div>
      </div>
    </AppCard>

    <!-- Records table -->
    <AppCard v-if="!loading && records.length" class="rec-table-card">
      <table class="rec-table">
        <thead>
          <tr>
            <th class="rec-th--sortable" @click="$emit('sort', 'testDate')">{{ t('th_date') }} <span class="rec-sort-arrow">{{ arrow('testDate') }}</span></th>
            <th>{{ t('th_patient') }}</th>
            <th class="rec-th--sortable" @click="$emit('sort', 'binaryLabel')">{{ t('th_result') }} <span class="rec-sort-arrow">{{ arrow('binaryLabel') }}</span></th>
            <th>{{ t('th_subtype') }}</th>
            <th>{{ t('th_confidence') }}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.id">
            <td class="mono rec-cell-date">{{ r.testDate }}</td>
            <td>
              <div class="rec-cell-patient">
                <AvatarInitials :name="r.patientName || r.patientPatientId || '–'" :size="28" />
                <span>{{ r.patientPatientId || (r.patient ? r.patient.patientId : '–') }}</span>
              </div>
            </td>
            <td>
              <AppBadge :variant="r.binaryLabel === 'malignant' ? 'malignant' : 'benign'">
                {{ r.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}
              </AppBadge>
            </td>
            <td>{{ pretty(r.subtypeLabel) }}</td>
            <td style="min-width: 110px;">
              <ConfidenceBar :value="r.binaryProbability" :variant="r.binaryLabel === 'malignant' ? 'malignant' : 'benign'" />
            </td>
            <td class="rec-cell-actions">
              <button type="button" class="rec-view" @click="$emit('view', r)">
                {{ t('rec_view_patient') }} →
              </button>
              <button v-if="!r.verified" type="button" class="rec-icon-btn" :title="t('verify_title')" :aria-label="t('verify_title')" @click="$emit('verify', r)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
              </button>
              <span v-else class="rec-verified" :title="r.correctedLabel ? `${t('rec_corrected_to')}: ${r.correctedLabel}` : ''">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                {{ t('rec_verified') }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </AppCard>

    <!-- Loading skeleton -->
    <AppCard v-else-if="loading" class="rec-table-card">
      <div class="rec-skel">
        <SkeletonLoader v-for="i in 5" :key="i" width="100%" height="56px" radius="var(--radius-md)" />
      </div>
    </AppCard>

    <!-- Empty state -->
    <AppCard v-else class="rec-empty-card">
      <div class="rec-empty">
        <svg viewBox="0 0 80 80" fill="none" class="rec-empty__icon" aria-hidden="true">
          <path d="M14 4h32l14 14v54a4 4 0 0 1-4 4H14a4 4 0 0 1-4-4V8a4 4 0 0 1 4-4z" stroke="currentColor" stroke-width="2.5"/>
          <line x1="42" y1="4" x2="42" y2="18" stroke="currentColor" stroke-width="2.5"/>
          <line x1="56" y1="18" x2="42" y2="18" stroke="currentColor" stroke-width="2.5"/>
          <line x1="20" y1="34" x2="50" y2="34" stroke="currentColor" stroke-width="2"/>
          <line x1="20" y1="44" x2="50" y2="44" stroke="currentColor" stroke-width="2"/>
          <line x1="20" y1="54" x2="40" y2="54" stroke="currentColor" stroke-width="2"/>
        </svg>
        <h4>{{ t('rec_none') }}</h4>
        <p>{{ t('rec_none_hint') }}</p>
      </div>
    </AppCard>

    <!-- Pagination -->
    <div class="rec-pager">
      <button type="button" class="rec-pager__btn" :disabled="pageObj.first" @click="$emit('page', page - 1)">{{ t('btn_prev') }}</button>
      <span class="rec-pager__info">{{ pginfo(t, pageObj) }}</span>
      <button type="button" class="rec-pager__btn" :disabled="pageObj.last" @click="$emit('page', page + 1)">{{ t('btn_next') }}</button>
    </div>
  </section>
</template>

<script setup>
import { watch } from 'vue';
import { useRoute } from 'vue-router';
import AppCard from '../components/AppCard.vue';
import AppBadge from '../components/AppBadge.vue';
import AvatarInitials from '../components/AvatarInitials.vue';
import ConfidenceBar from '../components/ConfidenceBar.vue';
import SkeletonLoader from '../components/SkeletonLoader.vue';
import { pretty, pginfo } from '../composables/format.js';

const props = defineProps({
  active: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  records: { type: Array, default: () => [] },
  filters: { type: Object, default: () => ({ label: '', from: '', to: '' }) },
  sort: { type: Object, default: () => ({ field: 'testDate', dir: 'desc' }) },
  page: { type: Number, default: 0 },
  pageObj: { type: Object, default: () => ({ number: 0, totalPages: 1, totalElements: 0, first: true, last: true }) },
  t: { type: Function, required: true },
});
const emit = defineEmits(['sort', 'page', 'view', 'verify', 'filter', 'update:filters', 'reset']);

const arrow = (field) => props.sort.field === field ? (props.sort.dir === 'asc' ? '▲' : '▼') : '';

// Deep-link support: when the URL carries ?label=... (e.g. clicking the
// "Malignant" stat card on the dashboard), apply it to the filter and
// refetch. Also react to back/forward navigation by watching the query.
const route = useRoute();
function applyLabelFromQuery() {
  const q = (route.query.label || '').toString().toLowerCase();
  const next = q === 'malignant' || q === 'benign' ? q : '';
  if (next !== (props.filters.label || '').toLowerCase()) {
    emit('update:filters', { ...props.filters, label: next });
    emit('filter');
  }
}
applyLabelFromQuery();
watch(() => route.query.label, applyLabelFromQuery);

function resetFilters() {
  emit('update:filters', { label: '', from: '', to: '' });
  emit('filter');
  emit('reset');
}
</script>

<style scoped>
/* === FILTER BAR === */
.rec-filter { padding: 16px; margin-bottom: 18px; }
.rec-filter__row {
  display: grid;
  grid-template-columns: 1.2fr 1fr 1fr auto;
  gap: 12px;
  align-items: end;
}
.rec-filter__field { display: flex; flex-direction: column; gap: 4px; }
.rec-filter__label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.rec-filter__input {
  height: 36px;
  padding: 0 14px;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font: inherit;
  font-size: 13px;
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out;
}
.rec-filter__input:focus {
  outline: 0;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px var(--color-brand-soft);
}
.rec-filter__actions { display: flex; align-items: center; gap: 8px; }
.rec-filter__btn {
  height: 36px;
  padding: 0 16px;
  background: var(--color-brand);
  color: #fff;
  border: 0;
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: background 150ms ease-out;
}
.rec-filter__btn:hover { background: var(--color-brand-hover); }
.rec-filter__btn svg { width: 14px; height: 14px; }
.rec-filter__reset {
  background: transparent;
  color: var(--text-muted);
  border: 0;
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
  transition: color 150ms ease-out;
}
.rec-filter__reset:hover { color: var(--color-brand); text-decoration: underline; text-underline-offset: 2px; }

@media (max-width: 760px) {
  .rec-filter__row { grid-template-columns: 1fr 1fr; }
  .rec-filter__actions { grid-column: 1 / -1; }
}

/* === TABLE === */
.rec-table-card { padding: 0; overflow: hidden; }
.rec-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.rec-table th {
  text-align: left;
  font-size: 11px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-secondary);
  padding: 12px 14px;
  border-bottom: 1px solid var(--border);
  font-weight: 500;
}
.rec-th--sortable { cursor: pointer; transition: color 150ms ease-out; }
.rec-th--sortable:hover { color: var(--text-primary); }
.rec-sort-arrow { font-size: 10px; margin-left: 4px; opacity: 0.7; }
.rec-table td { padding: 14px; border-bottom: 1px solid var(--border); color: var(--text-primary); vertical-align: middle; }
.rec-table tbody tr:last-child td { border-bottom: 0; }
.rec-table tbody tr { transition: background 150ms ease-out; }
.rec-table tbody tr:hover { background: var(--bg-subtle); }
.rec-cell-date { font-size: 13px; color: var(--text-secondary); }
.rec-cell-patient {
  display: flex;
  align-items: center;
  gap: 10px;
}
.rec-cell-actions { text-align: right; white-space: nowrap; display: flex; align-items: center; gap: 8px; justify-content: flex-end; }

.rec-view {
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
.rec-view:hover { background: var(--color-brand-soft); }
.rec-icon-btn {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-muted);
  cursor: pointer;
  transition: background 150ms ease-out, color 150ms ease-out, border-color 150ms ease-out;
}
.rec-icon-btn:hover { background: var(--bg-subtle); color: var(--text-primary); border-color: var(--border-strong); }
.rec-icon-btn svg { width: 13px; height: 13px; }
.rec-verified {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--color-benign);
  font-size: 12px;
  font-weight: 500;
}
.rec-verified svg { width: 12px; height: 12px; }

/* === SKELETON === */
.rec-skel { display: flex; flex-direction: column; gap: 10px; padding: 14px; }

/* === EMPTY STATE === */
.rec-empty-card { padding: 0; }
.rec-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  padding: 60px 24px;
  color: var(--text-muted);
}
.rec-empty__icon { width: 48px; height: 48px; opacity: 0.5; color: var(--text-muted); }
.rec-empty h4 { color: var(--text-primary); font-size: 16px; font-weight: 600; margin: 0; }
.rec-empty p { margin: 0; font-size: 14px; max-width: 360px; line-height: 1.6; }

/* === PAGINATION === */
.rec-pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px solid var(--border);
  font-size: 13px;
  color: var(--text-secondary);
}
.rec-pager__btn {
  height: 34px;
  padding: 0 14px;
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 150ms ease-out, border-color 150ms ease-out;
}
.rec-pager__btn:hover:not(:disabled) { background: var(--bg-subtle); border-color: var(--border-strong); }
.rec-pager__btn:disabled { opacity: 0.4; cursor: not-allowed; }
.rec-pager__info { font-weight: 500; }

@media (prefers-reduced-motion: reduce) {
  .rec-filter__btn, .rec-view, .rec-icon-btn, .rec-pager__btn { transition: none; }
}
</style>
