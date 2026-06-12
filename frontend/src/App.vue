<template>
  <div :data-theme="darkMode ? 'dark' : 'light'" class="app-shell">
    <header>
      <div class="bar">
        <div class="brand">
          <svg viewBox="0 0 32 34" fill="none" stroke="currentColor" stroke-width="3.1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31"/>
            <path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31"/>
          </svg>
          <div>
            <div class="brand-name">OncoVision</div>
            <div class="brand-tag">{{ t('tagline') }}</div>
          </div>
        </div>
        <button class="hamburger" @click="mobileOpen = !mobileOpen" :aria-label="mobileOpen ? 'Close menu' : 'Open menu'">
          <span></span><span></span><span></span>
        </button>
        <nav :class="{ open: mobileOpen }">
          <button :class="{ active: activeTab === 'dashboard' }" @click="switchTab('dashboard')">
            {{ t('nav_dashboard') }}
          </button>
          <button :class="{ active: activeTab === 'patients' }" @click="switchTab('patients')">
            {{ t('nav_patients') }}
            <span v-if="badges.patients" class="nav-badge">{{ badges.patients }}</span>
          </button>
          <button :class="{ active: activeTab === 'diagnose' }" @click="switchTab('diagnose')">
            {{ t('nav_diagnose') }}
          </button>
          <button :class="{ active: activeTab === 'records' }" @click="switchTab('records')">
            {{ t('nav_records') }}
            <span v-if="badges.records" class="nav-badge">{{ badges.records }}</span>
          </button>
        </nav>
        <div class="header-actions">
          <span class="shortcut-hint">⌘K</span>
          <button class="theme-toggle" @click="toggleDark" :title="darkMode ? t('dm_light') : t('dm_dark')">
            <svg v-if="!darkMode" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="5"/><line x1="12" y1="1" x2="12" y2="3"/><line x1="12" y1="21" x2="12" y2="23"/><line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/><line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/><line x1="1" y1="12" x2="3" y2="12"/><line x1="21" y1="12" x2="23" y2="12"/><line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/><line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
            </svg>
          </button>
          <div class="lang">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3c2.5 2.7 2.5 15.3 0 18M12 3c-2.5 2.7-2.5 15.3 0 18"/>
            </svg>
            <select v-model="currentLang">
              <option value="en">English</option>
              <option value="zh">中文</option>
              <option value="ru">Русский</option>
              <option value="uz">Oʻzbek</option>
              <option value="kk">Қазақша</option>
              <option value="tr">Türkçe</option>
            </select>
          </div>
        </div>
      </div>
    </header>

    <div v-if="anyModal" class="overlay" @click.self="closeAllModals"></div>

    <div v-if="showPalette" class="overlay" @click="showPalette = false"></div>
    <div v-if="showPalette" class="palette">
      <div class="palette-input">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
        <input ref="paletteInput" v-model="paletteQuery" :placeholder="t('cmd_placeholder')" @keydown.escape="showPalette = false" @keydown.enter="runFirstPaletteAction">
      </div>
      <div class="palette-actions">
        <button v-for="a in filteredActions" :key="a.id" class="palette-item" @click="runPaletteAction(a)">
          <span class="palette-label">{{ a.label }}</span>
          <span class="palette-shortcut mono">{{ a.shortcut }}</span>
        </button>
      </div>
    </div>

    <main>
      <DashboardView :active="activeTab === 'dashboard'" :loading="statsLoading" :stats="statsData" :last-updated="lastUpdated" :t="t" />

      <PatientsView
        :active="activeTab === 'patients'"
        :loading="patientsLoading"
        :patients="patients"
        :page="pPage"
        :page-obj="pPageObj"
        :sort="patientSort"
        :search="patientSearch"
        :t="t"
        @sort="togglePatientSort"
        @page="loadPatients"
        @view="viewPatient"
        @delete="confirmDelete"
        @add="addPatient"
        @update="updatePatient"
        @cancel-edit="cancelEdit"
        @search-input="(v) => { patientSearch = v; debouncedSearch(); }"
        @search-submit="loadPatients(0)"
      />

      <DiagnoseView
        ref="diagnoseRef"
        :active="activeTab === 'diagnose'"
        :patient-options="patientOptions"
        :analyzing="isAnalyzing"
        :result="diagResult"
        :t="t"
        @analyze="runDiagnose"
      />

      <RecordsView
        :active="activeTab === 'records'"
        :loading="recordsLoading"
        :records="records"
        :filters="rFilters"
        :sort="recordsSort"
        :page="rPage"
        :page-obj="rPageObj"
        :t="t"
        @sort="toggleRecSort"
        @page="loadRecords"
        @verify="openVerify"
        @filter="loadRecords(0)"
        @update:filters="(v) => Object.assign(rFilters, v)"
      />
    </main>

    <DeleteConfirmModal :target="deleteTarget" :t="t" @cancel="cancelDelete" @confirm="execDelete" />

    <PatientViewModal :target="viewTarget" :records="patRecords" :t="t" @close="closeModal" @verify="openVerify" />

    <VerifyRecordModal :target="verifyTarget" :t="t" @cancel="cancelVerify" @confirm="confirmVerify" />

    <ToastContainer :toasts="toasts" @dismiss="dismissToast" />

    <!-- Generated at print time, hidden in screen mode. -->
    <div v-if="printHeader" class="print-only print-header">
      <h2>{{ printHeader.title }}</h2>
      <p>{{ printHeader.subtitle }} · {{ printHeader.date }}</p>
    </div>
  </div>
