<template>
  <div v-if="target" class="modal" @click.self="$emit('cancel')">
    <div class="modal-card">
      <div class="modal-header">
        <h3>{{ t('verify_title') }}</h3>
        <button class="close-btn" @click="$emit('cancel')">&times;</button>
      </div>
      <div class="modal-body">
        <p style="color:var(--muted); font-size:14px; margin-bottom:12px">
          {{ target.patientPatientId || (target.patient ? target.patient.patientId : '') }} · {{ target.testDate }}
        </p>
        <div class="kv">
          <div><div class="k">{{ t('th_result') }}</div><div class="v">{{ target.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}</div></div>
          <div><div class="k">P(mal)</div><div class="v mono">{{ pct(target.binaryProbability) }}</div></div>
          <div><div class="k">{{ t('th_subtype') }}</div><div class="v">{{ pretty(target.subtypeLabel) }}</div></div>
        </div>
        <div style="margin-top:20px">
          <label class="f">{{ t('verify_correct_label') }}</label>
          <select class="in" v-model="correction">
            <option value="">{{ t('verify_as_is') }}</option>
            <option value="benign">{{ t('badge_benign') }}</option>
            <option value="malignant">{{ t('badge_malignant') }}</option>
          </select>
        </div>
      </div>
      <div class="modal-actions">
        <button class="btn ghost" @click="$emit('cancel')">{{ t('btn_cancel') }}</button>
        <button class="btn" @click="$emit('confirm', correction)">{{ t('verify_confirm') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { pct, pretty } from '../composables/format.js';

const props = defineProps({
  target: { type: Object, default: null },
  t: { type: Function, required: true },
});
defineEmits(['cancel', 'confirm']);

const correction = ref('');

// Reset the dropdown each time a new record is opened.
watch(() => props.target, () => { correction.value = ''; });
</script>
