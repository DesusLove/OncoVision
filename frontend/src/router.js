import { createRouter, createWebHashHistory } from 'vue-router';

/**
 * OncoVision uses hash-based routing so deep links work without any
 * server-side fallback configuration (the frontend is served as a
 * static SPA via Vite). Each tab is a route; the Records tab accepts
 * an optional `label` query parameter to deep-link a filtered view
 * (e.g. `/records?label=Malignant`).
 *
 * The legacy `activeTab` ref was removed in favour of route-driven
 * navigation — see App.vue.
 */
const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard', component: () => import('./views/DashboardView.vue') },
  { path: '/patients',  name: 'patients',  component: () => import('./views/PatientsView.vue') },
  { path: '/diagnose',  name: 'diagnose',  component: () => import('./views/DiagnoseView.vue') },
  { path: '/records',   name: 'records',   component: () => import('./views/RecordsView.vue') },
  // Catch-all: bounce unknown routes back to dashboard.
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
];

export const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() { return { top: 0 }; },
});

export const ROUTE_NAMES = {
  dashboard: 'dashboard',
  patients: 'patients',
  diagnose: 'diagnose',
  records: 'records',
};
