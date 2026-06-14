<template>
  <div class="app-shell">
    <!-- Login page renders its own minimal navbar (variant="landing")
         inside the LoginView/LandingLogin component, so we skip the
         full app navbar when on the login route. Without this guard,
         both navbars stack on top of each other on /login. -->
    <Navbar
      v-if="activeTab !== 'login'"
      variant="app"
      :tagline="t('tagline')"
      :mobile-open="mobileOpen"
      :authenticated="auth.isAuthenticated.value"
      :user-display-name="auth.user.value?.displayName || auth.user.value?.username || ''"
      :user-initials="userInitials"
      :current-lang="currentLang"
      :badges="badges"
      @toggle-mobile="mobileOpen = !mobileOpen"
      @navigate="onNavLink"
      @logout="onLogout"
      @open-user-menu="onUserMenu"
      @change-lang="(v) => currentLang = v"
      @toggle-theme="toggleTheme"
    />

    <div v-if="anyModal" class="overlay" @click.self="closeAllModals"></div>

    <div v-if="showPalette" class="overlay" @click="showPalette = false"></div>
    <div v-if="showPalette" class="palette" ref="paletteEl" tabindex="-1">
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

    <!-- Router outlet for routes that DON'T render inside the main app shell
         (e.g. the login page, which needs its own full-screen layout). -->
    <router-view v-if="activeTab === 'login'" />

    <main v-else>
      <DashboardView :active="activeTab === 'dashboard'" :loading="statsLoading" :stats="statsData" :recent="recentRecords" :last-updated="lastUpdated" :t="t" @view-record="viewRecent" />

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
        @analyze="runDiagnoseFromView"
        @save="onDiagSave"
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
        @reset="loadRecords(0)"
        @view-record="viewRecent"
      />
    </main>

    <DeleteConfirmModal :target="deleteTarget" :t="t" @cancel="cancelDelete" @confirm="execDelete" />

    <PatientViewModal :target="viewTarget" :records="patRecords" :t="t" @close="closeModal" @verify="openVerify" />

    <VerifyRecordModal :target="verifyTarget" :t="t" @cancel="cancelVerify" @confirm="confirmVerify" />

    <AppToast />

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
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from './composables/useI18n.js';
import { useToast } from './composables/useToast.js';
import { useFocusTrapListener } from './composables/useFocusTrap.js';
import { useMedicalData } from './composables/useMedicalData.js';
import { useAuth } from './composables/useAuth.js';
import { useTheme } from './composables/useTheme.js';
import { apiCall } from './composables/useApi.js';

import DashboardView from './views/DashboardView.vue';
import PatientsView from './views/PatientsView.vue';
import DiagnoseView from './views/DiagnoseView.vue';
import RecordsView from './views/RecordsView.vue';

import DeleteConfirmModal from './components/DeleteConfirmModal.vue';
import PatientViewModal from './components/PatientViewModal.vue';
import VerifyRecordModal from './components/VerifyRecordModal.vue';
import AppToast from './components/AppToast.vue';
import Navbar from './components/Navbar.vue';

// === i18n & toasts & auth ===
const { currentLang, t } = useI18n();
const toast = useToast();
const auth = useAuth();

// Display initials for the navbar avatar button.
const userInitials = computed(() => {
  const u = auth.user.value;
  if (!u) return '?';
  const name = (u.displayName || u.username || '').trim();
  if (!name) return '?';
  const parts = name.split(/\s+/);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
});

// === Global UI state ===
const router = useRouter();
const route = useRoute();
// `activeTab` is now derived from the current route name so the URL is
// the single source of truth — back/forward, deep-links and stat-card
// navigation all stay in sync without extra plumbing.
const activeTab = computed(() => route.name || 'dashboard');
// Theme state lives in the shared useTheme composable so the Navbar
// toggle and the ⌘D command-palette shortcut stay in lockstep.
const theme = useTheme();
const mobileOpen = ref(false);
const showPalette = ref(false);
const paletteQuery = ref('');
const paletteInput = ref(null);
const badges = reactive({ patients: 0, records: 0 });

// === Data layer (stats / patients / records / diagnose) ===
// All backend state lives in useMedicalData(); App.vue just wires the
// reactive refs into the view components and exposes mutators to them.
const {
  statsLoading, statsData, recentRecords, lastUpdated, loadStats,
  patientsLoading, patients, patientOptions, patientSort, patientSearch,
  pPage, pPageObj, loadPatients, loadPatientOptions, togglePatientSort, debouncedSearch,
  recordsLoading, records, recordsSort, rFilters, rPage, rPageObj,
  loadRecords, toggleRecSort,
  isAnalyzing, diagResult, runDiagnose,
} = useMedicalData({
  onPatientsLoaded: (n) => { badges.patients = n; },
  onRecordsLoaded:  (n) => { badges.records  = n; },
});

