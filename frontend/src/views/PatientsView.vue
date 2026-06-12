<template>
  <section :class="{ active: active }">
    <div class="head">
      <h2>{{ t('pat_title') }}</h2>
      <p>{{ t('pat_sub') }}</p>
    </div>

    <div class="toolbar" style="max-width: 800px;">
      <input class="search" :value="search" :placeholder="t('pat_search')" @input="(e) => $emit('search-input', e.target.value)">
      <button class="btn" @click="searchClicked">{{ t('btn_search') }}</button>
    </div>

    <div class="card pad" style="margin-bottom:22px; max-width: 800px;">
      <h3 style="margin-bottom:14px">{{ form.editingId ? t('pat_edit_title') : t('pat_register') }}</h3>
      <div class="grid2">
        <div><label class="f">{{ t('f_pid') }}</label><input v-model="form.pid" placeholder="P-002"></div>
        <div><label class="f">{{ t('f_name') }}</label><input v-model="form.name"></div>
        <div><label class="f">{{ t('f_gender') }}</label><select class="in" v-model="form.gender"><option value="FEMALE">{{ t('g_female') }}</option><option value="MALE">{{ t('g_male') }}</option><option value="OTHER">{{ t('g_other') }}</option></select></div>
        <div><label class="f">{{ t('f_pass') }}</label><input v-model="form.pass"></div>
      </div>
      <div style="margin-top:16px; display:flex; gap:10px">
        <button class="btn" @click="form.editingId ? updatePatient() : addPatient()">{{ form.editingId ? t('btn_update') : t('btn_add') }}</button>
        <button v-if="form.editingId" class="btn ghost" @click="cancelEdit">{{ t('btn_cancel') }}</button>
      </div>
    </div>

    <div class="card">
      <template v-if="loading">
        <div class="pad"><div v-for="i in 5" :key="i" class="skeleton skeleton-row"></div></div>
      </template>
      <template v-else-if="!patients.length">
        <div class="pad empty-state">
          <svg viewBox="0 0 80 80" fill="none" class="empty-illustration"><circle cx="40" cy="24" r="12" stroke="var(--muted)" stroke-width="3"/><path d="M20 64c0-11 9-20 20-20s20 9 20 20" stroke="var(--muted)" stroke-width="3" stroke-linecap="round"/></svg>
          <p>{{ t('pat_none') }}</p>
        </div>
      </template>
      <template v-else>
        <table>
          <thead>
            <tr>
              <th class="sortable" @click="$emit('sort', 'patientId')">ID <span class="sort-arrow">{{ arrow('patientId') }}</span></th>
              <th class="sortable" @click="$emit('sort', 'fullName')">{{ t('th_name') }} <span class="sort-arrow">{{ arrow('fullName') }}</span></th>
              <th>{{ t('th_gender') }}</th>
              <th>{{ t('th_pass') }}</th>
              <th style="text-align:center">{{ t('th_records') }}</th>
              <th style="text-align:right">{{ t('th_actions') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in patients" :key="p.id">
              <td class="mono">{{ p.patientId }}</td>
              <td>{{ p.fullName }}</td>
              <td>{{ p.gender }}</td>
              <td class="mono">{{ p.passportNumber }}</td>
              <td style="text-align:center" class="mono">{{ p.recordCount ?? '–' }}</td>
              <td style="text-align:right">
                <div class="table-actions">
                  <button class="icon-btn" :title="t('btn_view')" @click="$emit('view', p)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg></button>
                  <button class="icon-btn" :title="t('btn_edit')" @click="startEdit(p)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg></button>
                  <button class="icon-btn danger" :title="t('btn_delete')" @click="$emit('delete', p)"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </template>
    </div>

    <div class="pager">
      <button class="btn ghost" :disabled="pageObj.first" @click="$emit('page', page - 1)">{{ t('btn_prev') }}</button>
      <span>{{ pginfo(t, pageObj) }}</span>
      <button class="btn ghost" :disabled="pageObj.last" @click="$emit('page', page + 1)">{{ t('btn_next') }}</button>
    </div>
  </section>
</template>

<script setup>
/**
 * Patients tab. The form is a per-edit `reactive` so that starting a
 * different edit doesn't race with the user still typing in the previous
 * one (App.vue#editPatient used to mutate a single shared `pForm`).
 */
import { reactive, ref, watch } from 'vue';
import { pginfo } from '../composables/format.js';

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

// Per-edit form: editingId=null means we're creating. Each new edit gets
// a fresh reactive so a previous edit's half-typed field never clobbers
// the new one and vice versa.
const form = reactive({ editingId: null, pid: '', name: '', gender: 'FEMALE', pass: '' });

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

const arrow = (field) => props.sort.field === field ? (props.sort.dir === 'asc' ? '▲' : '▼') : '';

// Bubble the search input straight to parent (parent owns the debounce).
const localSearch = ref(props.search);
watch(() => props.search, (v) => { localSearch.value = v; });

const debouncedSearch = () => emit('search-input', localSearch.value);
const searchClicked = () => emit('search-submit', localSearch.value);
</script>
