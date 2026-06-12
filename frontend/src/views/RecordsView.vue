<template>
  <section :class="{ active: active }">
    <div class="head"><h2>{{ t('rec_title') }}</h2><p>{{ t('rec_sub') }}</p></div>
    <div class="toolbar">
      <select class="in" :value="filters.label" @change="(e) => $emit('update:filters', { ...filters, label: e.target.value })">
        <option value="">{{ t('all_labels') }}</option>
        <option value="malignant">{{ t('opt_malignant') }}</option>
        <option value="benign">{{ t('opt_benign') }}</option>
      </select>
      <input type="date" :value="filters.from" @input="(e) => $emit('update:filters', { ...filters, from: e.target.value })">
      <input type="date" :value="filters.to" @input="(e) => $emit('update:filters', { ...filters, to: e.target.value })">
      <button class="btn" @click="$emit('filter')">{{ t('btn_filter') }}</button>
    </div>
    <div class="card">
      <template v-if="loading">
        <div class="pad"><div v-for="i in 5" :key="i" class="skeleton skeleton-row"></div></div>
      </template>
      <template v-else-if="!records.length">
        <div class="pad empty-state">
          <svg viewBox="0 0 80 80" fill="none" class="empty-illustration"><rect x="16" y="8" width="48" height="64" rx="4" stroke="var(--muted)" stroke-width="3"/><line x1="28" y1="28" x2="52" y2="28" stroke="var(--line)" stroke-width="2"/><line x1="28" y1="38" x2="52" y2="38" stroke="var(--line)" stroke-width="2"/><line x1="28" y1="48" x2="42" y2="48" stroke="var(--line)" stroke-width="2"/></svg>
          <p>{{ t('rec_none') }}</p>
        </div>
      </template>
      <template v-else>
        <table>
          <thead>
            <tr>
              <th class="sortable" @click="$emit('sort', 'testDate')">{{ t('th_date') }} <span class="sort-arrow">{{ arrow('testDate') }}</span></th>
              <th>{{ t('th_patient') }}</th>
              <th class="sortable" @click="$emit('sort', 'binaryLabel')">{{ t('th_result') }} <span class="sort-arrow">{{ arrow('binaryLabel') }}</span></th>
              <th>P(mal)</th>
              <th>{{ t('th_subtype') }}</th>
              <th>{{ t('th_status') }}</th>
              <th>{{ t('th_image') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td class="mono">{{ r.testDate }}</td>
              <td>{{ r.patientPatientId || (r.patient ? r.patient.patientId : '–') }}</td>
              <td>
                <span class="badge solid" :class="r.binaryLabel === 'malignant' ? 'mal' : 'ben'">
                  {{ r.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}
                </span>
              </td>
              <td class="mono">{{ pct(r.binaryProbability) }}</td>
              <td>{{ pretty(r.subtypeLabel) }}</td>
              <td>
                <span v-if="r.verified" class="badge solid verified"
                      :title="r.correctedLabel ? `${t('rec_corrected_to')}: ${r.correctedLabel}` : ''">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" width="10" height="10"><polyline points="20 6 9 17 4 12"/></svg>
                  {{ t('rec_verified') }}
                  <span v-if="r.correctedLabel" class="mono" style="opacity:.85;margin-left:4px">→ {{ r.correctedLabel }}</span>
                </span>
                <button v-else class="icon-btn" :title="t('verify_title')" @click="$emit('verify', r)">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                </button>
              </td>
              <td class="mono" style="font-size:12px">
                <div class="image-cell">
                  <svg class="img-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                  <span>{{ r.imageFilename || 'image.png' }}</span>
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
import { reactive } from 'vue';
import { pct, pretty, pginfo } from '../composables/format.js';

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
defineEmits(['sort', 'page', 'verify', 'filter']);

const arrow = (field) => props.sort.field === field ? (props.sort.dir === 'asc' ? '▲' : '▼') : '';
</script>
