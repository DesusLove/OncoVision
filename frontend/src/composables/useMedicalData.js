import { ref, reactive, computed } from 'vue';
import { apiCall } from './useApi.js';
import { useToast } from './useToast.js';

/**
 * Centralised data store for OncoVision. Owns the records/patients/stats
 * state and the loaders that fetch from the backend. Returns reactive
 * refs and async functions so App.vue can wire them up to view props
 * without owning the fetch logic itself.
 *
 * This is the first step of the App.vue split — moving data loading out
 * of the shell component. Future work: split into useStats / usePatients
 * / useRecords / useDiagnose for finer-grained reactivity.
 */
export function useMedicalData(opts = {}) {
  const toast = useToast();
  // Optional callbacks fired when loaders finish — App.vue uses them to
  // keep the navbar badge counts in sync without owning the load logic.
  const onPatientsLoaded = opts.onPatientsLoaded;
  const onRecordsLoaded = opts.onRecordsLoaded;

  // === Stats ===
  const statsLoading = ref(true);
  const statsData = ref({});
  const recentRecords = ref([]);
  const lastUpdated = ref('');

  async function loadStats() {
    statsLoading.value = true;
    try {
      statsData.value = await apiCall('/api/stats');
      lastUpdated.value = new Date().toLocaleTimeString();
    } catch (e) { console.error(e); }
    finally { statsLoading.value = false; }
    // Recent activity is loaded with the stats so the dashboard table
    // is populated in the same round-trip.
    try {
      const r = await apiCall('/api/records?page=0&size=5&sortBy=testDate&sortDir=desc');
      recentRecords.value = Array.isArray(r.content) ? r.content : [];
    } catch (e) { /* keep previous value */ }
  }

  // === Patients ===
  const patientsLoading = ref(false);
  const patients = ref([]);
  const patientOptions = ref([]);
  const patientSort = reactive({ field: 'id', dir: 'asc' });
  const patientSearch = ref('');
  const pPage = ref(0);
  const pPageObj = reactive({ number: 0, totalPages: 1, totalElements: 0, first: true, last: true });

  let searchTimer = 0;
  function debouncedSearch() {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadPatients(0), 300);
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
      // Update the badge count so the nav reflects the current total.
      if (typeof onPatientsLoaded === 'function') onPatientsLoaded(d.totalElements || 0);
    } catch (e) { toast.error(e.message); }
    finally { patientsLoading.value = false; }
  }

  async function loadPatientOptions() {
    try {
      const d = await apiCall('/api/patients/options');
      patientOptions.value = d.options || [];
      if (d.truncated) {
        toast.error('Patient list truncated to ' + (d.cap || patientOptions.value.length) + ' — search to narrow.');
      }
    } catch (e) { toast.error(e.message); }
  }

  function togglePatientSort(field) {
    if (patientSort.field === field) patientSort.dir = patientSort.dir === 'asc' ? 'desc' : 'asc';
    else { patientSort.field = field; patientSort.dir = 'asc'; }
    loadPatients(pPage.value);
  }

  // === Records ===
  const recordsLoading = ref(false);
  const records = ref([]);
  const recordsSort = reactive({ field: 'testDate', dir: 'desc' });
  const rFilters = reactive({ label: '', from: '', to: '' });
  const rPage = ref(0);
  const rPageObj = reactive({ number: 0, totalPages: 1, totalElements: 0, first: true, last: true });

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
      if (typeof onRecordsLoaded === 'function') onRecordsLoaded(d.totalElements || 0);
    } catch (e) {
      records.value = [];
      toast.error(e.message);
    }
    finally { recordsLoading.value = false; }
  }

  function toggleRecSort(field) {
    if (recordsSort.field === field) recordsSort.dir = recordsSort.dir === 'asc' ? 'desc' : 'asc';
    else { recordsSort.field = field; recordsSort.dir = 'asc'; }
    loadRecords(rPage.value);
  }

  // === Diagnose ===
  const isAnalyzing = ref(false);
  const diagResult = ref(null);

  async function runDiagnose(form) {
    if (!form?.patientId) { toast.error('Select a patient first.'); return; }
    if (!form.imageFile) { toast.error('Choose an image.'); return; }
    const fd = new FormData();
    fd.append('image', form.imageFile);
    if (form.date) fd.append('testDate', form.date);
    isAnalyzing.value = true;
    diagResult.value = null;
    try {
      diagResult.value = await apiCall(`/api/patients/${form.patientId}/diagnose`, { method: 'POST', body: fd });
      toast.success('Diagnosis saved successfully.');
    } catch (e) { toast.error(e.message); }
    finally { isAnalyzing.value = false; }
  }

  return {
    // stats
    statsLoading, statsData, recentRecords, lastUpdated, loadStats,
    // patients
    patientsLoading, patients, patientOptions, patientSort, patientSearch,
    pPage, pPageObj, loadPatients, loadPatientOptions, togglePatientSort, debouncedSearch,
    // records
    recordsLoading, records, recordsSort, rFilters, rPage, rPageObj,
    loadRecords, toggleRecSort,
    // diagnose
    isAnalyzing, diagResult, runDiagnose,
  };
}
