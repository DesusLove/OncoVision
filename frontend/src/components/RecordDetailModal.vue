<template>
  <div class="rdm" @click.self="$emit('close')">
    <div class="rdm__card">
      <div class="rdm__head">
        <h3>{{ t('rec_detail_title') }} <span class="rdm__id mono">#{{ record.id }}</span></h3>
        <button class="close-btn" :aria-label="t('rec_close')" @click="$emit('close')">&times;</button>
      </div>

      <div class="rdm__body">
        <!-- Result banner -->
        <div class="rdm__banner" :class="record.binaryLabel === 'malignant' ? 'mal' : 'ben'">
          <span class="rdm__banner-icon" aria-hidden="true">
            <svg v-if="record.binaryLabel === 'malignant'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
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
            <div class="rdm__banner-cap">Classification result</div>
            <div class="rdm__banner-text">{{ record.binaryLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}</div>
          </div>
          <span class="rdm__banner-confidence">{{ pct(record.binaryProbability) }}</span>
        </div>

        <!-- Confidence meter -->
        <div class="rdm__meter">
          <div class="rdm__meter-head">
            <span class="rdm__meter-label">{{ t('r_pmal') }}</span>
          </div>
          <ConfidenceBar :value="record.binaryProbability" :variant="record.binaryLabel === 'malignant' ? 'malignant' : 'benign'" :show-label="false" />
        </div>

        <!-- Subtype -->
        <div class="rdm__subtype">
          <div class="rdm__meter-label">{{ t('r_subtype') }}</div>
          <div class="rdm__subtype-name">{{ pretty(record.subtypeLabel) }}</div>
          <p v-if="describe(record.subtypeLabel)" class="rdm__subtype-desc">{{ describe(record.subtypeLabel) }}</p>
          <div v-if="record.subtypeConfidence != null" class="rdm__subtype-conf">
            <span class="rdm__meter-label">{{ t('r_subconf') }}</span>
            <span class="mono">{{ pct(record.subtypeConfidence) }}</span>
          </div>
        </div>

        <!-- Metadata grid -->
        <dl class="rdm__meta">
          <div class="rdm__meta-row">
            <dt>{{ t('rec_meta_id') }}</dt>
            <dd class="mono">#{{ record.id }}</dd>
          </div>
          <div class="rdm__meta-row">
            <dt>{{ t('rec_meta_patient') }}</dt>
            <dd>
              <div class="rdm__patient">
                <AvatarInitials :name="record.patient?.fullName || record.patientPatientId || '–'" :size="28" />
                <div>
                  <div>{{ record.patient?.fullName || record.patientPatientId || '–' }}</div>
                  <div class="rdm__patient-id mono">{{ record.patientPatientId || (record.patient ? record.patient.patientId : '') }}</div>
                </div>
              </div>
            </dd>
          </div>
          <div class="rdm__meta-row">
            <dt>{{ t('rec_meta_date') }}</dt>
            <dd class="mono">{{ record.testDate || '—' }}</dd>
          </div>
          <div class="rdm__meta-row">
            <dt>{{ t('rec_meta_created') }}</dt>
            <dd class="mono">{{ formatTimestamp(record.createdAt) }}</dd>
          </div>
          <div v-if="record.verified" class="rdm__meta-row">
            <dt>{{ t('th_status') }}</dt>
            <dd>
              <AppBadge variant="neutral">
                <span class="status-dot" style="background: var(--color-benign)"></span>
                {{ t('rec_verified') }}<span v-if="record.verifiedBy" class="rdm__meta-sub"> · {{ record.verifiedBy }} · {{ formatTimestamp(record.verifiedAt) }}</span>
              </AppBadge>
            </dd>
          </div>
          <div v-if="record.correctedLabel" class="rdm__meta-row">
            <dt>{{ t('rec_meta_corrected') }}</dt>
            <dd>
              <AppBadge :variant="record.correctedLabel === 'malignant' ? 'malignant' : 'benign'">
                {{ record.correctedLabel === 'malignant' ? t('badge_malignant') : t('badge_benign') }}
              </AppBadge>
            </dd>
          </div>
          <div class="rdm__meta-row">
            <dt>{{ t('rec_meta_image') }}</dt>
            <dd>
              <span v-if="record.imageFilename" class="rdm__filename mono">{{ record.imageFilename }}</span>
              <span v-else class="rdm__meta-sub">{{ t('rec_no_image') }}</span>
            </dd>
          </div>
        </dl>

        <p class="rdm__disclaimer">{{ t('diag_disclaimer') }}</p>
      </div>

      <div class="rdm__actions">
        <button type="button" class="btn ghost" @click="$emit('close')">{{ t('rec_close') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import AppBadge from './AppBadge.vue';
import AvatarInitials from './AvatarInitials.vue';
import ConfidenceBar from './ConfidenceBar.vue';
import { useI18n } from '../composables/useI18n.js';
import { pct, pretty } from '../composables/format.js';
import { describeSubtype } from '../composables/subtypeDescriptions.js';

const props = defineProps({
  record: { type: Object, required: true },
});
defineEmits(['close']);

const { t } = useI18n();

function describe(wire) { return describeSubtype(wire); }

function formatTimestamp(ts) {
  if (!ts) return '—';
  // Backend serializes LocalDateTime as ISO string or array — handle both.
  if (typeof ts === 'string') return ts.replace('T', ' ').replace(/\.\d+$/, '');
  if (Array.isArray(ts) && ts.length >= 6) {
    // [year, month, day, hour, minute, second]
    const pad = (n) => String(n).padStart(2, '0');
    return `${ts[0]}-${pad(ts[1])}-${pad(ts[2])} ${pad(ts[3])}:${pad(ts[4])}:${pad(ts[5])}`;
  }
  return String(ts);
}
</script>

<style scoped>
.rdm {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--overlay);
  z-index: 220;
  padding: 20px;
  animation: rdm-in 200ms ease-out;
}
@keyframes rdm-in { from { opacity: 0; } to { opacity: 1; } }
.rdm__card {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  max-width: 640px;
  width: 100%;
  max-height: 85vh;
  overflow-y: auto;
  animation: rdm-card-in 250ms ease-out;
}
@keyframes rdm-card-in { from { opacity: 0; transform: scale(0.96) translateY(8px); } to { opacity: 1; transform: none; } }
.rdm__head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 0;
}
.rdm__head h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  gap: 8px;
}
.rdm__id {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-muted);
}
.close-btn {
  background: none;
  border: 0;
  font-size: 22px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
  transition: color 150ms ease-out;
}
.close-btn:hover { color: var(--text-primary); }

