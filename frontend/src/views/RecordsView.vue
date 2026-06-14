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
          <button type="button" class="rec-filter__btn rec-filter__btn--ghost" @click="exportCsv" :disabled="exporting">
            <svg v-if="!exporting" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="14" height="14" aria-hidden="true">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="7 10 12 15 17 10"/>
              <line x1="12" y1="15" x2="12" y2="3"/>
            </svg>
            {{ exporting ? '...' : t('rec_export_csv') }}
          </button>
          <button type="button" class="rec-filter__reset" @click="resetFilters">{{ t('btn_reset') }}</button>
        </div>
      </div>
    </AppCard>

    <!-- Bulk action bar (visible when rows are selected) -->
    <Transition name="slide-down">
      <div v-if="selectedIds.length" class="rec-bulk">
        <span class="rec-bulk__count">{{ t('rec_selected_count').replace('{n}', String(selectedIds.length)) }}</span>
        <button type="button" class="rec-bulk__btn" @click="bulkVerify">{{ t('rec_bulk_verify') }}</button>
        <button type="button" class="rec-bulk__btn rec-bulk__btn--danger" @click="bulkDelete">{{ t('rec_bulk_delete') }}</button>
        <button type="button" class="rec-bulk__clear" @click="selectedIds = []">×</button>
      </div>
    </Transition>

    <!-- Records table -->
    <AppCard v-if="!loading && records.length" class="rec-table-card">
      <table class="rec-table">
        <thead>
          <tr>
            <th style="width: 36px;">
              <input type="checkbox" :checked="allSelected" @change="toggleAll" :aria-label="t('rec_select_all')">
            </th>
            <th class="rec-th--sortable" @click="$emit('sort', 'testDate')">{{ t('th_date') }} <span class="rec-sort-arrow">{{ arrow('testDate') }}</span></th>
            <th>{{ t('th_patient') }}</th>
            <th class="rec-th--sortable" @click="$emit('sort', 'binaryLabel')">{{ t('th_result') }} <span class="rec-sort-arrow">{{ arrow('binaryLabel') }}</span></th>
            <th>{{ t('th_subtype') }}</th>
            <th>{{ t('th_confidence') }}</th>
            <th>{{ t('th_status') }}</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in records" :key="r.id" :class="{ 'rec-row--selected': selectedIds.includes(r.id) }">
            <td>
              <input type="checkbox" :checked="selectedIds.includes(r.id)" @change="toggleOne(r.id, $event.target.checked)" :aria-label="`Select record ${r.id}`">
            </td>
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
            <td>
              <span v-if="r.verified" class="rec-verified" :title="r.correctedLabel ? `${t('rec_corrected_to')}: ${r.correctedLabel}` : ''">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                {{ t('rec_verified') }}
              </span>
              <button v-else type="button" class="rec-icon-btn" :title="t('verify_title')" :aria-label="t('verify_title')" @click="$emit('verify', r)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
              </button>
            </td>
            <td class="rec-cell-actions">
              <button type="button" class="rec-view" @click="openDetail(r)">
                {{ t('rec_view_patient') }} →
              </button>
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

    <!-- Record detail modal -->
    <RecordDetailModal
      v-if="detailRecord"
      :record="detailRecord"
      @close="detailRecord = null"
    />
  </section>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import AppCard from '../components/AppCard.vue';
import AppBadge from '../components/AppBadge.vue';
import AvatarInitials from '../components/AvatarInitials.vue';
import ConfidenceBar from '../components/ConfidenceBar.vue';
import SkeletonLoader from '../components/SkeletonLoader.vue';
import RecordDetailModal from '../components/RecordDetailModal.vue';
import { apiCall, apiDownload } from '../composables/useApi.js';
import { useToast } from '../composables/useToast.js';
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
const emit = defineEmits(['sort', 'page', 'verify', 'filter', 'update:filters', 'reset', 'view-record']);

const toast = useToast();
const arrow = (field) => props.sort.field === field ? (props.sort.dir === 'asc' ? '▲' : '▼') : '';

// === Bulk selection ===
const selectedIds = ref([]);
function toggleOne(id, checked) {
  if (checked) {
    if (!selectedIds.value.includes(id)) selectedIds.value.push(id);
  } else {
    selectedIds.value = selectedIds.value.filter((x) => x !== id);
  }
}
const allSelected = computed(() =>
  props.records.length > 0 && selectedIds.value.length === props.records.length
);
function toggleAll(e) {
  if (e.target.checked) {
    selectedIds.value = props.records.map((r) => r.id);
  } else {
    selectedIds.value = [];
  }
}

// Clear selection when records change (filter applied, page changed, etc.)
watch(() => props.records, () => { selectedIds.value = []; });

