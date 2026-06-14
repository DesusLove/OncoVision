<template>
  <div class="login-shell">
    <div class="login-card">
      <div class="login-brand" aria-hidden="true">
        <span class="login-brand__mark">
          <svg viewBox="0 0 32 34" fill="none" stroke="currentColor" stroke-width="3.1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31"/>
            <path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31"/>
          </svg>
        </span>
        <div>
          <h1 class="login-brand__name">OncoVision</h1>
          <p class="login-brand__tag">{{ t('login_tagline') }}</p>
        </div>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <label class="login-field">
          <span class="login-field__label">{{ t('login_username') }}</span>
          <input
            ref="usernameInput"
            v-model="username"
            type="text"
            autocomplete="username"
            autocapitalize="off"
            autocorrect="off"
            spellcheck="false"
            required
            :disabled="busy"
            :aria-invalid="!!error"
          />
        </label>
        <label class="login-field">
          <span class="login-field__label">{{ t('login_password') }}</span>
          <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            required
            :disabled="busy"
            :aria-invalid="!!error"
          />
        </label>

        <p v-if="error" class="login-error" role="alert">{{ error }}</p>

        <button type="submit" class="login-submit" :disabled="busy || !username || !password">
          <span v-if="busy" class="login-submit__spinner" aria-hidden="true"></span>
          {{ busy ? t('login_submitting') : t('login_submit') }}
        </button>

        <p class="login-hint">{{ t('login_hint') }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth.js';
import { useToast } from '../composables/useToast.js';
import { useI18n } from '../composables/useI18n.js';

const router = useRouter();
const auth = useAuth();
const toast = useToast();
const { t } = useI18n();

const username = ref('');
const password = ref('');
const error = ref('');
const busy = ref(false);
const usernameInput = ref(null);

async function submit() {
  if (busy.value) return;
  error.value = '';
  busy.value = true;
  try {
    await auth.login(username.value, password.value);
    toast.success(t('login_success'));
    const redirect = router.currentRoute.value.query.redirect;
    router.push(typeof redirect === 'string' ? redirect : { name: 'dashboard' });
  } catch (e) {
    error.value = e.message || 'Login failed';
    password.value = '';
    nextTick(() => usernameInput.value?.focus());
  } finally {
    busy.value = false;
  }
}

onMounted(async () => {
  await auth.ensureLoaded();
  if (auth.isAuthenticated.value) {
    const redirect = router.currentRoute.value.query.redirect;
    router.push(typeof redirect === 'string' ? redirect : { name: 'dashboard' });
  } else {
    nextTick(() => usernameInput.value?.focus());
  }
});
</script>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, var(--bg-page), var(--bg-subtle));
}
.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 32px;
}
.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}
.login-brand__mark {
  width: 44px;
  height: 44px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-brand);
  background: var(--color-brand-soft);
  border-radius: 12px;
  flex-shrink: 0;
  animation: heartbeat 3s ease-in-out infinite;
}
@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.06); }
}
.login-brand__name { font-size: 22px; font-weight: 600; letter-spacing: -0.02em; margin: 0; color: var(--text-primary); }
.login-brand__tag  { font-size: 12px; color: var(--text-muted); margin: 2px 0 0; }

.login-form { display: flex; flex-direction: column; gap: 14px; }
.login-field { display: flex; flex-direction: column; gap: 6px; }
.login-field__label {
  font-size: 11px;
  font-weight: 500;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.login-field input {
  height: 40px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-primary);
  font: inherit;
  font-size: 14px;
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out;
}
.login-field input:focus {
  outline: 0;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px var(--color-brand-soft);
}
.login-field input:disabled { opacity: 0.6; cursor: not-allowed; }

.login-error {
  background: var(--color-malignant-soft);
  color: var(--color-malignant);
  font-size: 13px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  margin: 0;
}

.login-submit {
  height: 42px;
  background: var(--color-brand);
  color: #fff;
  border: 0;
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
  margin-top: 4px;
}
.login-submit:hover:not(:disabled) { background: var(--color-brand-hover); transform: translateY(-1px); }
.login-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.login-submit__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: login-spin 0.7s linear infinite;
}
@keyframes login-spin { to { transform: rotate(360deg); } }

.login-hint {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  margin: 8px 0 0;
}
</style>