.rdm__body { padding: 16px 24px 8px; }

.rdm__banner {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  border-radius: var(--radius-md);
  border-left: 4px solid var(--color-benign);
  background: var(--color-benign-soft);
  margin-bottom: 16px;
  color: var(--color-benign);
}
.rdm__banner.mal {
  background: var(--color-malignant-soft);
  border-left-color: var(--color-malignant);
  color: var(--color-malignant);
}
.rdm__banner-icon {
  width: 40px;
  height: 40px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.6);
  flex-shrink: 0;
}
.dark .rdm__banner-icon { background: rgba(255, 255, 255, 0.06); }
.rdm__banner-icon svg { width: 22px; height: 22px; }
.rdm__banner-cap {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 500;
  opacity: 0.8;
}
.rdm__banner-text { font-size: 22px; font-weight: 700; letter-spacing: -0.02em; line-height: 1.1; margin-top: 2px; }
.rdm__banner-confidence {
  margin-left: auto;
  font-size: 22px;
  font-weight: 700;
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.rdm__meter { margin-bottom: 16px; }
.rdm__meter-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.rdm__meter-label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.rdm__subtype {
  padding: 14px;
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
  margin-bottom: 16px;
}
.rdm__subtype-name { font-size: 16px; font-weight: 600; color: var(--text-primary); margin: 4px 0 6px; }
.rdm__subtype-desc { font-size: 13px; color: var(--text-secondary); line-height: 1.6; margin: 0 0 12px; }
.rdm__subtype-conf {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--border);
  font-size: 13px;
  font-weight: 500;
}

.rdm__meta {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  margin: 0 0 14px;
}
.rdm__meta-row {
  display: grid;
  grid-template-columns: 140px 1fr;
  gap: 12px;
  align-items: center;
}
.rdm__meta-row dt {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin: 0;
}
.rdm__meta-row dd { margin: 0; font-size: 13px; color: var(--text-primary); }
.rdm__meta-sub { color: var(--text-muted); font-size: 12px; margin-left: 4px; }
.rdm__patient { display: flex; align-items: center; gap: 10px; }
.rdm__patient-id { font-size: 11px; color: var(--text-muted); }
.rdm__filename { font-size: 12px; }

.rdm__disclaimer {
  font-size: 12px;
  font-style: italic;
  color: var(--text-muted);
  line-height: 1.6;
  padding: 10px 12px;
  background: var(--bg-subtle);
  border-left: 3px solid var(--color-warning);
  border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
  margin: 0 0 16px;
}

.rdm__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 24px 20px;
}

.btn {
  height: 38px;
  padding: 0 16px;
  background: var(--color-brand);
  color: #fff;
  border: 0;
  border-radius: var(--radius-md);
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 150ms ease-out;
}
.btn:hover { background: var(--color-brand-hover); }
.btn.ghost { background: transparent; color: var(--text-primary); border: 1px solid var(--border); }
.btn.ghost:hover { background: var(--bg-subtle); }

.status-dot { width: 8px; height: 8px; border-radius: 9999px; display: inline-block; margin-right: 4px; }

@media (prefers-reduced-motion: reduce) {
  .rdm, .rdm__card, .rdm__banner-icon, .btn, .close-btn { transition: none; animation: none; }
}
</style>
