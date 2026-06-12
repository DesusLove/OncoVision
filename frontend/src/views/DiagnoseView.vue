<template>
  <section :class="{ active: active }">
    <div class="head"><h2>{{ t('diag_title') }}</h2><p>{{ t('diag_sub') }}</p></div>
    <div class="card pad" style="max-width: 800px;">
      <div class="grid2">
        <div><label class="f">{{ t('d_patient') }}</label>
          <select class="in" v-model="form.patientId">
            <option v-for="p in patientOptions" :key="p.id" :value="p.id">{{ p.patientId }} - {{ p.fullName }}</option>
          </select>
        </div>
        <div><label class="f">{{ t('d_date') }}</label><input type="date" v-model="form.date"></div>
      </div>
      <div style="margin-top:24px">
        <label class="f">{{ t('d_image') }}</label>
        <div class="drop-zone" :class="{ dragging: isDragging, hasFile: !!preview }">
          <input type="file" id="d-file" @change="onFile" accept="image/*" class="file-hidden">
          <label for="d-file" class="drop-zone-content"
                 @dragover.prevent="isDragging = true"
                 @dragleave.prevent="isDragging = false"
                 @drop.prevent="onDrop">
            <template v-if="!preview">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="drop-icon"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
              <span class="drop-text">{{ t('drop_hint') }}</span>
            </template>
            <template v-else><img :src="preview" class="preview-img" alt="Preview"></template>
          </label>
        </div>
      </div>
      <button class="btn" :disabled="analyzing" style="margin-top:20px; min-width:180px" @click="$emit('analyze')">
        <template v-if="analyzing"><span class="spinner"></span> {{ t('analyzing') }}</template>
        <template v-else>{{ t('btn_analyze') }}</template>
      </button>
      <p class="note">{{ t('diag_note') }}</p>
      <div v-if="result" class="card pad result" :class="result.binaryLabel === 'malignant' ? 'mal' : 'ben'" style="animation: rise .4s ease both">
        <div class="result-header">
          <h3>{{ t('diag_output') }}</h3>
          <span class="badge large" :class="result.binaryLabel === 'malignant' ? 'mal' : 'ben'">
            {{ result.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}
          </span>
        </div>
        <div class="kv">
          <div><div class="k">{{ t('r_pmal') }}</div><div class="v mono highlight-pct" :class="result.binaryLabel === 'malignant' ? 'mal-color' : 'ben-color'">{{ pct(result.binaryProbability) }}</div></div>
          <div><div class="k">{{ t('r_subtype') }}</div><div class="v">{{ pretty(result.subtypeLabel) }}</div></div>
          <div><div class="k">{{ t('r_subconf') }}</div><div class="v mono">{{ pct(result.subtypeConfidence) }}</div></div>
        </div>
        <p class="note">{{ t('saved_record') }} #{{ result.id }} · {{ result.imageFilename }} · {{ result.testDate }}</p>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { pct, pretty } from '../composables/format.js';

const props = defineProps({
  active: { type: Boolean, default: false },
  patientOptions: { type: Array, default: () => [] },
  analyzing: { type: Boolean, default: false },
  result: { type: Object, default: null },
  t: { type: Function, required: true },
});
const emit = defineEmits(['analyze']);

const form = reactive({
  patientId: '',
  date: new Date().toISOString().slice(0, 10),
  imageFile: null,
});

const preview = ref(null);
const isDragging = ref(false);

function onFile(e) { setFile(e.target.files[0]); }
function onDrop(e) { isDragging.value = false; setFile(e.dataTransfer.files[0]); }

function setFile(file) {
  form.imageFile = file;
  preview.value = null;
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (ev) => { preview.value = ev.target.result; };
  reader.readAsDataURL(file);
}

defineExpose({ form, preview });
</script>
