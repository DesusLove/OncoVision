<template>
  <section :class="{ active: active }">
    <div class="head">
      <h2>{{ t('diag_title') }}</h2>
      <p>{{ t('diag_sub') }}</p>
    </div>

    <div class="diag-grid">
      <!-- LEFT PANEL: patient + upload + run -->
      <AppCard class="diag-left">
        <div class="diag-field">
          <label class="diag-field__label">{{ t('d_patient') }}</label>
          <AppSearchSelect
            v-model="form.patientId"
            :options="selectOptions"
            value-key="id"
            label-key="displayLabel"
            :placeholder="t('d_patient_placeholder')"
            :search-placeholder="t('pat_search')"
            :aria-label="t('d_patient')"
            :t="t"
          />
        </div>

        <div class="diag-field">
          <label class="diag-field__label">{{ t('d_image') }}</label>
          <div class="diag-drop" :class="{ 'diag-drop--dragging': isDragging, 'diag-drop--has': !!preview }">
            <input type="file" id="d-file" @change="onFile" accept="image/*" class="diag-drop__hidden" :aria-label="t('d_image')">
            <label for="d-file" class="diag-drop__zone"
                   @dragover.prevent="isDragging = true"
                   @dragleave.prevent="isDragging = false"
                   @drop.prevent="onDrop">
              <template v-if="!preview">
                <span class="diag-drop__icon" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M6 18h8"/><path d="M3 22h18"/>
                    <path d="M14 22a7 7 0 1 0 0-14h-1"/>
                    <path d="M9 14h2"/><path d="M9 12a2 2 0 0 1-2-2V6h6v4a2 2 0 0 1-2 2Z"/>
                    <path d="M12 6V3a1 1 0 0 0-1-1H9a1 1 0 0 0-1 1v3"/>
                  </svg>
                </span>
                <span class="diag-drop__primary">{{ t('drop_hint') }}</span>
                <span class="diag-drop__secondary">{{ t('drop_sub') }}</span>
                <button type="button" class="diag-drop__browse" @click.prevent>{{ t('diag_browse') }}</button>
              </template>
              <template v-else>
                <img :src="preview" class="diag-drop__preview" alt="Selected image preview">
              </template>
            </label>
          </div>
          <div v-if="preview" class="diag-drop__file">
            <span class="diag-drop__filename">{{ form.imageFile?.name }}</span>
            <button type="button" class="diag-drop__change" @click="clearImage">{{ t('diag_change') }}</button>
          </div>
        </div>

        <button
          type="button"
          class="diag-run"
          :disabled="analyzing || !canRun"
          @click="$emit('analyze')"
        >
          <template v-if="analyzing">
            <span class="diag-run__spinner" aria-hidden="true"></span>
            {{ t('analyzing') }}
          </template>
          <template v-else>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
              <path d="M12 5a3 3 0 1 0-5.997.125 4 4 0 0 0-2.526 5.77 4 4 0 0 0 .556 6.588A4 4 0 1 0 12 18Z"/>
              <path d="M12 13a3 3 0 1 0 5.997.125 4 4 0 0 0 2.526-5.77 4 4 0 0 0-.556-6.588A4 4 0 1 0 12 8Z"/>
              <path d="M12 5v14"/><path d="M9 8h-2"/><path d="M15 16h2"/>
            </svg>
            {{ t('btn_analyze') }}
          </template>
        </button>

        <p class="diag-note">{{ t('diag_note') }}</p>
      </AppCard>

      <!-- RIGHT PANEL: result -->
      <AppCard class="diag-right">
        <h3 class="diag-right__title">{{ t('diag_output') }}</h3>

        <!-- Loading skeleton -->
        <div v-if="analyzing" class="diag-result-skel">
          <SkeletonLoader width="100%" height="64px" radius="var(--radius-md)" />
          <SkeletonLoader width="100%" height="10px" radius="9999px" />
          <SkeletonLoader width="80%" height="14px" radius="var(--radius-sm)" />
          <SkeletonLoader width="100%" height="14px" radius="var(--radius-sm)" />
          <SkeletonLoader width="60%" height="14px" radius="var(--radius-sm)" />
        </div>

        <!-- Empty / pre-result -->
        <div v-else-if="!result" class="diag-empty">
          <svg viewBox="0 0 80 80" fill="none" class="diag-empty__icon" aria-hidden="true">
            <circle cx="40" cy="40" r="36" stroke="currentColor" stroke-width="2"/>
            <path d="M40 28v18M40 52v.5" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
          </svg>
          <h4>{{ t('diag_empty_title') }}</h4>
          <p>{{ t('diag_empty_msg') }}</p>
        </div>

        <!-- Result -->
        <Transition name="diag-result">
          <div v-if="result && !analyzing" class="diag-result" :key="result.id">
            <div class="diag-result__banner" :class="result.binaryLabel === 'malignant' ? 'mal' : 'ben'">
              <span class="diag-result__banner-icon" aria-hidden="true">
                <svg v-if="result.binaryLabel === 'malignant'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/>
                  <line x1="12" y1="9" x2="12" y2="13"/>
                  <line x1="12" y1="17" x2="12.01" y2="17"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                  <polyline points="9 12 11 14 15 10"/>
                </svg>
              </span>
              <div>
                <div class="diag-result__banner-label">Classification result</div>
                <div class="diag-result__banner-text">{{ result.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}</div>
              </div>
            </div>

            <div class="diag-result__section">
              <div class="diag-result__head">
                <span class="diag-result__label">{{ t('r_pmal') }}</span>
                <strong :class="result.binaryLabel === 'malignant' ? 'diag-mal' : 'diag-ben'">{{ pct(result.binaryProbability) }}</strong>
              </div>
              <ConfidenceBar :value="result.binaryProbability" :variant="result.binaryLabel === 'malignant' ? 'malignant' : 'benign'" />
              <p class="diag-result__caption">{{ t('diag_confidence_caption').replace('{p}', pct(result.binaryProbability)) }}</p>
            </div>

            <div class="diag-result__subtype">
              <div class="diag-result__label">{{ t('r_subtype') }}</div>
              <div class="diag-result__subtype-name">{{ pretty(result.subtypeLabel) }}</div>
              <p v-if="describe(result.subtypeLabel)" class="diag-result__subtype-desc">{{ describe(result.subtypeLabel) }}</p>
              <div v-if="result.subtypeConfidence != null" class="diag-result__subtype-conf">
                <span class="diag-result__label">{{ t('r_subconf') }}</span>
                <span class="mono">{{ pct(result.subtypeConfidence) }}</span>
              </div>
            </div>

            <p class="diag-result__disclaimer">{{ t('diag_disclaimer') }}</p>

            <button type="button" class="diag-save" @click="$emit('save', result)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16" aria-hidden="true">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
                <polyline points="17 21 17 13 7 13 7 21"/>
                <polyline points="7 3 7 8 15 8"/>
              </svg>
              {{ t('diag_save') }}
            </button>

            <div class="diag-result__meta">
              <span>{{ t('saved_record') }} #{{ result.id }} · {{ result.imageFilename }} · {{ result.testDate }}</span>
            </div>
          </div>
        </Transition>
      </AppCard>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref, computed } from 'vue';