</template>

<script setup>
/**
 * App shell. Owns global state, composes the views and modals, and wires
 * keyboard shortcuts. Per-feature logic lives in the corresponding view
 * or composable; this file should stay close to a layout component.
 */
import { ref, reactive, computed, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useI18n } from './composables/useI18n.js';
import { useToasts } from './composables/useToasts.js';
import { apiCall } from './composables/useApi.js';

import DashboardView from './views/DashboardView.vue';
import PatientsView from './views/PatientsView.vue';
import DiagnoseView from './views/DiagnoseView.vue';
import RecordsView from './views/RecordsView.vue';

import DeleteConfirmModal from './components/DeleteConfirmModal.vue';
import PatientViewModal from './components/PatientViewModal.vue';
import VerifyRecordModal from './components/VerifyRecordModal.vue';
import ToastContainer from './components/ToastContainer.vue';

// === i18n & toasts ===
const { currentLang, t } = useI18n();
const { toasts, addToast, dismissToast } = useToasts();

// === Global UI state ===
const activeTab = ref('dashboard');
const darkMode = ref(localStorage.getItem('theme') === 'dark');
const mobileOpen = ref(false);
const showPalette = ref(false);
const paletteQuery = ref('');
const paletteInput = ref(null);
const lastUpdated = ref('');
const badges = reactive({ patients: 0, records: 0 });

const searchTimer = ref(0); // number, not ref(null) — setTimeout returns a number

// === Loading flags ===
const statsLoading = ref(true);
const patientsLoading = ref(false);
const recordsLoading = ref(false);

// === Data ===
const statsData = ref({});
const patients = ref([]);
const patientOptions = ref([]);
const records = ref([]);
const patRecords = ref([]);

// === Pagination & sort ===
const pPage = ref(0);
const pPageObj = reactive({ number: 0, totalPages: 1, totalElements: 0, first: true, last: true });
const rPage = ref(0);
const rPageObj = reactive({ number: 0, totalPages: 1, totalElements: 0, first: true, last: true });
const patientSort = reactive({ field: 'id', dir: 'asc' });
const recordsSort = reactive({ field: 'testDate', dir: 'desc' });

const patientSearch = ref('');
const rFilters = reactive({ label: '', from: '', to: '' });

// === Modal state ===
const viewTarget = ref(null);
const deleteTarget = ref(null);
const verifyTarget = ref(null);
const anyModal = computed(() => !!(viewTarget.value || deleteTarget.value || verifyTarget.value));

// === Diagnose ===
const isAnalyzing = ref(false);
const diagResult = ref(null);
const diagnoseRef = ref(null);

// === Print header (rendered at print time) ===
const printHeader = ref(null);
const pageTitle = computed(() => {
  const titles = { dashboard: 'Dashboard', patients: 'Patients', diagnose: 'Diagnose', records: 'Records' };
  return `${titles[activeTab.value]} · OncoVision`;
});

const faviconData = computed(() => {
  const c = darkMode.value ? '%23e2e8f0' : '%231f2937';
  return `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 34" fill="none" stroke="${c}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31"/><path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31"/></svg>`;
});

// === Command palette ===
const paletteActions = computed(() => [
  { id: 'dash', label: t('nav_dashboard'), shortcut: '⌘1', action: () => switchTab('dashboard') },
  { id: 'patients', label: t('nav_patients'), shortcut: '⌘2', action: () => switchTab('patients') },
  { id: 'diagnose', label: t('nav_diagnose'), shortcut: '⌘3', action: () => switchTab('diagnose') },
  { id: 'records', label: t('nav_records'), shortcut: '⌘4', action: () => switchTab('records') },
  { id: 'dark', label: darkMode.value ? t('dm_light') : t('dm_dark'), shortcut: '⌘D', action: toggleDark },
]);
const filteredActions = computed(() => {
  const q = paletteQuery.value.toLowerCase();
  if (!q) return paletteActions.value;
  return paletteActions.value.filter((a) => a.label.toLowerCase().includes(q));
});
function runPaletteAction(a) {
  showPalette.value = false;
  paletteQuery.value = '';
  nextTick(() => a.action());
}
function runFirstPaletteAction() {
  const first = filteredActions.value[0];
  if (first) runPaletteAction(first);
}

// === Tab switching ===
function switchTab(tab) {
  activeTab.value = tab;
  mobileOpen.value = false;
  if (tab === 'dashboard') loadStats();
  if (tab === 'patients') loadPatients(pPage.value);
  if (tab === 'diagnose') loadPatientOptions();
  if (tab === 'records') loadRecords(rPage.value);
}

