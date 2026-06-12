<template>
  <div v-if="target" class="modal wide" @click.self="$emit('close')">
    <div class="modal-card">
      <div class="modal-header">
        <h3>{{ target.fullName }}</h3>
        <button class="close-btn" @click="$emit('close')">&times;</button>
      </div>
      <div class="modal-body">
        <div class="patient-details">
          <div class="detail-row"><span class="dk">{{ t('f_pid') }}</span><span class="dv mono">{{ target.patientId }}</span></div>
          <div class="detail-row"><span class="dk">{{ t('f_gender') }}</span><span class="dv">{{ target.gender }}</span></div>
          <div class="detail-row"><span class="dk">{{ t('f_pass') }}</span><span class="dv mono">{{ target.passportNumber }}</span></div>
        </div>
        <h4 style="margin:20px 0 10px">{{ t('pat_history_title') }}</h4>
        <div v-if="records.length" class="mini-table">
          <table>
            <thead><tr><th>{{ t('th_date') }}</th><th>{{ t('th_result') }}</th><th>P(mal)</th><th>{{ t('th_subtype') }}</th><th>{{ t('th_status') }}</th></tr></thead>
            <tbody>
              <tr v-for="r in records" :key="r.id">
                <td class="mono">{{ r.testDate }}</td>
                <td><span class="badge solid" :class="r.binaryLabel === 'malignant' ? 'mal' : 'ben'">{{ r.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}</span></td>
                <td class="mono">{{ pct(r.binaryProbability) }}</td>
                <td>{{ pretty(r.subtypeLabel) }}</td>
                <td>
                  <span v-if="r.verified" class="badge solid verified">{{ t('rec_verified') }}</span>
                  <button v-else class="icon-btn" :title="t('verify_title')" @click="$emit('verify', r)">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="20 6 9 17 4 12"/></svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-else style="color:var(--muted); font-size:14px">{{ t('pat_no_records') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { pct, pretty } from '../composables/format.js';
defineProps({
  target: { type: Object, default: null },
  records: { type: Array, default: () => [] },
  t: { type: Function, required: true },
});
defineEmits(['close', 'verify']);
</script>