import AppCard from '../components/AppCard.vue';
import AppSearchSelect from '../components/AppSearchSelect.vue';
import ConfidenceBar from '../components/ConfidenceBar.vue';
import SkeletonLoader from '../components/SkeletonLoader.vue';
import { pct, pretty } from '../composables/format.js';
import { describeSubtype } from '../composables/subtypeDescriptions.js';

const props = defineProps({
  active: { type: Boolean, default: false },
  patientOptions: { type: Array, default: () => [] },
  analyzing: { type: Boolean, default: false },
  result: { type: Object, default: null },
  t: { type: Function, required: true },
});
const emit = defineEmits(['analyze', 'save']);

// Flatten patient options into the shape the search-select expects.
const selectOptions = computed(() =>
  props.patientOptions.map((p) => ({ ...p, displayLabel: `${p.patientId} — ${p.fullName}` }))
);

const form = reactive({
  patientId: '',
  date: new Date().toISOString().slice(0, 10),
  imageFile: null,
});
const preview = ref(null);
const isDragging = ref(false);

const canRun = computed(() => !!form.patientId && !!form.imageFile);

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
function clearImage() {
  form.imageFile = null;
  preview.value = null;
}
function describe(wire) { return describeSubtype(wire); }

defineExpose({ form, preview });
</script>

<style scoped>
/* === GRID === */
.diag-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: start;
}
.diag-left, .diag-right { padding: 24px; min-height: 460px; }
.diag-right__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px;
}

/* === FIELDS === */
.diag-field { margin-bottom: 18px; }
.diag-field__label {
  display: block;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.diag-select-wrap { position: relative; }
.diag-select {
  width: 100%;
  height: 40px;
  padding: 0 36px 0 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--bg-subtle);
  color: var(--text-primary);
  font: inherit;
  font-size: 14px;
  appearance: none;
  cursor: pointer;
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out;
}
.diag-select:hover { border-color: var(--border-strong); }
.diag-select:focus {
  outline: 0;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px var(--color-brand-soft);
}
.diag-select__chevron {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: var(--text-muted);
  pointer-events: none;
}