// patRecords is local — only the patient view modal needs it, and it's
// populated per-modal-open rather than as part of the global cache.
const patRecords = ref([]);

// === Modal state ===
const viewTarget = ref(null);
const deleteTarget = ref(null);
const verifyTarget = ref(null);
const anyModal = computed(() => !!(viewTarget.value || deleteTarget.value || verifyTarget.value));

// === Diagnose view ref (isAnalyzing / diagResult come from useMedicalData) ===
const diagnoseRef = ref(null);

// === Print header (rendered at print time) ===
const printHeader = ref(null);
const pageTitle = computed(() => {
  const titles = { dashboard: 'Dashboard', patients: 'Patients', diagnose: 'Diagnose', records: 'Records' };
  return `${titles[activeTab.value]} · OncoVision`;
});

const faviconData = computed(() => {
  const c = theme.isDark.value ? '%23e2e8f0' : '%231f2937';
  return `data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 34" fill="none" stroke="${c}" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31"/><path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31"/></svg>`;
});

// === Command palette ===
const paletteActions = computed(() => [
  { id: 'dash', label: t('nav_dashboard'), shortcut: '⌘1', action: () => switchTab('dashboard') },
  { id: 'patients', label: t('nav_patients'), shortcut: '⌘2', action: () => switchTab('patients') },
  { id: 'diagnose', label: t('nav_diagnose'), shortcut: '⌘3', action: () => switchTab('diagnose') },
  { id: 'records', label: t('nav_records'), shortcut: '⌘4', action: () => switchTab('records') },
  { id: 'dark', label: theme.isDark.value ? t('dm_light') : t('dm_dark'), shortcut: '⌘D', action: toggleTheme },
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
// All navigation goes through the router so the URL stays in sync.
// The view-load side effects (loadStats, loadPatients, etc.) are still
// fired here for parity with the old tab behaviour.
function switchTab(tab) {
  mobileOpen.value = false;
  if (route.name === tab) {
    // Already on this tab — just re-run the loader in case data is stale.
    runTabLoader(tab);
    return;
  }
  router.push({ name: tab }).then(() => runTabLoader(tab));
}
function runTabLoader(tab) {
  if (tab === 'dashboard') loadStats();
  if (tab === 'patients') loadPatients(pPage.value);
  if (tab === 'diagnose') loadPatientOptions();
  if (tab === 'records') loadRecords(rPage.value);
}
// When the route changes via back/forward or a deep-link, fire the
// matching loader so data is fresh.
watch(() => route.name, (name) => { if (name) runTabLoader(name); });

// === Debounce ===
// debouncedSearch comes from useMedicalData() — see the import at the top.

// The data layer (loadStats / loadPatients / loadRecords / togglePatientSort /
// toggleRecSort / runDiagnose) now lives in useMedicalData(). Only the
// mutators below — add/update/delete patient, viewPatient, openVerify,
// confirmVerify, etc. — stay here because they orchestrate local modal
// state alongside the API call.

async function addPatient(form) {
  const body = { patientId: form.pid.trim(), fullName: form.name.trim(), gender: form.gender, passportNumber: form.pass.trim() };
  try {
    await apiCall('/api/patients', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    toast.success(t('msg_registered'));
    loadPatients(0);
  } catch (e) { toast.error(e.message); }
}

async function updatePatient(form) {
  if (!form.editingId) return;
  const body = { patientId: form.pid.trim(), fullName: form.name.trim(), gender: form.gender, passportNumber: form.pass.trim() };
  try {
    await apiCall(`/api/patients/${form.editingId}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    toast.success(t('msg_updated'));
    loadPatients(pPage.value);
  } catch (e) { toast.error(e.message); }
}
function cancelEdit() { /* PatientsView already resets its local form */ }

function confirmDelete(p) { deleteTarget.value = p; }
function cancelDelete() { deleteTarget.value = null; }
async function execDelete() {
  if (!deleteTarget.value) return;
  const id = deleteTarget.value.id;
  try {
    await apiCall(`/api/patients/${id}`, { method: 'DELETE' });
    toast.success(t('msg_deleted'));
    loadPatients(pPage.value);
  } catch (e) { toast.error(e.message); }
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

/**
 * User-avatar click. Real profile/settings screens are out of scope for
 * this build, so we surface a single info toast. The avatar stays as a
 * permanent affordance so the IA matches the rest of the app.
 */
function onUserMenu() {
  toast.info('Profile & settings are coming soon.');
}
async function onLogout() {
  await auth.logout();
  toast.success(t('logout_success'));
  router.push({ name: 'login' });
}

/**
 * Click handler for a "Recent activity" row on the dashboard. Looks up
 * the owning patient and opens the patient view modal so the record is
 * visible in context (full history, not just one row).
 */
async function viewRecent(r) {
  if (!r) return;
  // Records from the list endpoint don't carry the full Patient object,
  // only the display fields. Fetch the patient by id to populate the modal.
  const pid = r.patientId || r.patient?.id;
  if (!pid) { openVerify(r); return; }
  try {
    const p = await apiCall(`/api/patients/${pid}`);
    viewPatient(p);
  } catch (e) {
    // Fallback: at least open the verify modal so the user can act on it.
    openVerify(r);
  }
}

/**
 * Save button on the Diagnose result panel. The backend persists the
 * record on the initial /diagnose POST, so this button is a confirmation
 * ack that refreshes both the recent-activity table and the records
 * page cache so the new entry is visible everywhere.
 */
function onDiagSave() {
  if (!diagResult.value) return;
  toast.success(t('msg_diag_record_saved'));
  // Refresh both the dashboard's recent activity and the records list.
  loadStats();
  loadRecords(rPage.value);
}

// Wrapper for the DiagnoseView template: pulls the form from the
// view's exposed ref and forwards to the composable.
function runDiagnoseFromView() {
  const form = diagnoseRef.value?.form;
  if (form) runDiagnose(form);
}

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
    toast.success(correction ? t('msg_verified_corrected') : t('msg_verified'));
  } catch (e) { toast.error(e.message); }
  finally { verifyTarget.value = null; }
}

// The remaining data loaders (loadStats / loadPatients / loadRecords /
// loadPatientOptions / runDiagnose / togglePatientSort / toggleRecSort)
// live in useMedicalData() — see the import at the top of this file.

// === Theme ===
// Theme state is shared via useTheme(). The pre-mount boot script in
// index.html applies the saved `.dark` class before Vue mounts, so
// by the time the SPA starts we just need to keep the in-memory ref
// in sync with whatever the user clicks.
function toggleTheme() { theme.toggle(); }
function onNavLink(name) { switchTab(name); }

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
// Focus trap for modals and palette. When a modal/palette is visible,
// Tab/Shift+Tab cycle inside it instead of escaping to the page behind.
const paletteEl = ref(null);
useFocusTrapListener(paletteEl, () => showPalette.value);
function onKeyDown(e) {
  if (e.key === 'Escape') {
    if (verifyTarget.value) { cancelVerify(); return; }
    if (viewTarget.value) { closeModal(); return; }
    if (deleteTarget.value) { cancelDelete(); return; }
    if (showPalette.value) { showPalette.value = false; return; }
  }
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') { e.preventDefault(); showPalette.value = !showPalette.value; nextTick(() => paletteInput.value?.focus()); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === 'd') { e.preventDefault(); toggleTheme(); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '1') { e.preventDefault(); router.push({ name: 'dashboard' }); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '2') { e.preventDefault(); router.push({ name: 'patients' }); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '3') { e.preventDefault(); router.push({ name: 'diagnose' }); return; }
  if ((e.metaKey || e.ctrlKey) && e.key === '4') { e.preventDefault(); router.push({ name: 'records' }); return; }
}

// === Init ===
onMounted(() => {
  // Sync theme state from the DOM in case it was toggled before this
  // component mounted. The pre-mount boot script already applied the
  // class, so this is just a safety re-read.
  theme.syncFromDom();
  loadStats();
  window.addEventListener('keydown', onKeyDown);
  window.addEventListener('beforeprint', beforePrint);
  window.addEventListener('afterprint', afterPrint);
});
onUnmounted(() => {
  window.removeEventListener('keydown', onKeyDown);
  window.removeEventListener('beforeprint', beforePrint);
  window.removeEventListener('afterprint', afterPrint);
});
</script>

<style>
  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

  html { scroll-behavior: smooth; }

  /* === LIGHT THEME — OncoVision design tokens === */
  :root {
    /* Brand */
    --color-brand:        #1A56DB;
    --color-brand-hover:  #1648B8;
    --color-brand-soft:   rgba(26, 86, 219, 0.10);
    --color-ai:           #7C3AED;
    --color-ai-soft:      rgba(124, 58, 237, 0.12);
    --color-malignant:    #DC2626;
    --color-malignant-soft: #FEE2E2;
    --color-benign:       #059669;
    --color-benign-soft:  #D1FAE5;
    --color-warning:      #D97706;
    --color-warning-soft: #FEF3C7;

    /* Surfaces */
    --bg-page:    #F0F4FA;
    --bg-card:    #FFFFFF;
    --bg-subtle:  #E8EEF8;

    /* Text */
    --text-primary:   #0F172A;
    --text-secondary: #475569;
    --text-muted:     #94A3B8;

    /* Borders */
    --border:        #E2E8F0;
    --border-strong: #CBD5E1;

    /* Radii */
    --radius-sm: 6px;
    --radius-md: 10px;
    --radius-lg: 14px;

    /* Typography */
    --font-body: 'Inter', system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;

    /* Back-compat aliases for any code still using old names. */
    --primary: var(--color-brand);
    --primary-soft: var(--color-brand-soft);
    --primary-hover: var(--color-brand-hover);
    --accent: var(--color-ai);
    --danger: var(--color-malignant);
    --danger-soft: var(--color-malignant-soft);
    --safe: var(--color-benign);
    --safe-soft: var(--color-benign-soft);
    --warning: var(--color-warning);
    --warning-soft: var(--color-warning-soft);
    --bg: var(--bg-page);
    --surface: var(--bg-card);
    --subtle: var(--bg-subtle);
    --ink: var(--text-primary);
    --muted: var(--text-secondary);
    --line: var(--border);
    --rose: var(--color-brand);
    --rose-deep: var(--color-brand-hover);
    --rose-soft: var(--color-brand-soft);
    --plum: var(--text-primary);
    --mal: var(--color-malignant);
    --mal-soft: var(--color-malignant-soft);
    --ben: var(--color-benign);
    --ben-soft: var(--color-benign-soft);
    --radius-card: var(--radius-lg);
    --radius-input: var(--radius-md);
    --radius-badge: var(--radius-sm);
    --shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.04);
    --shadow-lg: 0 4px 8px rgba(0, 0, 0, 0.04), 0 16px 32px rgba(0, 0, 0, 0.06);
    --overlay: rgba(15, 23, 42, 0.45);
  }

  /* === DARK THEME — activated via .dark class on <html> === */
  .dark {
    --color-brand:        #3B82F6;
    --color-brand-hover:  #2563EB;
    --color-brand-soft:   rgba(59, 130, 246, 0.14);
    --color-ai:           #A78BFA;
    --color-ai-soft:      rgba(167, 139, 250, 0.14);
    --color-malignant:    #F87171;
    --color-malignant-soft: rgba(248, 113, 113, 0.14);
    --color-benign:       #34D399;
    --color-benign-soft:  rgba(52, 211, 153, 0.14);
    --color-warning:      #FBBF24;
    --color-warning-soft: rgba(251, 191, 36, 0.14);

    --bg-page:    #0D1117;
    --bg-card:    #161B22;
    --bg-subtle:  #1C2333;

    --text-primary:   #F0F6FC;
    --text-secondary: #8B949E;
    --text-muted:     #484F58;

    --border:        #21262D;
    --border-strong: #30363D;

    --shadow: 0 1px 2px rgba(0, 0, 0, 0.3), 0 4px 12px rgba(0, 0, 0, 0.25);
    --shadow-lg: 0 4px 12px rgba(0, 0, 0, 0.4), 0 16px 32px rgba(0, 0, 0, 0.35);
    --overlay: rgba(0, 0, 0, 0.65);
  }

  * { box-sizing: border-box; margin: 0; padding: 0; }
  *:focus-visible { outline: 2px solid var(--primary); outline-offset: 2px; border-radius: 4px; }

  body {
    font-family: "Inter", system-ui, sans-serif;
    background: var(--bg);
    color: var(--ink);
    -webkit-font-smoothing: antialiased;
    line-height: 1.65;
    transition: background 0.2s ease-out, color 0.2s ease-out;
  }

  .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace; }
  h1, h2, h3, h4, .brand-name {
    font-family: "Inter", system-ui, sans-serif;
    font-weight: 600;
    letter-spacing: -0.02em;
    color: var(--ink);
  }

  /* === SKELETON === */
  .skeleton { background: linear-gradient(90deg, var(--skeleton-base) 25%, var(--skeleton-shine) 50%, var(--skeleton-base) 75%); background-size: 200% 100%; animation: shimmer 1.5s infinite; border-radius: 8px; }
  .skeleton-stat-icon { width: 36px; height: 36px; border-radius: 10px; margin-bottom: 12px; }
  .skeleton-stat-value { width: 80px; height: 36px; margin-bottom: 8px; }
  .skeleton-stat-label { width: 100px; height: 14px; }
  .skeleton-row { height: 52px; margin-bottom: 4px; border-radius: 0; }
  @keyframes shimmer { 0% { background-position: -200% 0; } 100% { background-position: 200% 0; } }

  /* The HEADER / NAVBAR styles now live in src/components/Navbar.vue
     (scoped). Keeping the brand markup, theme toggle, and nav-link
     active state in one place fixes the duplicate-logo / both-icons /
     no-active-state bugs. */

  /* === MAIN === */
  main { max-width: 1200px; margin: 28px auto; padding: 0 26px; }
  /* Non-active sections are removed from the layout entirely so they
     don't stack under the active one. The fade-in is a CSS animation
     that fires once per section when it becomes active. */
  section { display: none; }
  section.active {
    display: block;
    animation: page-enter 200ms ease-out;
  }
  @keyframes page-enter {
    from { opacity: 0; transform: translateY(6px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  @media (prefers-reduced-motion: reduce) {
    section.active { animation: none; }
  }

  .head { margin-bottom: 22px; }
  .head h2 { font-size: 28px; font-weight: 600; letter-spacing: -0.02em; color: var(--text-primary); }
  .head p { color: var(--text-secondary); font-size: 14px; margin-top: 4px; }
  .updated { font-style: italic; opacity: .7; font-size: 12px; }

  .card {
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: var(--radius-card);
    box-shadow: var(--shadow);
    transition: background 0.2s ease-out, border-color 0.2s ease-out, box-shadow 0.2s ease-out, transform 0.15s ease-out;
  }
  .card:hover { box-shadow: var(--shadow-lg); }
  .pad { padding: 24px; }

  /* === STAT CARDS === */
  .stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 22px; }
  .stat { padding: 20px; animation: stat-enter .4s ease-out both; }
  @keyframes stat-enter {
    from { opacity: 0; transform: translateY(6px); }
    to   { opacity: 1; transform: translateY(0); }
  }
  .stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
  .stat .label {
    font-size: 12px;
    text-transform: uppercase;
    letter-spacing: 0.06em;
    color: var(--muted);
    font-weight: 500;
  }
  .stat-icon-box { width: 38px; height: 38px; display: flex; align-items: center; justify-content: center; border-radius: 10px; background: var(--subtle); }
  .stat-icon-box svg { width: 18px; height: 18px; color: var(--muted); }
  .stat-icon-box.mal { background: var(--danger-soft); } .stat-icon-box.mal svg { color: var(--danger); }
  .stat-icon-box.ben { background: var(--safe-soft); } .stat-icon-box.ben svg { color: var(--safe); }
  .stat-icon-box.acc { background: var(--primary-soft); } .stat-icon-box.acc svg { color: var(--primary); }
  .stat .value { font-size: 32px; font-weight: 600; color: var(--ink); line-height: 1.2; letter-spacing: -0.02em; }
  .stat .value.mal { color: var(--danger); }
  .stat .value.ben { color: var(--safe); }
  .stat .trend { font-size: 12px; color: var(--muted); margin-top: 6px; display: flex; align-items: center; gap: 4px; }

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
  th {
    text-align: left;
    font-size: 11px;
    letter-spacing: 0.06em;
    text-transform: uppercase;
    color: var(--muted);
    padding: 12px 14px;
    border-bottom: 1px solid var(--line);
    font-weight: 500;
    user-select: none;
    background: transparent;
  }
  th.sortable { cursor: pointer; transition: color 0.15s ease-out; }
  th.sortable:hover { color: var(--ink); }
  .sort-arrow { font-size: 10px; margin-left: 2px; opacity: 0.7; }
  td { padding: 14px; border-bottom: 1px solid var(--line); vertical-align: middle; color: var(--ink); }
  tr:last-child td { border-bottom: 0; }
  tbody tr { transition: background 0.15s ease-out; }
  .dark tbody tr:hover { background: rgba(255,255,255,0.025); }
  tbody tr:hover { background: var(--bg-subtle); }

  /* === BADGES === */
  .badge {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 3px 10px;
    border-radius: var(--radius-badge);
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.01em;
  }
  .badge.large { font-size: 15px; padding: 6px 14px; border-radius: 8px; }
  .badge.mal { background: var(--danger-soft); color: var(--danger); }
  .badge.ben { background: var(--safe-soft); color: var(--safe); }
  .badge.solid.mal { background: var(--danger); color: #fff; }
  .badge.solid.ben { background: var(--safe); color: #fff; }
  .badge.solid.verified { background: var(--safe); color: #fff; display: inline-flex; align-items: center; gap: 4px; }
  .badge.warn { background: var(--warning-soft); color: var(--warning); }
  .badge.neu  { background: var(--subtle); color: var(--muted); }

  /* Status dot — 9999px radius per design spec. */
  .status-dot { display: inline-block; width: 8px; height: 8px; border-radius: 9999px; background: var(--muted); flex-shrink: 0; }
  .status-dot.mal { background: var(--danger); }
  .status-dot.ben { background: var(--safe); }
  .status-dot.warn { background: var(--warning); }

  /* === FORMS === */
  .grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
  label.f {
    display: block;
    font-size: 12px;
    font-weight: 500;
    margin-bottom: 6px;
    color: var(--muted);
    text-transform: uppercase;
    letter-spacing: 0.06em;
  }
  input, select.in {
    width: 100%;
    padding: 10px 14px;
    border: 1px solid var(--line);
    border-radius: var(--radius-input);
    font: inherit;
    background: var(--surface);
    color: var(--ink);
    transition: border-color 0.15s ease-out, box-shadow 0.15s ease-out;
    font-size: 14px;
  }
  .dark input, .dark select.in { background: var(--bg); }
  input::placeholder, select.in::placeholder { color: var(--muted); }
  input:hover, select.in:hover { border-color: var(--muted); }
  input:focus, select.in:focus { outline: 0; border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-soft); }

  /* === BUTTONS === */
  .btn {
    background: var(--primary);
    color: #fff;
    border: 0;
    border-radius: var(--radius-input);
    padding: 10px 18px;
    font: inherit;
    font-weight: 600;
    font-size: 14px;
    cursor: pointer;
    transition: background 0.15s ease-out, transform 0.1s ease-out, box-shadow 0.15s ease-out;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    line-height: 1.2;
  }
  .btn:hover { background: var(--primary-hover); }
  .btn:active { transform: scale(0.98); }
  .btn.ghost { background: transparent; color: var(--ink); border: 1px solid var(--line); }
  .btn.ghost:hover { background: var(--subtle); }
  .btn:disabled { opacity: 0.5; cursor: not-allowed; }
  .btn:disabled:hover { background: var(--primary); }
  .btn.danger-btn { background: var(--danger); }
  .btn.danger-btn:hover { background: #B91C1C; }
  .btn.full { width: 100%; padding: 12px 18px; font-size: 15px; }
  .btn .spinner { width: 14px; height: 14px; border-width: 2px; }

  .table-actions { display: flex; gap: 6px; justify-content: flex-end; }
  .icon-btn { display: inline-flex; align-items: center; justify-content: center; width: 32px; height: 32px; border-radius: 8px; border: 1px solid var(--line); background: var(--surface); color: var(--muted); cursor: pointer; transition: background 0.15s ease-out, color 0.15s ease-out, border-color 0.15s ease-out; }
  .icon-btn:hover { background: var(--subtle); color: var(--ink); border-color: var(--muted); }
  .icon-btn svg { width: 15px; height: 15px; }
  .icon-btn.danger:hover { background: var(--danger-soft); color: var(--danger); border-color: var(--danger-soft); }

  .toolbar { display: flex; gap: 10px; margin-bottom: 18px; flex-wrap: wrap; align-items: center; }
  .toolbar input, .toolbar select.in { width: auto; }
  .toolbar .search { flex: 1 1 240px; }

  /* === DROP ZONE === */
  .file-hidden { opacity: 0; width: 0.1px; height: 0.1px; position: absolute; }
  .drop-zone-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 40px 24px;
    border: 2px dashed var(--line);
    border-radius: var(--radius-card);
    background: var(--bg);
    cursor: pointer;
    transition: border-color 0.18s ease-out, background 0.18s ease-out;
    text-align: center;
    min-height: 220px;
  }
  .drop-zone-content:hover { border-color: var(--primary); background: var(--primary-soft); }
  .drop-zone.hasFile .drop-zone-content { padding: 12px; border-style: solid; }
  .drop-zone.dragging .drop-zone-content { border-color: var(--primary); background: var(--primary-soft); border-style: solid; }
  .drop-icon { width: 40px; height: 40px; color: var(--muted); margin-bottom: 14px; transition: color 0.18s ease-out; }
  .drop-zone-content:hover .drop-icon { color: var(--primary); }
  .drop-text { font-size: 14px; font-weight: 500; color: var(--ink); }
  .drop-sub { font-size: 12px; color: var(--muted); margin-top: 4px; }
  .preview-img { max-width: 100%; max-height: 280px; border-radius: 8px; object-fit: contain; }

  /* === SPINNER === */
  .spinner { display: inline-block; width: 16px; height: 16px; border: 2.5px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; flex-shrink: 0; }
  .btn.ghost .spinner, .spinner.dark { border-color: rgba(0,0,0,0.15); border-top-color: var(--ink); }
  @keyframes spin { to { transform: rotate(360deg); } }

  /* === MISC === */
  .pager {
    display: flex;
    align-items: center;
    gap: 12px;
    justify-content: flex-end;
    margin-top: 18px;
    padding-top: 16px;
    border-top: 1px solid var(--line);
    font-size: 13px;
    color: var(--muted);
    font-weight: 500;
  }
  .pager .page-info { padding: 0 8px; }

  /* === EMPTY STATES === */
  .empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 14px;
    padding: 48px 24px;
    color: var(--muted);
    font-size: 14px;
    text-align: center;
  }
  .empty-state h4 { color: var(--ink); font-size: 16px; font-weight: 600; }
  .empty-state p { max-width: 320px; line-height: 1.6; }
  .empty-illustration { width: 72px; height: 72px; opacity: 0.5; }

  /* === RESULTS === */
  .result { margin-top: 24px; border-left: 6px solid var(--line); border-radius: var(--radius-card); }
  .result.mal { border-left-color: var(--danger); background: linear-gradient(to right, var(--danger-soft) 0%, transparent 100%); }
  .result.ben { border-left-color: var(--safe); background: linear-gradient(to right, var(--safe-soft) 0%, transparent 100%); }
  .result-header { display: flex; align-items: center; gap: 16px; margin-bottom: 12px; }
  .result h3 { font-size: 20px; margin: 0; color: var(--ink); }
  .kv { display: grid; grid-template-columns: repeat(3, 1fr); gap: 24px; margin-top: 16px; padding-top: 16px; border-top: 1px solid var(--line); }
  .kv .k { font-size: 11px; color: var(--muted); text-transform: uppercase; letter-spacing: 0.06em; font-weight: 500; }
  .kv .v { font-size: 16px; font-weight: 600; margin-top: 4px; color: var(--ink); }
  .highlight-pct { font-size: 24px; font-weight: 700; letter-spacing: -0.02em; }
  .mal-color { color: var(--danger); }
  .ben-color { color: var(--safe); }
  .note { font-size: 13px; color: var(--muted); margin-top: 20px; line-height: 1.6; }
  .image-cell { display: flex; align-items: center; gap: 8px; }
  .img-icon { width: 16px; height: 16px; color: var(--muted); flex-shrink: 0; }

  /* === MODALS === */
  .overlay { position: fixed; inset: 0; background: var(--overlay); z-index: 200; animation: fadeIn .2s ease-out; }
  @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
  .modal { position: fixed; inset: 0; display: flex; align-items: center; justify-content: center; z-index: 210; padding: 20px; animation: fadeIn .2s ease-out; }
  .modal-card {
    background: var(--bg-card);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-lg);
    border: 1px solid var(--border);
    max-width: 500px;
    width: 100%;
    max-height: 80vh;
    overflow-y: auto;
    animation: modalIn .25s ease-out;
  }
  .modal.wide .modal-card { max-width: 700px; }
  @keyframes modalIn { from { opacity: 0; transform: scale(0.96) translateY(8px); } to { opacity: 1; transform: scale(1) translateY(0); } }
  .modal-header { display: flex; align-items: center; justify-content: space-between; padding: 20px 24px 0; }
  .modal-header h3 { font-size: 18px; font-weight: 600; color: var(--text-primary); }
  .close-btn { background: none; border: 0; font-size: 22px; color: var(--text-secondary); cursor: pointer; padding: 0 4px; line-height: 1; transition: color 0.15s ease-out; }
  .close-btn:hover { color: var(--text-primary); }
  .modal-body { padding: 16px 24px 24px; }
  .modal-actions { display: flex; gap: 10px; justify-content: flex-end; padding: 16px 24px 24px; }
  .patient-details { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
  .detail-row { display: flex; flex-direction: column; gap: 2px; }
  .dk { font-size: 11px; color: var(--text-secondary); text-transform: uppercase; font-weight: 500; letter-spacing: 0.06em; }
  .dv { font-size: 15px; color: var(--text-primary); font-weight: 500; }
  .mini-table { margin-top: 8px; }
  .mini-table table { font-size: 13px; }
  .mini-table th { padding: 10px 12px; }
  .mini-table td { padding: 10px 12px; }
  h3 + p { color: var(--text-secondary); font-size: 14px; margin-bottom: 16px; }

  /* === COMMAND PALETTE === */
  .palette { position: fixed; top: 20%; left: 50%; transform: translateX(-50%); z-index: 300; width: 480px; max-width: 90vw; background: var(--bg-card); border: 1px solid var(--border); border-radius: var(--radius-lg); box-shadow: var(--shadow-lg); overflow: hidden; animation: modalIn .2s ease-out; }
  .palette-input { display: flex; align-items: center; gap: 10px; padding: 14px 18px; border-bottom: 1px solid var(--border); color: var(--text-secondary); }
  .palette-input input { flex: 1; border: 0; background: none; font: inherit; font-size: 15px; color: var(--text-primary); }
  .palette-input input:focus { outline: 0; }
  .palette-actions { padding: 8px; display: flex; flex-direction: column; gap: 2px; }
  .palette-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 14px; border-radius: var(--radius-sm); border: 0; background: none; font: inherit; font-size: 14px; color: var(--text-primary); cursor: pointer; text-align: left; transition: background 0.1s ease-out; }
  .palette-item:hover { background: var(--bg-subtle); }
  .palette-shortcut { font-size: 12px; color: var(--text-secondary); }

  /* === DARK MODE HARDENING === */
  /* Toolbar sits directly on the page (no .card wrapper). Give it a
     subtle surface + border in dark mode so the filter controls read
     as a distinct group, and so the Filter button's pink has a clean
     dark surface behind it instead of blending with the page bg. */
  .dark .toolbar {
    background: var(--surface);
    border: 1px solid var(--line);
    border-radius: 12px;
    padding: 14px;
  }
  /* Ghost pager buttons: in dark mode the default 1px line border
     becomes hard to see; reinforce it and bump the text to --ink. */
  .dark .btn.ghost {
    background: var(--surface);
    color: var(--ink);
    border-color: var(--line);
  }
  .dark .btn.ghost:hover {
    background: var(--bg);
  }
  /* Empty-state icon: explicit muted stroke so the illustration reads
     against the dark surface. */
  .dark .empty-illustration { opacity: 0.55; }

  /* === PRINT === */
  .print-only { display: none; }
  @media print {
    /* Generated by beforeprint() in App.vue */
    .nav, .toolbar, .pager, .btn, .icon-btn,
    .nav-hamburger, .nav-theme, .nav-lang, .nav-kbd, .nav-avatar { display: none !important; }
    body { background: #ffffff; color: #000000; font-size: 12px; }
    .app-card, .card { box-shadow: none; border: 1px solid #ccc; break-inside: avoid; }
    .badge.solid.mal { background: var(--color-malignant); color: #ffffff; }
    .badge.solid.ben { background: var(--color-benign); color: #ffffff; }
    .badge.solid.verified { background: var(--color-benign); color: #ffffff; }
    /* Force every section visible in print (overrides the opacity-based
       visibility used for animated tab switches). */
    section { opacity: 1 !important; visibility: visible !important; transform: none !important; display: block; page-break-after: always; }
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
  @media (max-width: 900px) {
    .stats { grid-template-columns: repeat(2, 1fr); }
  }
  @media (max-width: 760px) {
    .stats { grid-template-columns: 1fr 1fr; }
    .grid2 { grid-template-columns: 1fr; }
    .kv { grid-template-columns: 1fr; }
    .patient-details { grid-template-columns: 1fr; }
    .chart-section { flex-direction: column; align-items: center; }
    main { padding: 0 16px; margin: 20px auto; }
  }
  @media (max-width: 480px) {
    .stats { grid-template-columns: 1fr; }
  }
  @media (prefers-reduced-motion: reduce) {
    *, *::before, *::after { animation-duration: 0.01ms !important; transition-duration: 0.01ms !important; }
  }
</style>