// === Debounce ===
function debouncedSearch() {
  clearTimeout(searchTimer.value);
  searchTimer.value = setTimeout(() => loadPatients(0), 300);
}

// === Sort ===
function togglePatientSort(field) {
  if (patientSort.field === field) patientSort.dir = patientSort.dir === 'asc' ? 'desc' : 'asc';
  else { patientSort.field = field; patientSort.dir = 'asc'; }
  loadPatients(pPage.value);
}
function toggleRecSort(field) {
  if (recordsSort.field === field) recordsSort.dir = recordsSort.dir === 'asc' ? 'desc' : 'asc';
  else { recordsSort.field = field; recordsSort.dir = 'asc'; }
  loadRecords(rPage.value);
}

// === Data loaders ===
async function loadStats() {
  statsLoading.value = true;
  try {
    statsData.value = await apiCall('/api/stats');
    lastUpdated.value = new Date().toLocaleTimeString();
  } catch (e) { console.error(e); }
  finally { statsLoading.value = false; }
}

async function loadPatients(page) {
  if (page < 0) return;
  patientsLoading.value = true;
  try {
    const url = `/api/patients?q=${encodeURIComponent(patientSearch.value.trim())}&page=${page}&size=8&sortBy=${patientSort.field}&sortDir=${patientSort.dir}`;
    const d = await apiCall(url);
    patients.value = d.content || [];
    pPage.value = d.number;
    Object.assign(pPageObj, d);
    badges.patients = d.totalElements || 0;
  } catch (e) { addToast(e.message, 'error'); }
  finally { patientsLoading.value = false; }
}

