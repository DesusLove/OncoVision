<template>
  <section :class="{ active: active }">
    <!-- Header row: title + add button -->
    <div class="pat-head">
      <div>
        <h2>{{ t('pat_title') }}</h2>
        <p>{{ t('pat_sub') }}</p>
      </div>
      <button type="button" class="pat-add" @click="scrollToForm" :aria-label="t('btn_add')">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        {{ t('btn_add') }}
      </button>
    </div>

    <!-- Search bar -->
    <div class="pat-search-wrap">
      <svg class="pat-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
        <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
      </svg>
      <input
        type="search"
        class="pat-search"
        :value="search"
        :placeholder="t('pat_search')"
        @input="(e) => $emit('search-input', e.target.value)"
        @keyup.enter="$emit('search-submit', search)"
        :aria-label="t('pat_search')"
      />
    </div>

    <!-- Patient list -->
    <AppCard v-if="!loading && patients.length" class="pat-table-card">
      <table class="pat-table">
        <thead>
          <tr>
            <th class="pat-th--sortable" @click="$emit('sort', 'fullName')">{{ t('th_name') }} <span class="pat-sort-arrow">{{ arrow('fullName') }}</span></th>
            <th class="pat-th--sortable" @click="$emit('sort', 'patientId')">ID <span class="pat-sort-arrow">{{ arrow('patientId') }}</span></th>
            <th>{{ t('th_records') }}</th>
            <th>{{ t('th_actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in patients" :key="p.id">
            <td>
              <div class="pat-cell-patient">
                <AvatarInitials :name="p.fullName || p.patientId" :size="36" />
                <div class="pat-cell-patient__text">
                  <span class="pat-cell-patient__name">{{ p.fullName }}</span>
                  <span class="pat-cell-patient__sub">{{ p.gender }} · {{ p.passportNumber }}</span>
                </div>
              </div>
            </td>
            <td class="mono pat-cell-id">{{ p.patientId }}</td>
            <td>
              <AppBadge v-if="p.recordCount > 0" variant="neutral">
                {{ p.recordCount }} {{ p.recordCount === 1 ? t('rec_count_one') : t('rec_count_other') }}
              </AppBadge>
              <span v-else class="pat-cell-noresult">{{ t('pat_no_records') }}</span>
            </td>
            <td class="pat-cell-actions">
              <button type="button" class="pat-action" @click="$emit('view', p)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                {{ t('btn_view') }}
              </button>
              <button type="button" class="pat-action pat-action--danger" @click="askDelete(p)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                {{ t('btn_delete') }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </AppCard>

    <!-- Loading skeleton -->
    <AppCard v-else-if="loading" class="pat-table-card">
      <div class="pat-skel">
        <SkeletonLoader v-for="i in 5" :key="i" width="100%" height="56px" radius="var(--radius-md)" />
      </div>
    </AppCard>

    <!-- Empty state -->
    <AppCard v-else class="pat-empty-card">
      <div class="pat-empty">
        <svg viewBox="0 0 80 80" fill="none" class="pat-empty__icon" aria-hidden="true">
          <circle cx="40" cy="28" r="14" stroke="currentColor" stroke-width="2.5"/>
          <path d="M16 70c0-13 11-24 24-24s24 11 24 24" stroke="currentColor" stroke-width="2.5" stroke-linecap="round"/>
        </svg>
        <h4>{{ t('pat_empty_title') }}</h4>
        <p>{{ t('pat_empty_msg') }}</p>
        <button type="button" class="pat-add" @click="scrollToForm">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          {{ t('btn_add') }}
        </button>
      </div>
    </AppCard>

    <!-- Inline delete confirmation row -->
    <AppCard v-if="confirmDeleteId" class="pat-confirm-card">
      <div class="pat-confirm">
        <p>{{ t('del_confirm_msg').replace('{name}', confirmDeleteName) }}</p>
        <div class="pat-confirm__actions">
          <button type="button" class="pat-action" @click="confirmDeleteId = null">{{ t('btn_cancel') }}</button>
          <button type="button" class="pat-action pat-action--danger" @click="doDelete">{{ t('btn_delete') }}</button>
        </div>
      </div>
    </AppCard>

    <!-- Inline register/edit form -->
    <AppCard ref="formCard" class="pat-form-card">
      <h3 class="pat-form-title">{{ form.editingId ? t('pat_edit_title') : t('pat_register') }}</h3>
      <div class="grid2">
        <div><label class="f">{{ t('f_pid') }}</label><input v-model="form.pid" placeholder="P-002"></div>
        <div><label class="f">{{ t('f_name') }}</label><input v-model="form.name"></div>
        <div><label class="f">{{ t('f_gender') }}</label><select class="in" v-model="form.gender"><option value="FEMALE">{{ t('g_female') }}</option><option value="MALE">{{ t('g_male') }}</option><option value="OTHER">{{ t('g_other') }}</option></select></div>
        <div><label class="f">{{ t('f_pass') }}</label><input v-model="form.pass"></div>
      </div>
      <div class="pat-form-actions">
        <button type="button" class="btn" @click="form.editingId ? updatePatient() : addPatient()">{{ form.editingId ? t('btn_update') : t('btn_add') }}</button>
        <button v-if="form.editingId" type="button" class="btn ghost" @click="cancelEdit">{{ t('btn_cancel') }}</button>
      </div>
    </AppCard>
  </section>
</template>

<script setup>
import { reactive, ref, watch, nextTick } from 'vue';
import AppCard from '../components/AppCard.vue';
import AppBadge from '../components/AppBadge.vue';
import AvatarInitials from '../components/AvatarInitials.vue';
import SkeletonLoader from '../components/SkeletonLoader.vue';

const props = defineProps({
  active: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  patients: { type: Array, default: () => [] },
  page: { type: Number, default: 0 },
  pageObj: { type: Object, default: () => ({ number: 0, totalPages: 1, totalElements: 0, first: true, last: true }) },
  sort: { type: Object, default: () => ({ field: 'id', dir: 'asc' }) },
  search: { type: String, default: '' },
  t: { type: Function, required: true },
});
const emit = defineEmits(['sort', 'page', 'view', 'delete', 'add', 'update', 'cancel-edit', 'search-input', 'search-submit']);

const formCard = ref(null);

// Per-edit form: editingId=null means we're creating. Each new edit gets
// a fresh reactive so a previous edit's half-typed field never clobbers
// the new one and vice versa.
const form = reactive({ editingId: null, pid: '', name: '', gender: 'FEMALE', pass: '' });

// Inline delete confirmation (replaces the modal per Section 06 brief).
const confirmDeleteId = ref(null);
const confirmDeleteName = ref('');

function startEdit(p) {
  form.editingId = p.id;
  form.pid = p.patientId;
  form.name = p.fullName;
  form.gender = p.gender;
  form.pass = p.passportNumber;
}

function cancelEdit() {
  form.editingId = null;
  form.pid = ''; form.name = ''; form.gender = 'FEMALE'; form.pass = '';
  emit('cancel-edit');
}

function addPatient() { emit('add', { ...form }); resetForm(); }
function updatePatient() { emit('update', { ...form }); }

function resetForm() {
  form.editingId = null;
  form.pid = ''; form.name = ''; form.gender = 'FEMALE'; form.pass = '';
}

function askDelete(p) {
  confirmDeleteId.value = p.id;
  confirmDeleteName.value = p.fullName;
}
function doDelete() {
  const id = confirmDeleteId.value;
  confirmDeleteId.value = null;
  if (id) emit('delete', { id });
}

const arrow = (field) => props.sort.field === field ? (props.sort.dir === 'asc' ? '▲' : '▼') : '';

// Bubble the search input straight to parent (parent owns the debounce).
const localSearch = ref(props.search);
watch(() => props.search, (v) => { localSearch.value = v; });

const debouncedSearch = () => emit('search-input', localSearch.value);
const searchClicked = () => emit('search-submit', localSearch.value);

function scrollToForm() {
  nextTick(() => {
    if (formCard.value && formCard.value.$el) {
      formCard.value.$el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  });
}
</script>

<style scoped>
/* === HEADER === */
.pat-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 22px;
}
.pat-add {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  background: var(--color-brand);
  color: #fff;
  border: 0;
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease-out, transform 150ms ease-out;
}
.pat-add:hover { background: var(--color-brand-hover); transform: translateY(-1px); }
.pat-add svg { width: 16px; height: 16px; }

/* === SEARCH === */
.pat-search-wrap { position: relative; margin-bottom: 18px; }
.pat-search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  color: var(--text-muted);
  pointer-events: none;
}
.pat-search {
  width: 100%;
  height: 40px;
  padding: 0 14px 0 38px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font: inherit;
  font-size: 14px;
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out;
}
.pat-search::placeholder { color: var(--text-muted); }
.pat-search:focus {
  outline: 0;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px var(--color-brand-soft);
}

/* === TABLE === */
.pat-table-card { padding: 0; overflow: hidden; }
.pat-table { width: 100%; border-collapse: collapse; font-size: 14px; }
.pat-table th {
  text-align: left;
  font-size: 11px;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-secondary);
  padding: 14px;
  border-bottom: 1px solid var(--border);
  font-weight: 500;
}
.pat-th--sortable { cursor: pointer; transition: color 150ms ease-out; }
.pat-th--sortable:hover { color: var(--text-primary); }
.pat-sort-arrow { font-size: 10px; margin-left: 4px; opacity: 0.7; }
.pat-table td { padding: 14px; border-bottom: 1px solid var(--border); color: var(--text-primary); }
.pat-table tbody tr:last-child td { border-bottom: 0; }
.pat-table tbody tr { transition: background 150ms ease-out; }
.pat-table tbody tr:hover { background: var(--bg-subtle); }

.pat-cell-patient {
  display: flex;
  align-items: center;
  gap: 12px;
}
.pat-cell-patient__text { display: flex; flex-direction: column; line-height: 1.3; }
.pat-cell-patient__name { font-size: 14px; font-weight: 600; color: var(--text-primary); }
.pat-cell-patient__sub { font-size: 12px; color: var(--text-muted); margin-top: 2px; }
.pat-cell-id { color: var(--text-secondary); }
.pat-cell-noresult { font-size: 13px; color: var(--text-muted); }
.pat-cell-actions { text-align: right; white-space: nowrap; }

.pat-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: transparent;
  border: 0;
  padding: 6px 10px;
  border-radius: var(--radius-sm);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  color: var(--color-brand);
  cursor: pointer;
  transition: background 150ms ease-out;
}
.pat-action:hover { background: var(--color-brand-soft); }
.pat-action svg { width: 14px; height: 14px; }
.pat-action--danger { color: var(--color-malignant); }
.pat-action--danger:hover { background: var(--color-malignant-soft); }

