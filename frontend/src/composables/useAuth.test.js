import { describe, it, expect, beforeEach, vi } from 'vitest';
import { useAuth } from './useAuth.js';

describe('useAuth', () => {
    beforeEach(async () => {
        // Force-reimport so the module-level `user` ref is reset between tests.
        vi.resetModules();
        await vi.importActual('./useAuth.js');
    });

    it('reports not authenticated before ensureLoaded() resolves', async () => {
        // Default fetch returns 401 since no session cookie is sent.
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: false, status: 401, json: async () => null,
        });
        const auth = useAuth();
        await auth.ensureLoaded();
        expect(auth.isAuthenticated.value).toBe(false);
        expect(auth.user.value).toBe(null);
        expect(auth.ready.value).toBe(true);
    });

    it('reports authenticated when /me returns 200', async () => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true, status: 200, json: async () => ({ username: 'admin', role: 'ADMIN', displayName: 'Admin' }),
        });
        const auth = useAuth();
        await auth.ensureLoaded();
        expect(auth.isAuthenticated.value).toBe(true);
        expect(auth.user.value.username).toBe('admin');
        expect(auth.user.value.role).toBe('ADMIN');
    });

    it('login throws a friendly error on 401', async () => {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: false, status: 401, json: async () => null,
        });
        const { useAuth: fresh } = await import('./useAuth.js');
        const auth = fresh();
        await expect(auth.login('admin', 'wrong')).rejects.toThrow(/Invalid username or password/);
        expect(auth.isAuthenticated.value).toBe(false);
    });

    it('logout clears local state even if the server call fails', async () => {
        // First /me succeeds, then logout fails.
        let calls = 0;
        globalThis.fetch = vi.fn().mockImplementation(async (url) => {
            calls++;
            if (calls === 1) {
                return { ok: true, status: 200, json: async () => ({ username: 'admin', role: 'ADMIN' }) };
            }
            return { ok: false, status: 500, json: async () => ({ detail: 'server down' }) };
        });
        const auth = useAuth();
        await auth.ensureLoaded();
        expect(auth.isAuthenticated.value).toBe(true);
        await auth.logout();
        expect(auth.isAuthenticated.value).toBe(false);
        expect(auth.user.value).toBe(null);
    });
});