async function addPatient(form) {
  const body = { patientId: form.pid.trim(), fullName: form.name.trim(), gender: form.gender, passportNumber: form.pass.trim() };
  try {
    await apiCall('/api/patients', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    addToast(t('msg_registered'));
    loadPatients(0);
  } catch (e) { addToast(e.message, 'error'); }
}

async function updatePatient(form) {
  if (!form.editingId) return;
  const body = { patientId: form.pid.trim(), fullName: form.name.trim(), gender: form.gender, passportNumber: form.pass.trim() };
  try {
    await apiCall(`/api/patients/${form.editingId}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    addToast(t('msg_updated'));
    loadPatients(pPage.value);
  } catch (e) { addToast(e.message, 'error'); }
}
function cancelEdit() { /* PatientsView already resets its local form */ }

function confirmDelete(p) { deleteTarget.value = p; }
function cancelDelete() { deleteTarget.value = null; }
async function execDelete() {
  if (!deleteTarget.value) return;
  const id = deleteTarget.value.id;
  try {
    await apiCall(`/api/patients/${id}`, { method: 'DELETE' });
    addToast(t('msg_deleted'));
    loadPatients(pPage.value);
  } catch (e) { addToast(e.message, 'error'); }
  finally { deleteTarget.value = null; }
}

async function viewPatient(p) {
  viewTarget.value = p;
  patRecords.value = [];
  try {
    const d = await apiCall(`/api/patients/${p.id}/records`);
    patRecords.value = Array.isArray(d) ? d : (d.content || []);
  } catch (e) { console.error(e); }
}
function closeModal() { viewTarget.value = null; patRecords.value = []; }
function closeAllModals() { closeModal(); cancelDelete(); cancelVerify(); }

function openVerify(r) { verifyTarget.value = r; }
function cancelVerify() { verifyTarget.value = null; }
async function confirmVerify(correction) {
  if (!verifyTarget.value) return;
  const body = correction ? { correctedLabel: correction } : {};
  try {
    const updated = await apiCall(`/api/records/${verifyTarget.value.id}/verify`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body),
    });
    const idx = records.value.findIndex((x) => x.id === updated.id);
    if (idx >= 0) records.value[idx] = updated;
    const pidx = patRecords.value.findIndex((x) => x.id === updated.id);
    if (pidx >= 0) patRecords.value[pidx] = updated;
    addToast(correction ? t('msg_verified_corrected') : t('msg_verified'));
  } catch (e) { addToast(e.message, 'error'); }
  finally { verifyTarget.value = null; }
}

async function loadPatientOptions() {
  try {
    const d = await apiCall('/api/patients/options');
    patientOptions.value = d.options || [];
    if (d.truncated) {
      addToast(t('patient_options_truncated').replace('{n}', String(d.cap || patientOptions.value.length)), 'error');
    }
  } catch (e) { addToast(e.message, 'error'); }
}

async function runDiagnose() {
  const form = diagnoseRef.value?.form;
  if (!form?.patientId) { addToast(t('sel_patient'), 'error'); return; }
  if (!form.imageFile) { addToast(t('choose_image'), 'error'); return; }
  const fd = new FormData();
  fd.append('image', form.imageFile);
  if (form.date) fd.append('testDate', form.date);
  isAnalyzing.value = true;
  diagResult.value = null;
  try {
    diagResult.value = await apiCall(`/api/patients/${form.patientId}/diagnose`, { method: 'POST', body: fd });
    addToast(t('diag_saved'));
  } catch (e) { addToast(e.message, 'error'); }
  finally { isAnalyzing.value = false; }
}

async function loadRecords(page) {
  if (page < 0) return;
  recordsLoading.value = true;
  let url = `/api/records?page=${page}&size=10&sortBy=${recordsSort.field}&sortDir=${recordsSort.dir}`;
  if (rFilters.label) url += `&label=${rFilters.label}`;
  if (rFilters.from) url += `&from=${rFilters.from}`;
  if (rFilters.to) url += `&to=${rFilters.to}`;
  try {
    const d = await apiCall(url);
    records.value = d.content || [];
    rPage.value = d.number;
    Object.assign(rPageObj, d);
    badges.records = d.totalElements || 0;
  } catch (e) {
    records.value = [];
    addToast(e.message, 'error');
  }
  finally { recordsLoading.value = false; }
}

// === Theme ===
function toggleDark() {
  darkMode.value = !darkMode.value;
  localStorage.setItem('theme', darkMode.value ? 'dark' : 'light');
}
watch(darkMode, (v) => localStorage.setItem('theme', v ? 'dark' : 'light'));

// === Page title + favicon ===
watch(pageTitle, (title) => { document.title = title; }, { immediate: true });
watch(faviconData, (href) => {
  let link = document.querySelector('link[rel="icon"]');
  if (!link) { link = document.createElement('link'); link.rel = 'icon'; document.head.appendChild(link); }
  link.href = href;
}, { immediate: true });

// === Print header ===
function beforePrint() {
  const tab = activeTab.value;
  const titles = { dashboard: 'Dashboard', patients: 'Patients', diagnose: 'Diagnose', records: 'Records' };
  printHeader.value = {
    title: titles[tab] || 'OncoVision',
    subtitle: tab === 'records' ? t('rec_title') : (tab === 'patients' ? t('pat_title') : (tab === 'diagnose' ? t('diag_title') : t('dash_title'))),
    date: new Date().toLocaleString(),
  };
}
function afterPrint() { printHeader.value = null; }

// === Keyboard shortcuts ===
function onKeyDown(e) {
  if (e.key === 'Escape') {
    if (verifyTarget.value) { cancelVerify(); return; }
    if (viewTarget.value) { closeModal(); return; }
    if (deleteTarget.value) { cancelDelete(); return; }
    if (showPalette.value) { showPalette.value = false; return; }
  }
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') { e.preventDefault(); showPalette.value = !showPalette.value; nextTick(() => paletteInput.value?.focus()); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === 'd') { e.preventDefault(); toggleDark(); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '1') { e.preventDefault(); switchTab('dashboard'); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '2') { e.preventDefault(); switchTab('patients'); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '3') { e.preventDefault(); switchTab('diagnose'); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '4') { e.preventDefault(); switchTab('records'); return; }
}

// === Init ===
onMounted(() => {
  loadStats();
  window.addEventListener('keydown', onKeyDown);
  window.addEventListener('beforeprint', beforePrint);
  window.addEventListener('afterprint', afterPrint);
});
onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown);
  window.removeEventListener('beforeprint', beforePrint);
  window.removeEventListener('afterprint', afterPrint);
  clearTimeout(searchTimer.value);
});
</script>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

  html { scroll-behavior: smooth; }

  /* === LIGHT THEME === */
  :root, [data-theme="light"] {
    --bg: #F8F9FA;
    --surface: #ffffff;
    --ink: #1f2937;
    --muted: #6b7280;
    --line: #e5e7eb;
    --rose: #d6447a;
    --rose-deep: #9c2d57;
    --rose-soft: #fbe6ee;
    --plum: #5e2747;
    --mal: #DC2626;
    --mal-soft: #fef2f2;
    --ben: #16A34A;
    --ben-soft: #f0fdf4;
    --shadow: 0 1px 2px rgba(0,0,0,.05), 0 8px 24px rgba(0,0,0,.04);
    --shadow-lg: 0 4px 12px rgba(0,0,0,.06), 0 16px 48px rgba(0,0,0,.08);
    --overlay: rgba(0,0,0,.4);
    --skeleton-base: #e5e7eb;
    --skeleton-shine: #f3f4f6;
  }

  /* === DARK THEME === */
  [data-theme="dark"] {
    --bg: #0f172a;
    --surface: #1e293b;
    --ink: #e2e8f0;
    --muted: #94a3b8;
    --line: #334155;
    --rose: #f472b6;
    --rose-deep: #ec4899;
    --rose-soft: rgba(244,114,182,.1);
    --plum: #1a1325;
    --mal: #f87171;
    --mal-soft: rgba(248,113,113,.1);
    --ben: #4ade80;
    --ben-soft: rgba(74,222,128,.1);
    --shadow: 0 1px 2px rgba(0,0,0,.3), 0 8px 24px rgba(0,0,0,.25);
    --shadow-lg: 0 4px 12px rgba(0,0,0,.4), 0 16px 48px rgba(0,0,0,.35);
    --overlay: rgba(0,0,0,.6);
    --skeleton-base: #334155;
    --skeleton-shine: #475569;
  }

  * { box-sizing: border-box; margin: 0; padding: 0; }
  *:focus-visible { outline: 2px solid #d6447a; outline-offset: 2px; border-radius: 4px; }

  body {
    font-family: "Inter", system-ui, sans-serif;
    background: var(--bg);
    color: var(--ink);
    -webkit-font-smoothing: antialiased;
    line-height: 1.5;
    transition: background 0.3s, color 0.3s;
  }

  .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
  h1, h2, h3, h4, .brand-name { font-family: "Inter", system-ui, sans-serif; font-weight: 700; letter-spacing: -0.02em; }

  /* === SKELETON === */
  .skeleton { background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-shine) 50%, var(--skeleton-base) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 8px; }
  .skeleton-stat-icon { width: 36px; height: 36px; border-radius: 10px; margin-bottom: 12px; }
  .skeleton-stat-value { width: 80px; height: 36px; margin-bottom: 8px; }
  .skeleton-stat-label { width: 100px; height: 14px; }
  .skeleton-row { height: 52px; margin-bottom: 4px; border-radius: 0; }
  @keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

  /* === HEADER === */
  header { background: linear-gradient(125deg, var(--plum), #d6447a); color: #fff; padding: 0 26px; box-shadow: var(--shadow-lg); position: sticky; top: 0; z-index: 100; transition: background 0.3s; }
  .bar { display: flex; align-items: center; gap: 22px; max-width: 1200px; margin: 0 auto; min-height: 70px; padding: 8px 0; }
  .brand { display: flex; align-items: center; gap: 12px; }
  .brand svg { width: 34px; height: 34px; flex: 0 0 auto; }
  .brand-name { font-size: 21px; line-height: 1; }
  .brand-tag { font-size: 11.5px; color: #f6d6e3; letter-spacing: .02em; font-weight: 500; }

  nav { display: flex; gap: 4px; margin-left: auto; }
  nav button { background: transparent; border: 0; color: #f3d4e1; font: inherit; font-weight: 600; padding: 8px 15px; border-radius: 9px; cursor: pointer; transition: .15s; white-space: nowrap; display: inline-flex; align-items: center; gap: 6px; position: relative; }
  nav button:hover { background: rgba(255,255,255,.12); color: #fff; }
  nav button.active { background: #fff; color: #9c2d57; }
  .nav-badge { background: rgba(255,255,255,.25); color: #fff; font-size: 11px; padding: 2px 7px; border-radius: 99px; font-weight: 600; min-width: 20px; text-align: center; }
  nav button.active .nav-badge { background: #fbe6ee; color: #9c2d57; }

  .header-actions { display: flex; align-items: center; gap: 10px; }
  .shortcut-hint { font-size: 11px; color: rgba(255,255,255,.5); font-weight: 500; letter-spacing: .05em; display: none; }
  @media (min-width: 900px) { .shortcut-hint { display: block; } }
  .theme-toggle { background: rgba(255,255,255,.14); border: 1px solid rgba(255,255,255,.3); border-radius: 8px; padding: 7px 9px; cursor: pointer; display: flex; align-items: center; transition: .15s; }
  .theme-toggle:hover { background: rgba(255,255,255,.25); }
  .theme-toggle svg { width: 16px; height: 16px; color: #fff; }
  .lang { display: flex; align-items: center; gap: 7px; color: #fff; }
  .lang svg { width: 16px; height: 16px; opacity: .85; }
  .lang select { background: rgba(255,255,255,.14); color: #fff; border: 1px solid rgba(255,255,255,.3); border-radius: 8px; padding: 7px 9px; font: inherit; font-size: 13px; font-weight: 500; cursor: pointer; }
  .lang select option { color: #1f2937; }
  .hamburger { display: none; flex-direction: column; gap: 5px; background: none; border: 0; cursor: pointer; padding: 4px; }
  .hamburger span { display: block; width: 22px; height: 2.5px; background: #fff; border-radius: 99px; transition: .2s; }

  /* === MAIN === */
  main { max-width: 1200px; margin: 32px auto; padding: 0 26px; }
  section { display: none; animation: rise .35s ease both; }
  section.active { display: block; }
  @keyframes rise { from { opacity: 0; transform: translateY(12px); } to { opacity: 1; transform: none; } }

  .head { margin-bottom: 24px; }
  .head h2 { font-size: 28px; color: var(--ink); }
  .head p { color: var(--muted); font-size: 14px; margin-top: 4px; }
  .updated { font-style: italic; opacity: .7; font-size: 12px; }

  .card { background: var(--surface); border: 1px solid var(--line); border-radius: 16px; box-shadow: var(--shadow); transition: background 0.3s, border-color 0.3s, box-shadow 0.3s, transform 0.2s; }
  .card:hover { transform: translateY(-1px); box-shadow: var(--shadow-lg); }
  .pad { padding: 24px; }

  /* === STAT CARDS === */
  .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 24px; }
  .stat { padding: 20px; animation: rise .4s ease both; }
  .stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
  .stat .label { font-size: 12px; text-transform: uppercase; letter-spacing: .08em; color: var(--muted); font-weight: 600; }
  .stat-icon-box { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: #f3f4f6; }
  [data-theme="dark"] .stat-icon-box { background: #334155; }
  .stat-icon-box svg { width: 18px; height: 18px; color: var(--muted); }
  .stat-icon-box.mal { background: var(--mal-soft); } .stat-icon-box.mal svg { color: var(--mal); }
  .stat-icon-box.ben { background: var(--ben-soft); } .stat-icon-box.ben svg { color: var(--ben); }
  .stat .value { font-size: 36px; font-weight: 700; color: var(--ink); line-height: 1.2; }
  .stat .value.mal { color: var(--mal); }
  .stat .value.ben { color: var(--ben); }

  /* === DONUT CHART === */
  .chart-section { display: flex; gap: 32px; align-items: center; flex-wrap: wrap; }
  .donut-wrap { width: 180px; height: 180px; flex-shrink: 0; }
  .donut { width: 100%; height: 100%; }
  .donut-center { font-size: 28px; font-weight: 700; fill: var(--ink); }
  .donut-sub { font-size: 12px; fill: var(--muted); }
  .donut-legend { flex: 1; display: flex; flex-direction: column; gap: 8px; min-width: 200px; }
  .legend-item { display: grid; grid-template-columns: 12px 1fr 40px 48px; align-items: center; gap: 10px; font-size: 13px; }
  .legend-dot { width: 10px; height: 10px; border-radius: 50%; flex-shrink: 0; }
  .legend-label { font-weight: 500; color: var(--ink); }
  .legend-val { color: var(--ink); font-weight: 600; text-align: right; }
  .legend-pct { color: var(--muted); text-align: right; }

  /* === TABLES === */
  table { width: 100%; border-collapse: collapse; font-size: 14px; }
  th { text-align: left; font-size: 11px; letter-spacing: .06em; text-transform: uppercase; color: var(--muted); padding: 14px; border-bottom: 2px solid var(--line); font-weight: 600; user-select: none; }
  th.sortable { cursor: pointer; }
  th.sortable:hover { color: var(--ink); }
  .sort-arrow { font-size: 10px; margin-left: 2px; }
  td { padding: 14px; border-bottom: 1px solid var(--line); vertical-align: middle; }
  tr:last-child td { border-bottom: 0; }
  [data-theme="dark"] tbody tr:hover { background: rgba(255,255,255,.03); }
  [data-theme="light"] tbody tr:hover { background: #f9fafb; }

  /* === BADGES === */
  .badge { display: inline-flex; align-items: center; padding: 4px 12px; border-radius: 99px; font-size: 12px; font-weight: 600; }
  .badge.large { font-size: 16px; padding: 6px 16px; }
  .badge.mal { background: var(--mal-soft); color: var(--mal); }
  .badge.ben { background: var(--ben-soft); color: var(--ben); }
  .badge.solid.mal { background: var(--mal); color: #fff; }
  .badge.solid.ben { background: var(--ben); color: #fff; }
  .badge.solid.verified { background: var(--ben); color: #fff; display: inline-flex; align-items: center; gap: 4px; }

  /* === FORMS === */
  .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  label.f { display: block; font-size: 13px; font-weight: 600; margin-bottom: 8px; color: var(--ink); }
  input, select.in { width: 100%; padding: 10px 14px; border: 1px solid #d1d5db; border-radius: 8px; font: inherit; background: var(--surface); color: var(--ink); transition: .15s; font-size: 14px; }
  [data-theme="dark"] input, [data-theme="dark"] select.in { border-color: #475569; background: #0f172a; }
  input:focus, select.in:focus { outline: 0; border-color: #d6447a; box-shadow: 0 0 0 3px var(--rose-soft); }

  /* === BUTTONS === */
  .btn { background: #d6447a; color: #fff; border: 0; border-radius: 8px; padding: 10px 20px; font: inherit; font-weight: 600; cursor: pointer; transition: .15s; display: inline-flex; align-items: center; gap: 8px; }
  .btn:hover { background: #9c2d57; }
  .btn:active { transform: scale(.97); }
  .btn.ghost { background: var(--surface); color: var(--ink); border: 1px solid var(--line); }
  .btn.ghost:hover { background: var(--bg); }
  .btn:disabled { opacity: .55; cursor: not-allowed; }
  .btn.danger-btn { background: #DC2626; }
  .btn.danger-btn:hover { background: #b91c1c; }

  .table-actions { display: flex; gap: 8px; justify-content: flex-end; }
  .icon-btn { display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 6px; border: 1px solid var(--line); background: var(--surface); color: var(--muted); cursor: pointer; transition: 0.2s; }
  .icon-btn:hover { background: var(--bg); color: var(--ink); }
  .icon-btn svg { width: 16px; height: 16px; }
  .icon-btn.danger:hover { background: var(--mal-soft); color: var(--mal); border-color: var(--mal-soft); }

  .toolbar { display: flex; gap: 12px; margin-bottom: 20px; flex-wrap: wrap; }
  .toolbar input, .toolbar select.in { width: auto; }
  .toolbar .search { flex: 1 1 240px; }

  /* === DROP ZONE === */
  .file-hidden { opacity: 0; width: 0.1px; height: 0.1px; position: absolute; }
  .drop-zone-content { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 32px; border: 2px dashed var(--line); border-radius: 16px; background: var(--bg); cursor: pointer; transition: 0.2s; text-align: center; min-height: 160px; }
  .drop-zone-content:hover { border-color: #d6447a; } [data-theme="light"] .drop-zone-content:hover { background: var(--rose-soft); }
  .drop-zone.hasFile .drop-zone-content { padding: 12px; }
  .drop-zone.dragging .drop-zone-content { border-color: #d6447a; background: var(--rose-soft); border-style: solid; }
  .drop-icon { width: 32px; height: 32px; color: var(--muted); margin-bottom: 12px; transition: .2s; }
  .drop-zone-content:hover .drop-icon { color: #d6447a; }
  .drop-text { font-size: 14px; font-weight: 500; color: var(--muted); }
  .preview-img { max-width: 100%; max-height: 240px; border-radius: 8px; object-fit: contain; }

  /* === SPINNER === */
  .spinner { display: inline-block; width: 18px; height: 18px; border: 2.5px solid rgba(255,255,255,.3); border-top-color: #fff; border-radius: 50%; animation: spin .6s linear infinite; }
  @keyframes spin { to { transform: rotate(360deg); } }

  /* === MISC === */
  .pager { display: flex; align-items: center; gap: 16px; justify-content: flex-end; margin-top: 16px; font-size: 14px; color: var(--muted); font-weight: 500; }

  /* === EMPTY STATES === */
  .empty-state { display: flex; flex-direction: column; align-items: center; gap: 16px; padding: 40px 20px; color: var(--muted); font-size: 14px; text-align: center; }
  .empty-illustration { width: 80px; height: 80px; opacity: .5; }
  .empty-state p { max-width: 260px; }

  /* === RESULTS === */
  .result { margin-top: 24px; border-left: 6px solid var(--line); }
  .result.mal { border-left-color: var(--mal); } [data-theme="light"] .result.mal { background: linear-gradient(to right, var(--mal-soft) 0%, transparent 100%); }
  [data-theme="light"] .result.ben { background: linear-gradient(to right, var(--ben-soft) 0%, transparent 100%); }
  .result-header { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
  .result h3 { font-size: 20px; margin: 0; color: var(--ink); }
  .kv { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--line); }
  .kv .k { font-size: 12px; color: var(--muted); text-transform: uppercase; letter-spacing: .06em; font-weight: 600; }
  .kv .v { font-size: 18px; font-weight: 600; margin-top: 4px; color: var(--ink); }
  .highlight-pct { font-size: 24px; font-weight: 700; }
  .mal-color { color: var(--mal); }
  .ben-color { color: var(--ben); }
  .note { font-size: 13px; color: var(--muted); margin-top: 20px; }
  .image-cell { display: flex; align-items: center; gap: 8px; }
  .img-icon { width: 16px; height: 16px; color: var(--muted); flex-shrink: 0; }

  /* === MODALS === */
  .overlay { position: fixed; inset: 0; background: var(--overlay); z-index: 200; animation: fadeIn .2s ease; }
  @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  .modal { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 210; padding: 20px; animation: fadeIn .2s ease; }
  .modal-card { background: var(--surface); border-radius: 16px; box-shadow: var(--shadow-lg); max-width: 500px; width: 100%; max-height: 80vh; overflow-y: auto; animation: modalIn .25s ease; }
  .modal.wide .modal-card { max-width: 700px; }
  @keyframes modalIn { from { opacity: 0; transform: scale(.95) translateY(10px); } to { opacity: 1; transform: scale(1) translateY(0); } }
  .modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
  .modal-header h3 { font-size: 20px; color: var(--ink); }
  .close-btn { background: none; border: 0; font-size: 24px; color: var(--muted); cursor: pointer; padding: 0 4px; line-height: 1; }
  .close-btn:hover { color: var(--ink); }
  .modal-body { padding: 16px 24px 24px; }
  .modal-actions { display: flex; gap: 10px; justify-content: flex-end; padding: 16px 24px 24px; }
  .patient-details { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
  .detail-row { display: flex; flex-direction: column; gap: 2px; }
  .dk { font-size: 12px; color: var(--muted); text-transform: uppercase; font-weight: 600; letter-spacing: .04em; }
  .dv { font-size: 15px; color: var(--ink); font-weight: 500; }
  .mini-table { margin-top: 8px; }
  .mini-table table { font-size: 13px; }
  .mini-table th { padding: 10px 12px; }
  .mini-table td { padding: 10px 12px; }
  h3 + p { color: var(--muted); font-size: 14px; margin-bottom: 16px; }

  /* === TOASTS === */
  .toast-container { position: fixed; bottom: 24px; right: 24px; z-index: 999; display: flex; flex-direction: column-reverse; gap: 8px; pointer-events: none; }
  .toast { display: flex; align-items: center; gap: 10px; padding: 12px 16px; border-radius: 10px; font-size: 14px; font-weight: 500; box-shadow: var(--shadow-lg); pointer-events: auto; min-width: 280px; max-width: 420px; animation: toastIn .3s ease; }
  .toast.success { background: var(--surface); color: var(--ben); border: 1px solid #bbf7d0; }
  .toast.error { background: var(--surface); color: var(--mal); border: 1px solid #fecaca; }
  [data-theme="dark"] .toast.success { border-color: rgba(74,222,128,.3); background: #0f172a; }
  [data-theme="dark"] .toast.error { border-color: rgba(248,113,113,.3); background: #0f172a; }
  .toast-close { background: none; border: 0; color: inherit; opacity: .5; cursor: pointer; font-size: 18px; margin-left: auto; line-height: 1; }
  .toast-enter-active { transition: all .3s ease; }
  .toast-leave-active { transition: all .2s ease; }
  .toast-enter-from { opacity: 0; transform: translateX(40px); }
  .toast-leave-to { opacity: 0; transform: translateX(40px); }
  @keyframes toastIn { from { opacity: 0; transform: translateX(40px); } to { opacity: 1; transform: none; } }

  /* === COMMAND PALETTE === */
  .palette { position: fixed; top: 20%; left: 50%; transform: translateX(-50%); z-index: 300; width: 480px; max-width: 90vw; background: var(--surface); border-radius: 16px; box-shadow: var(--shadow-lg); overflow: hidden; animation: modalIn .2s ease; }
  .palette-input { display: flex; align-items: center; gap: 10px; padding: 14px 18px; border-bottom: 1px solid var(--line); color: var(--muted); }
  .palette-input input { flex: 1; border: 0; background: none; font: inherit; font-size: 15px; color: var(--ink); }
  .palette-input input:focus { outline: 0; }
  .palette-actions { padding: 8px; display: flex; flex-direction: column; gap: 2px; }
  .palette-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; border-radius: 8px; border: 0; background: none; font: inherit; font-size: 14px; color: var(--ink); cursor: pointer; text-align: left; transition: .1s; }
  .palette-item:hover { background: var(--bg); }
  .palette-shortcut { font-size: 12px; color: var(--muted); }

  /* === PRINT === */
  .print-only { display: none; }
  @media print {
    /* Generated by beforeprint() in App.vue */
    header, .toolbar, .pager, .btn, .icon-btn, .toast-container,
    .hamburger, .theme-toggle, .lang, .shortcut-hint { display: none !important; }
    body { background: #fff; color: #000; font-size: 12px; }
    .card { box-shadow: none; border: 1px solid #ccc; break-inside: avoid; }
    .badge.solid.mal { background: #DC2626; color: #fff; }
    .badge.solid.ben { background: #16A34A; color: #fff; }
    .badge.solid.verified { background: #16A34A; color: #fff; }
    section { display: block !important; margin-bottom: 16px; }
    .stats { grid-template-columns: repeat(4, 1fr); }
    /* Show the print header only in print mode. */
    .print-only { display: block; }
    .print-header { border-bottom: 2px solid #000; padding-bottom: 8px; margin-bottom: 16px; }
    .print-header h2 { font-size: 18px; color: #000; margin-bottom: 4px; }
    .print-header p { font-size: 11px; color: #444; }
    /* Drop the modal/overlay so print is a clean page. */
    .overlay, .modal, .palette { display: none !important; }
  }

  /* === MOBILE === */
  @media (max-width: 760px) {
    .stats { grid-template-columns: 1fr 1fr; }
    .grid2 { grid-template-columns: 1fr; }
    .kv { grid-template-columns: 1fr; }
    .patient-details { grid-template-columns: 1fr; }
    .chart-section { flex-direction: column; align-items: center; }
    .hamburger { display: flex; }
    nav { display: none; position: absolute; top: 100%; left: 0; right: 0; flex-direction: column; background: linear-gradient(125deg, var(--plum), #d6447a); padding: 8px 26px 16px; gap: 2px; box-shadow: var(--shadow-lg); }
    nav.open { display: flex; }
    nav button { padding: 12px 15px; text-align: left; }
    .header-actions { margin-left: auto; }
    .lang select { font-size: 12px; padding: 6px 8px; }
    .toast-container { left: 16px; right: 16px; bottom: 16px; }
    .toast { min-width: 0; max-width: 100%; }
  }
</style>
