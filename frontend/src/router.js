import { createRouter, createWebHashHistory } from 'vue-router';

/**
 * Routes. The login page is the catch-all entry point: every other
 * route is protected by the auth guard below.
 */
const routes = [
  { path: '/login', name: 'login', component: () => import('./views/LandingLogin.vue'), meta: { public: true } },
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', name: 'dashboard', component: () => import('./views/DashboardView.vue') },
  { path: '/patients',  name: 'patients',  component: () => import('./views/PatientsView.vue') },
  { path: '/diagnose',  name: 'diagnose',  component: () => import('./views/DiagnoseView.vue') },
  { path: '/records',   name: 'records',   component: () => import('./views/RecordsView.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
];

export const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() { return { top: 0 }; },
});

/**
 * Global auth guard. Public routes (login) pass through; everything
 * else requires an authenticated user. The auth state is hydrated by
 * useAuth.ensureLoaded() before the first navigation resolves, so by
 * the time this guard runs we know whether the user is logged in.
 */
import { useAuth } from './composables/useAuth.js';

router.beforeEach(async (to) => {
  const auth = useAuth();
  if (!auth.ready.value) await auth.ensureLoaded();
  if (to.meta.public) return true;
  if (!auth.isAuthenticated.value) {
    return { name: 'login', query: { redirect: to.fullPath } };
  }
  return true;
});

export const ROUTE_NAMES = {
  dashboard: 'dashboard',
  patients: 'patients',
  diagnose: 'diagnose',
  records: 'records',
  login: 'login',
};
