import { ref, computed } from 'vue';
import { apiCall } from './useApi.js';

/**
 * Auth state. Single source of truth for "who is logged in". Mounted once
 * at app startup via App.vue's setup; the SPA reads `user` from here to
 * decide whether to show the login page or the main app.
 *
 * Uses session cookies (HttpOnly, set by the backend). The frontend
 * never sees the cookie value — `credentials: 'include'` is added to
 * every fetch automatically via useApi.js.
 */

const user = ref(null);
const ready = ref(false);
let inFlight = null;

async function fetchMe() {
    try {
        const data = await apiCall('/api/auth/me', { method: 'GET' });
        user.value = data;
    } catch {
        user.value = null;
    } finally {
        ready.value = true;
    }
}

async function ensureLoaded() {
    if (inFlight) return inFlight;
    inFlight = fetchMe().finally(() => { inFlight = null; });
    return inFlight;
}

export function useAuth() {
    return {
        user: computed(() => user.value),
        ready: computed(() => ready.value),
        isAuthenticated: computed(() => user.value !== null),

        async login(username, password) {
            // We can't use apiCall() because it throws on non-2xx and the
            // login endpoint returns 401 on bad creds. Use fetch directly.
            const res = await fetch('/api/auth/login', {
                method: 'POST',
                credentials: 'include',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: new URLSearchParams({ username, password }).toString(),
            });
            if (!res.ok) {
                if (res.status === 401) throw new Error('Invalid username or password');
                throw new Error('Login failed (' + res.status + ')');
            }
            await fetchMe();
            return user.value;
        },

        async logout() {
            try {
                await apiCall('/api/auth/logout', { method: 'POST' });
            } catch (e) {
                // Even if the server call fails, clear local state so the
                // SPA doesn't leave the user in a half-logged-in limbo.
            }
            user.value = null;
        },

        /** Reload the cached user (e.g. after profile update). */
        async refresh() {
            await fetchMe();
        },

        /** Call once at app startup to determine the initial auth state. */
        ensureLoaded,
    };
}