/* === SKELETON === */
.pat-skel { display: flex; flex-direction: column; gap: 10px; padding: 14px; }

/* === EMPTY STATE === */
.pat-empty-card { padding: 0; }
.pat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  padding: 60px 24px;
  color: var(--text-muted);
}
.pat-empty__icon { width: 48px; height: 48px; opacity: 0.5; color: var(--text-muted); }
.pat-empty h4 { color: var(--text-primary); font-size: 18px; font-weight: 600; margin: 0; }
.pat-empty p { margin: 0; font-size: 14px; max-width: 320px; line-height: 1.6; }

/* === INLINE CONFIRM === */
.pat-confirm-card { margin-top: 14px; padding: 16px 20px; border-color: var(--color-malignant); }
.pat-confirm {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.pat-confirm p { margin: 0; font-size: 14px; color: var(--text-primary); }
.pat-confirm__actions { display: flex; gap: 6px; }

/* === FORM === */
.pat-form-card { margin-top: 24px; padding: 24px; max-width: 800px; }
.pat-form-title { font-size: 16px; font-weight: 600; margin: 0 0 14px; color: var(--text-primary); }
.pat-form-actions { display: flex; gap: 10px; margin-top: 16px; }

.btn {
  background: var(--color-brand);
  color: #fff;
  border: 0;
  border-radius: var(--radius-md);
  padding: 10px 18px;
  font: inherit;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease-out, transform 150ms ease-out;
}
.btn:hover { background: var(--color-brand-hover); transform: translateY(-1px); }
.btn.ghost { background: transparent; color: var(--text-primary); border: 1px solid var(--border); }
.btn.ghost:hover { background: var(--bg-subtle); transform: none; }

.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
label.f {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
input, select.in {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 14px;
  background: var(--bg-card);
  color: var(--text-primary);
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out;
}
input:focus, select.in:focus { outline: 0; border-color: var(--color-brand); box-shadow: 0 0 0 3px var(--color-brand-soft); }

@media (max-width: 760px) {
  .grid2 { grid-template-columns: 1fr; }
  .pat-confirm { flex-direction: column; align-items: flex-start; }
}
</style>