async function bulkVerify() {
  try {
    const { verified } = await apiCall('/api/records/bulk-verify', {
      method: 'POST',
      body: { ids: selectedIds.value },
    });
    toast.success(`Verified ${verified} record(s)`);
    selectedIds.value = [];
    emit('filter');
  } catch (e) { toast.error(e.message); }
}
async function bulkDelete() {
  if (!confirm(`Delete ${selectedIds.value.length} record(s)? This cannot be undone.`)) return;
  try {
    const { deleted } = await apiCall('/api/records/bulk-delete', {
      method: 'POST',
      body: { ids: selectedIds.value },
    });
    toast.success(`Deleted ${deleted} record(s)`);
    selectedIds.value = [];
    emit('filter');
  } catch (e) { toast.error(e.message); }
}

// === CSV export ===
const exporting = ref(false);
async function exportCsv() {
  exporting.value = true;
  try {
    const params = new URLSearchParams();
    if (props.filters.label) params.set('label', props.filters.label);
    if (props.filters.from)  params.set('from', props.filters.from);
    if (props.filters.to)    params.set('to', props.filters.to);
    const url = '/api/records/export.csv' + (params.toString() ? '?' + params : '');
    const res = await apiDownload(url);
    const blob = await res.blob();
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    const cd = res.headers.get('Content-Disposition') || '';
    const m = cd.match(/filename="([^"]+)"/);
    a.download = m ? m[1] : 'oncovision-records.csv';
    document.body.appendChild(a);
    a.click();
    a.remove();
    URL.revokeObjectURL(a.href);
    toast.success('Export downloaded');
  } catch (e) {
    toast.error(e.message);
  } finally {
    exporting.value = false;
  }
}

// === Record detail modal ===
const detailRecord = ref(null);
const route = useRoute();
function openDetail(r) { detailRecord.value = r; }

// Deep-link support: ?label=... applies the filter and fetches.
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
.rec-filter__btn:hover:not(:disabled) { background: var(--color-brand-hover); }
.rec-filter__btn--ghost { background: transparent; color: var(--text-secondary); border: 1px solid var(--border); }
.rec-filter__btn--ghost:hover:not(:disabled) { background: var(--bg-subtle); border-color: var(--border-strong); color: var(--text-primary); }
.rec-filter__btn:disabled { opacity: 0.5; cursor: not-allowed; }
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

/* === BULK BAR === */
.rec-bulk {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 12px;
  background: var(--color-brand-soft);
  border: 1px solid var(--color-brand);
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-primary);
}
.rec-bulk__count { font-weight: 600; }
.rec-bulk__btn {
  height: 30px;
  padding: 0 12px;
  background: transparent;
  color: var(--color-brand);
  border: 1px solid var(--color-brand);
  border-radius: var(--radius-sm);
  font: inherit;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease-out, color 150ms ease-out;
}
.rec-bulk__btn:hover { background: var(--color-brand); color: #fff; }
.rec-bulk__btn--danger { color: var(--color-malignant); border-color: var(--color-malignant); }
.rec-bulk__btn--danger:hover { background: var(--color-malignant); color: #fff; }
.rec-bulk__clear {
  margin-left: auto;
  background: transparent;
  border: 0;
  color: var(--text-secondary);
  font-size: 18px;
  cursor: pointer;
  padding: 0 8px;
}
.rec-bulk__clear:hover { color: var(--text-primary); }
.slide-down-enter-active, .slide-down-leave-active { transition: all 200ms ease-out; }
.slide-down-enter-from, .slide-down-leave-to { opacity: 0; transform: translateY(-8px); }

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
  background: transparent;
}
.rec-th--sortable { cursor: pointer; transition: color 150ms ease-out; }
.rec-th--sortable:hover { color: var(--text-primary); }
.rec-sort-arrow { font-size: 10px; margin-left: 4px; opacity: 0.7; }
.rec-table td { padding: 14px; border-bottom: 1px solid var(--border); color: var(--text-primary); vertical-align: middle; }
.rec-table tbody tr:last-child td { border-bottom: 0; }
.rec-table tbody tr { transition: background 150ms ease-out; }
.rec-table tbody tr:hover { background: var(--bg-subtle); }
.rec-row--selected { background: var(--color-brand-soft) !important; }
.rec-cell-date { font-size: 13px; color: var(--text-secondary); }
.rec-cell-patient { display: flex; align-items: center; gap: 10px; }
.rec-cell-actions { text-align: right; white-space: nowrap; }

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
  .rec-filter__btn, .rec-view, .rec-icon-btn, .rec-pager__btn,
  .rec-bulk, .slide-down-enter-active, .slide-down-leave-active { transition: none; }
}
</style>