/* === DROPZONE === */
.diag-drop { position: relative; }
.diag-drop__hidden {
  position: absolute;
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
  pointer-events: none;
}
.diag-drop__zone {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 220px;
  padding: 32px 24px;
  border: 2px dashed var(--border-strong);
  border-radius: var(--radius-lg);
  background: var(--bg-subtle);
  cursor: pointer;
  text-align: center;
  gap: 10px;
  transition: border-color 150ms ease-out, background 150ms ease-out;
}
.diag-drop--dragging .diag-drop__zone {
  border-color: var(--color-brand);
  background: var(--color-brand-soft);
}
.diag-drop--has .diag-drop__zone {
  padding: 8px;
  border-style: solid;
  border-color: var(--border);
}
.diag-drop__icon {
  width: 40px;
  height: 40px;
  color: var(--text-muted);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.diag-drop__icon svg { width: 100%; height: 100%; }
.diag-drop__primary {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-primary);
}
.diag-drop__secondary {
  font-size: 13px;
  color: var(--text-muted);
}
.diag-drop__browse {
  margin-top: 4px;
  background: transparent;
  border: 1px solid var(--border-strong);
  color: var(--color-brand);
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  padding: 6px 14px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 150ms ease-out;
}
.diag-drop__browse:hover { background: var(--bg-card); }
.diag-drop__preview {
  width: 100%;
  max-height: 320px;
  object-fit: contain;
  border-radius: var(--radius-md);
  background: var(--bg-page);
}
.diag-drop__file {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
}
.diag-drop__filename { color: var(--text-secondary); }
.diag-drop__change {
  background: transparent;
  border: 0;
  color: var(--color-brand);
  font: inherit;
  font-weight: 500;
  font-size: 13px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-sm);
}
.diag-drop__change:hover { background: var(--color-brand-soft); }

/* === RUN BUTTON === */
.diag-run {
  width: 100%;
  height: 48px;
  margin-top: 20px;
  background: var(--color-brand);
  color: #fff;
  border: 0;
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: background 150ms ease-out, transform 150ms ease-out, opacity 150ms ease-out;
}
.diag-run:hover:not(:disabled) {
  background: var(--color-brand-hover);
  transform: translateY(-1px);
}
.diag-run:active:not(:disabled) { transform: translateY(0); }
.diag-run:disabled { opacity: 0.4; cursor: not-allowed; }
.diag-run svg { width: 18px; height: 18px; }
.diag-run__spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

.diag-note {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 14px;
  line-height: 1.55;
}

/* === RESULT SKELETON === */
.diag-result-skel { display: flex; flex-direction: column; gap: 12px; }
.diag-result-skel > *:nth-child(2) { margin-bottom: 16px; }
.diag-result-skel > *:nth-child(3) { width: 70%; }

/* === EMPTY STATE === */
.diag-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--text-muted);
  min-height: 320px;
}
.diag-empty__icon { width: 64px; height: 64px; opacity: 0.5; }
.diag-empty h4 { color: var(--text-primary); font-size: 16px; font-weight: 600; margin: 0; }
.diag-empty p { max-width: 280px; line-height: 1.6; margin: 0; font-size: 13px; }

/* === RESULT === */
.diag-result-enter-active { transition: opacity 300ms ease-out, transform 300ms ease-out; }
.diag-result-leave-active { transition: opacity 200ms ease-out; }
.diag-result-enter-from { opacity: 0; transform: translateY(8px); }
.diag-result-leave-to { opacity: 0; }

.diag-result__banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: var(--radius-md);
  margin-bottom: 20px;
  border-left: 4px solid var(--color-benign);
}
.diag-result__banner.mal {
  background: var(--color-malignant-soft);
  border-left-color: var(--color-malignant);
}
.diag-result__banner.ben {
  background: var(--color-benign-soft);
  border-left-color: var(--color-benign);
}
.diag-result__banner-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
  color: var(--color-malignant);
}
.diag-result__banner.ben .diag-result__banner-icon { color: var(--color-benign); }
.dark .diag-result__banner-icon { background: rgba(255, 255, 255, 0.06); }
.diag-result__banner-icon svg { width: 22px; height: 22px; }
.diag-result__banner-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 500;
  opacity: 0.8;
  color: inherit;
}
.diag-result__banner-text {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-top: 2px;
}
.diag-result__banner.mal .diag-result__banner-text { color: var(--color-malignant); }
.diag-result__banner.ben .diag-result__banner-text { color: var(--color-benign); }

.diag-result__section { margin-bottom: 20px; }
.diag-result__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.diag-result__label {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.diag-result__head strong {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}
.diag-mal { color: var(--color-malignant); }
.diag-ben { color: var(--color-benign); }
.diag-result__caption {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 8px;
  line-height: 1.5;
}

.diag-result__subtype {
  padding: 16px;
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}
.diag-result__subtype-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 4px 0 8px;
}
.diag-result__subtype-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
}
.diag-result__subtype-conf {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.diag-result__disclaimer {
  font-size: 12px;
  font-style: italic;
  color: var(--text-muted);
  line-height: 1.6;
  padding: 12px 14px;
  background: var(--bg-subtle);
  border-left: 3px solid var(--color-warning);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  margin-bottom: 16px;
}

.diag-save {
  width: 100%;
  height: 44px;
  background: transparent;
  color: var(--color-brand);
  border: 1.5px solid var(--color-brand);
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 150ms ease-out, transform 150ms ease-out;
}
.diag-save:hover { background: var(--color-brand-soft); transform: translateY(-1px); }

.diag-result__meta {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
  font-size: 12px;
  color: var(--text-muted);
}

@media (max-width: 900px) {
  .diag-grid { grid-template-columns: 1fr; }
}

@media (prefers-reduced-motion: reduce) {
  .diag-result-enter-active, .diag-result-leave-active, .diag-run, .diag-save { transition: none; }
  .diag-run__spinner { animation: none; }
}
</style>
