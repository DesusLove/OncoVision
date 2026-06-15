<template>
  <div class="landing">
    <!-- =========================================================
         SECTION 01 — Minimal nav bar (shared with the app shell)
         ========================================================= -->
    <Navbar
      variant="landing"
      :class="{ 'is-scrolled': scrolled }"
      @sign-in="scrollTo('#login-card')"
    />

    <!-- =========================================================
         SECTION 02 — Hero (two-column: copy + login card)
         ========================================================= -->
    <section id="hero" class="hero">
      <div class="hero__inner">
        <div class="hero__copy reveal">
          <span class="hero__eyebrow">{{ t('landing_hero_eyebrow') }}</span>
          <h1 class="hero__headline">
            <span class="hero__line1">{{ t('landing_hero_h1_line1') }}</span><br />
            <span class="hero__line2">{{ t('landing_hero_h1_line2') }}</span>
          </h1>
          <p class="hero__sub">{{ t('landing_hero_sub') }}</p>

          <ul class="hero__badges" aria-label="Model highlights">
            <li class="hero__badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
              <span>Binary classification</span>
            </li>
            <li class="hero__badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
              <span>Malignant-recall optimized</span>
            </li>
            <li class="hero__badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><polyline points="20 6 9 17 4 12"/></svg>
              <span>Subtype analysis pending</span>
            </li>
          </ul>

          <div class="hero__cta">
            <button type="button" class="hero__primary" @click="scrollTo('#login-card')">
              {{ t('landing_hero_cta_primary') }} <span aria-hidden="true">→</span>
            </button>
            <a class="hero__secondary" href="#model-performance" @click.prevent="scrollTo('#model-performance')">
              {{ t('landing_hero_cta_secondary') }} <span aria-hidden="true">↓</span>
            </a>
          </div>
        </div>

        <div id="login-card" class="hero__login reveal" ref="loginCardRef">
          <div class="login-card">
            <header class="login-card__head">
              <div class="login-card__brand">
                <span class="login-card__logo" aria-hidden="true">
                  <svg viewBox="0 0 32 34" fill="none" stroke="currentColor" stroke-width="3.1" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31" />
                    <path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31" />
                  </svg>
                </span>
                <h2 class="login-card__title">{{ t('landing_signin_title') }}</h2>
              </div>
              <p class="login-card__sub">{{ t('landing_signin_sub') }}</p>
            </header>

            <form class="login-form" @submit.prevent="submit" novalidate>
              <label class="field">
                <span class="field__label">{{ t('login_email') }}</span>
                <input
                  ref="emailInput"
                  v-model="email"
                  type="email"
                  inputmode="email"
                  autocomplete="username"
                  autocapitalize="off"
                  autocorrect="off"
                  spellcheck="false"
                  :placeholder="'dr.name@hospital.com'"
                  required
                  :disabled="busy"
                  :aria-invalid="!!error"
                />
              </label>

              <label class="field">
                <span class="field__label">{{ t('login_password') }}</span>
                <div class="field__wrap">
                  <input
                    v-model="password"
                    :type="showPassword ? 'text' : 'password'"
                    autocomplete="current-password"
                    required
                    :disabled="busy"
                    :aria-invalid="!!error"
                  />
                  <button
                    type="button"
                    class="field__toggle"
                    @click="showPassword = !showPassword"
                    :aria-label="showPassword ? 'Hide password' : 'Show password'"
                    :title="showPassword ? 'Hide password' : 'Show password'"
                  >
                    <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  </button>
                </div>
              </label>

              <p v-if="error" class="login-error" role="alert">{{ error }}</p>

              <button type="submit" class="login-submit" :disabled="busy || !email || !password">
                <span v-if="busy" class="login-submit__spinner" aria-hidden="true"></span>
                <span>{{ busy ? t('login_submitting') : t('login_submit') }}</span>
              </button>

              <div class="login-divider" role="separator">
                <span>{{ t('landing_signin_or') }}</span>
              </div>

              <p class="login-demo">{{ t('landing_signin_demo') }}</p>
            </form>

            <footer class="login-card__foot">{{ t('landing_signin_security') }}</footer>
          </div>
        </div>
      </div>
    </section>

    <!-- =========================================================
         SECTION 03 — Stats strip
         ========================================================= -->
    <section class="stats-band reveal" ref="statsBandRef">
      <div class="stats-band__inner">
        <div
          v-for="(s, i) in stats"
          :key="i"
          class="stat"
        >
          <span class="stat__value">{{ displayed[i] }}<span class="stat__suffix">{{ s.suffix }}</span></span>
          <span class="stat__label">{{ t(s.labelKey) }}</span>
        </div>
      </div>
    </section>

    <!-- =========================================================
         SECTION 04 — Feature cards
         ========================================================= -->
    <section class="features reveal" id="features">
      <header class="section-head">
        <h2 class="section-head__title">{{ t('landing_features_title') }}</h2>
        <p class="section-head__sub">{{ t('landing_features_sub') }}</p>
      </header>

      <div class="features__grid">
        <article
          v-for="(f, i) in features"
          :key="i"
          class="feature"
          :style="{ '--reveal-delay': (i * 100) + 'ms' }"
        >
          <span class="feature__icon" :class="`feature__icon--${f.iconKey}`" aria-hidden="true">
            <!-- Microscope -->
            <svg v-if="f.iconKey === 'microscope'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M6 18h8"/>
              <path d="M3 22h18"/>
              <path d="M14 22a7 7 0 1 0 0-14h-1"/>
              <path d="M9 14h2"/>
              <path d="M9 12a2 2 0 0 1-2-2V6h6v4a2 2 0 0 1-2 2Z"/>
              <path d="M12 6V3a1 1 0 0 0-1-1H9a1 1 0 0 0-1 1v3"/>
            </svg>
            <!-- DNA / cells -->
            <svg v-else-if="f.iconKey === 'dna'" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M2 15c6.667-6 13.333 0 20-6"/>
              <path d="M9 22c1.798-1.998 2.518-3.995 2.807-5.993"/>
              <path d="M15 2c-1.798 1.998-2.518 3.995-2.807 5.993"/>
              <path d="M2 9c6.667 6 13.333 0 20 6"/>
              <path d="M16.5 8.5l1.5 1.5"/>
              <path d="M4.5 14.5l1.5 1.5"/>
              <path d="M14.5 5.5l1.5 1.5"/>
              <path d="M6.5 13.5l1.5 1.5"/>
            </svg>
            <!-- Users -->
            <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M22 21v-2a4 4 0 0 0-3-3.87"/>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </span>
          <h3 class="feature__title">{{ t(f.titleKey) }}</h3>
          <p class="feature__body">{{ t(f.bodyKey) }}</p>
          <span class="feature__tag">{{ t(f.tagKey) }}</span>
        </article>
      </div>
    </section>

    <!-- =========================================================
         SECTION 05 — How it works
         ========================================================= -->
    <section class="how reveal" id="how">
      <header class="section-head">
        <h2 class="section-head__title">{{ t('landing_how_title') }}</h2>
        <p class="section-head__sub">{{ t('landing_how_sub') }}</p>
      </header>

      <ol class="how__list">
        <li
          v-for="(step, i) in steps"
          :key="i"
          class="how__step"
        >
          <span class="how__num">{{ i + 1 }}</span>
          <div class="how__text">
            <h3 class="how__title">{{ t(step.titleKey) }}</h3>
            <p class="how__body">{{ t(step.bodyKey) }}</p>
          </div>
          <span v-if="i < steps.length - 1" class="how__connector" aria-hidden="true"></span>
        </li>
      </ol>
    </section>

    <!-- =========================================================
         SECTION 06 — Model performance
         ========================================================= -->
    <section class="perf reveal" id="model-performance">
      <header class="section-head">
        <h2 class="section-head__title">{{ t('landing_model_title') }}</h2>
        <p class="section-head__sub">{{ t('landing_model_sub') }}</p>
      </header>

      <div class="perf__grid">
        <AppCard class="perf-card" v-for="(c, i) in perfCards" :key="i">
          <header class="perf-card__head">
            <h3 class="perf-card__title">{{ t(c.titleKey) }}</h3>
            <span class="perf-card__badge">{{ t(c.badgeKey) }}</span>
          </header>

          <dl class="perf-card__metrics">
            <div class="perf-metric" v-for="(m, j) in c.metrics" :key="j">
              <dt class="perf-metric__k">{{ t(m.labelKey) }}</dt>
              <dd class="perf-metric__v" :class="m.valueClass">{{ m.valueDisplay }}</dd>
            </div>
          </dl>

          <div class="perf-card__bar">
            <ConfidenceBar :value="c.barValue" :variant="c.barVariant" />
          </div>
        </AppCard>
      </div>

      <div class="warning-box" role="note">
        <span class="warning-box__icon" aria-hidden="true">⚠️</span>
        <p class="warning-box__text">{{ t('landing_model_disclaimer') }}</p>
      </div>
    </section>

    <!-- =========================================================
         SECTION 07 — Footer
         ========================================================= -->
    <footer class="lfooter">
      <div class="lfooter__top">
        <div class="lfooter__brand">
          <span class="lfooter__logo" aria-hidden="true">
            <svg viewBox="0 0 32 34" fill="none" stroke="currentColor" stroke-width="3.1" stroke-linecap="round" stroke-linejoin="round">
              <path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31" />
              <path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31" />
            </svg>
          </span>
          <span class="lfooter__tagline">{{ t('landing_footer_tagline') }}</span>
        </div>

        <nav class="lfooter__links" aria-label="Footer">
          <a href="#" @click.prevent>{{ t('landing_footer_github') }}</a>
          <span class="lfooter__sep" aria-hidden="true">·</span>
          <a href="#" @click.prevent>{{ t('landing_footer_dataset') }}</a>
          <span class="lfooter__sep" aria-hidden="true">·</span>
          <a href="#" @click.prevent>{{ t('landing_footer_about') }}</a>
        </nav>
      </div>

      <p class="lfooter__copy">{{ t('landing_footer_copyright') }}</p>
    </footer>
  </div>
</template>

<script setup>
/**
 * Landing + login page. Combines the marketing surface that earns
 * trust (hero, stats, features, how-it-works, performance, footer)
 * with the existing login form. The auth flow is unchanged — only the
 * surrounding chrome is new.
 *
 * Auth state is checked AFTER mount, not in the guard, so we never
 * redirect a still-rendering DOM and cause a flash of login content.
 */
import { ref, reactive, computed, onMounted, onUnmounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '../composables/useAuth.js';
import { useToast } from '../composables/useToast.js';
import { useI18n } from '../composables/useI18n.js';
import AppCard from '../components/AppCard.vue';
import ConfidenceBar from '../components/ConfidenceBar.vue';
import Navbar from '../components/Navbar.vue';

const router = useRouter();
const auth = useAuth();
const toast = useToast();
const { t } = useI18n();

// === Form state (login) ===
const email = ref('');
const password = ref('');
const error = ref('');
const busy = ref(false);
const showPassword = ref(false);
const emailInput = ref(null);
const loginCardRef = ref(null);

// === Nav scroll state ===
// Theme is owned by the shared Navbar component now, so the landing
// page only needs to track the scroll position to drive the
// transparent→solid navbar transition.
const scrolled = ref(false);
function onScroll() { scrolled.value = window.scrollY > 8; }

// === Stats counter animation ===
const statsBandRef = ref(null);
const stats = [
  { target: 0, suffix: '', labelKey: 'landing_stats_images',   format: 'int' },
  { target: 0, suffix: '', labelKey: 'landing_stats_patients', format: 'int' },
  { target: 0, suffix: '', labelKey: 'landing_stats_accuracy', format: 'pct1' },
  { target: 0, suffix: '', labelKey: 'landing_stats_recall',   format: 'int' },
];
const displayed = reactive([0, 0, 0, 0]);
let countersStarted = false;
function easeOutCubic(t) { return 1 - Math.pow(1 - t, 3); }
function animateCounters() {
  if (countersStarted) return;
  countersStarted = true;
  const duration = 1500;
  const start = performance.now();
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduceMotion) {
    stats.forEach((s, i) => { displayed[i] = formatStat(s.target, s.format); });
    return;
  }
  function tick(now) {
    const elapsed = now - start;
    const t = Math.min(1, elapsed / duration);
    const eased = easeOutCubic(t);
    stats.forEach((s, i) => {
      const v = s.target * eased;
      displayed[i] = formatStat(v, s.format);
    });
    if (t < 1) requestAnimationFrame(tick);
  }
  requestAnimationFrame(tick);
}
function formatStat(v, fmt) {
  if (fmt === 'pct1') return v.toFixed(1);
  return Math.round(v).toLocaleString('en-US');
}

// === Reveal-on-scroll (IntersectionObserver) ===
let revealObserver = null;
function setupReveal(rootEl) {
  const targets = rootEl.querySelectorAll('.reveal');
  if (!('IntersectionObserver' in window)) {
    targets.forEach((el) => el.classList.add('revealed'));
    return;
  }
  revealObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (entry.isIntersecting) {
        entry.target.classList.add('revealed');
        revealObserver.unobserve(entry.target);
      }
    }
  }, { threshold: 0.12, rootMargin: '0px 0px -8% 0px' });
  targets.forEach((el) => revealObserver.observe(el));
}

// === Stats observer (separate — also fires the counter) ===
let statsObserver = null;
function setupStatsObserver() {
  if (!statsBandRef.value) return;
  if (!('IntersectionObserver' in window)) {
    animateCounters();
    return;
  }
  statsObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (entry.isIntersecting) {
        animateCounters();
        statsObserver.disconnect();
        break;
      }
    }
  }, { threshold: 0.4 });
  statsObserver.observe(statsBandRef.value);
}

// === Marketing data (drives the i18n key lookups) ===
const features = [
  { iconKey: 'microscope', titleKey: 'landing_feature_binary_title',   bodyKey: 'landing_feature_binary_body',   tagKey: 'landing_feature_binary_tag' },
  { iconKey: 'dna',        titleKey: 'landing_feature_subtype_title',  bodyKey: 'landing_feature_subtype_body',  tagKey: 'landing_feature_subtype_tag' },
  { iconKey: 'users',      titleKey: 'landing_feature_records_title',  bodyKey: 'landing_feature_records_body',  tagKey: 'landing_feature_records_tag' },
];
const steps = [
  { titleKey: 'landing_how_1_title', bodyKey: 'landing_how_1_body' },
  { titleKey: 'landing_how_2_title', bodyKey: 'landing_how_2_body' },
  { titleKey: 'landing_how_3_title', bodyKey: 'landing_how_3_body' },
];
const perfCards = [
  {
    titleKey: 'landing_model_binary_title',
    badgeKey: 'landing_model_binary_badge',
    barValue: 0,
    barVariant: 'benign',
    metrics: [
      { labelKey: 'landing_model_binary_acc',    valueDisplay: '—',     valueClass: 'muted' },
      { labelKey: 'landing_model_binary_recall', valueDisplay: '—',     valueClass: 'muted' },
      { labelKey: 'landing_model_binary_task',   valueDisplay: '',     valueClass: '', rawKey: 'landing_model_binary_task_val' },
      { labelKey: 'landing_model_binary_data',   valueDisplay: '',     valueClass: '', rawKey: 'landing_model_binary_data_val' },
    ],
  },
  {
    titleKey: 'landing_model_subtype_title',
    badgeKey: 'landing_model_subtype_badge',
    barValue: 0,
    barVariant: 'neutral',
    metrics: [
      { labelKey: 'landing_model_subtype_acc',     valueDisplay: '—',      valueClass: 'muted' },
      { labelKey: 'landing_model_subtype_f1',      valueDisplay: '—',      valueClass: 'muted' },
      { labelKey: 'landing_model_subtype_classes', valueDisplay: '',      valueClass: '', rawKey: 'landing_model_subtype_classes_val' },
      { labelKey: 'landing_model_subtype_data',    valueDisplay: '',      valueClass: '', rawKey: 'landing_model_subtype_data_val' },
    ],
  },
];
// Resolve any metric that needs a raw translated string (Task / Dataset).
for (const c of perfCards) {
  for (const m of c.metrics) {
    if (m.rawKey && !m.valueDisplay) m.valueDisplay = t(m.rawKey);
  }
}

// === Submit ===
async function submit() {
  if (busy.value) return;
  error.value = '';
  busy.value = true;
  try {
    await auth.login(email.value, password.value);
    toast.success(t('login_success'));
    const redirect = router.currentRoute.value.query.redirect;
    router.push(typeof redirect === 'string' ? redirect : { name: 'dashboard' });
  } catch (e) {
    error.value = e.message || 'Login failed';
    password.value = '';
    nextTick(() => emailInput.value?.focus());
  } finally {
    busy.value = false;
  }
}

// === Smooth scroll helper ===
function scrollTo(sel) {
  const el = document.querySelector(sel);
  if (!el) return;
  el.scrollIntoView({ behavior: 'smooth', block: 'start' });
  // Move focus too, for keyboard users — but only if the target is
  // focusable; otherwise focus the login card container.
  nextTick(() => {
    const focusable = el.matches('input,button,a[href],select,textarea')
      ? el
      : (el.querySelector('input,button,a[href]') || el);
    try { focusable.focus({ preventScroll: true }); } catch {}
  });
}

// === Init ===
onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true });
  onScroll();

  // Auth check AFTER mount so we don't flash-login content before the
  // redirect actually fires (per brief: no pre-mount redirect).
  await auth.ensureLoaded();
  if (auth.isAuthenticated.value) {
    const redirect = router.currentRoute.value.query.redirect;
    router.push(typeof redirect === 'string' ? redirect : { name: 'dashboard' });
    return;
  }

  setupReveal(document.querySelector('.landing'));
  setupStatsObserver();

  nextTick(() => emailInput.value?.focus());
});

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll);
  revealObserver?.disconnect();
  statsObserver?.disconnect();
});
</script>

<style scoped>
/* =========================================================
   LANDING PAGE
   ========================================================= */

.landing {
  min-height: 100vh;
  background: var(--bg-page);
  color: var(--text-primary);
  overflow-x: hidden;
}

/* The navbar is now shared via src/components/Navbar.vue. The page
   only needs to make sure the hero sits below the fixed navbar. */

/* =========================================================
   SECTION 02 — Hero
   ========================================================= */
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  padding: 96px 80px 64px;
}
.hero__inner {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 55% 45%;
  gap: 48px;
  align-items: center;
}

.hero__copy { max-width: 560px; }
.hero__eyebrow {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.04em;
  padding: 6px 12px;
  border-radius: 9999px;
  background: var(--landing-eyebrow-bg);
  color: var(--color-ai);
  border: 1px solid var(--landing-eyebrow-border);
  margin-bottom: 20px;
}

.hero__headline {
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1.1;
  margin: 0;
  color: var(--text-primary);
}
.hero__line2 { color: var(--color-brand); }

.hero__sub {
  font-size: 18px;
  line-height: 1.7;
  color: var(--text-secondary);
  max-width: 480px;
  margin: 16px 0 0;
}

.hero__badges {
  list-style: none;
  padding: 0;
  margin: 28px 0 0;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}
.hero__badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
}
.hero__badge + .hero__badge {
  margin-left: 14px;
  padding-left: 14px;
  border-left: 1px solid var(--border);
}
.hero__badge svg {
  width: 14px;
  height: 14px;
  color: var(--color-benign);
}

.hero__cta {
  margin-top: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.hero__primary {
  height: 48px;
  padding: 0 22px;
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
  gap: 8px;
  transition: background 150ms ease-out, transform 150ms ease-out, box-shadow 150ms ease-out;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.hero__primary:hover { background: var(--color-brand-hover); transform: translateY(-1px); box-shadow: 0 4px 12px rgba(26,86,219,0.25); }
.hero__primary:active { transform: translateY(0); }

.hero__secondary {
  color: var(--color-brand);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  position: relative;
  padding: 4px 0;
}
.hero__secondary::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 0;
  height: 1px;
  background: var(--color-brand);
  transition: width 180ms ease-out;
}
.hero__secondary:hover::after { width: 100%; }

/* Login card */
.hero__login {
  display: flex;
  justify-content: center;
}
.login-card {
  width: 100%;
  max-width: 400px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  padding: 28px 28px 22px;
  display: flex;
  flex-direction: column;
}
.login-card__head { text-align: center; margin-bottom: 22px; }
.login-card__brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.login-card__logo {
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-brand);
}
.login-card__logo svg { width: 22px; height: 22px; }
.login-card__title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  letter-spacing: -0.01em;
}
.login-card__sub {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.field { display: flex; flex-direction: column; gap: 6px; }
.field__label {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-primary);
}
.field input {
  width: 100%;
  height: 42px;
  padding: 0 12px;
  background: var(--bg-subtle);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font: inherit;
  font-size: 14px;
  transition: border-color 150ms ease-out, box-shadow 150ms ease-out, background 150ms ease-out;
}
.field input::placeholder { color: var(--text-muted); }
.field input:hover { border-color: var(--border-strong); }
.field input:focus {
  outline: 0;
  border-color: var(--color-brand);
  background: var(--bg-card);
  box-shadow: 0 0 0 3px var(--landing-focus-ring);
}
.field input:disabled { opacity: 0.6; cursor: not-allowed; }

.field__wrap {
  position: relative;
}
.field__wrap input { padding-right: 42px; }
.field__toggle {
  position: absolute;
  top: 50%;
  right: 6px;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: 0;
  color: var(--text-muted);
  cursor: pointer;
  border-radius: var(--radius-sm);
  transition: background 150ms ease-out, color 150ms ease-out;
}
.field__toggle:hover { background: var(--bg-subtle); color: var(--text-primary); }
.field__toggle svg { width: 16px; height: 16px; }

.login-error {
  background: var(--color-malignant-soft);
  color: var(--color-malignant);
  font-size: 13px;
  padding: 8px 12px;
  border-radius: var(--radius-sm);
  margin: 0;
}

.login-submit {
  height: 44px;
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
  gap: 8px;
  transition: background 150ms ease-out, transform 150ms ease-out, box-shadow 150ms ease-out;
  margin-top: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.login-submit:hover:not(:disabled) {
  background: var(--color-brand-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(26,86,219,0.25);
}
.login-submit:disabled { opacity: 0.5; cursor: not-allowed; }
.login-submit__spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.login-divider {
  display: flex;
  align-items: center;
  margin: 6px 0;
  color: var(--text-muted);
  font-size: 12px;
}
.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}
.login-divider span { padding: 0 12px; }

.login-demo {
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
  margin: 0;
  line-height: 1.5;
}

.login-card__foot {
  margin-top: 18px;
  padding-top: 14px;
  border-top: 1px solid var(--border);
  font-size: 11px;
  color: var(--text-muted);
  text-align: center;
  letter-spacing: 0.01em;
}

/* =========================================================
   SECTION 03 — Stats strip
   ========================================================= */
.stats-band {
  background: var(--bg-subtle);
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  padding: 40px 80px;
}
.stats-band__inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  align-items: stretch;
}
.stat {
  padding: 8px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
  border-right: 1px solid var(--border);
}
.stat:last-child { border-right: 0; }
.stat__value {
  font-size: 36px;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--color-brand);
  line-height: 1.1;
  font-variant-numeric: tabular-nums;
}
.stat__suffix {
  font-size: 28px;
  font-weight: 700;
  margin-left: 1px;
}
.stat__label {
  font-size: 13px;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.06em;
  font-weight: 500;
}

/* =========================================================
   SECTION 04 — Features
   ========================================================= */
.features,
.how,
.perf {
  max-width: 1200px;
  margin: 0 auto;
  padding: 80px 24px;
}
.section-head { text-align: center; margin-bottom: 40px; }
.section-head__title {
  font-size: 28px;
  font-weight: 600;
  letter-spacing: -0.02em;
  color: var(--text-primary);
  margin: 0;
}
.section-head__sub {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 8px 0 0;
}

.features__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.feature {
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 28px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: transform 200ms ease-out, box-shadow 200ms ease-out, border-color 200ms ease-out;
}
.feature:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--border-strong);
}
.feature__icon {
  width: 44px;
  height: 44px;
  border-radius: 9999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 4px;
}
.feature__icon svg { width: 22px; height: 22px; }
.feature__icon--microscope { background: var(--color-brand); }
.feature__icon--dna        { background: var(--color-ai); }
.feature__icon--users      { background: var(--color-benign); }

.feature__title {
  font-size: 17px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--text-primary);
  margin: 0;
}
.feature__body {
  font-size: 14px;
  line-height: 1.65;
  color: var(--text-secondary);
  margin: 0;
  flex: 1;
}
.feature__tag {
  align-self: flex-start;
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-subtle);
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  margin-top: 4px;
}

/* =========================================================
   SECTION 05 — How it works
   ========================================================= */
.how__list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  align-items: start;
}
.how__step {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 0 12px;
}
.how__num {
  width: 36px;
  height: 36px;
  border-radius: 9999px;
  background: var(--color-brand);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 14px;
}
.how__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 6px;
}
.how__body {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0;
}
.how__connector {
  position: absolute;
  top: 18px;
  right: -12px;
  width: 24px;
  height: 1px;
  border-top: 1px dashed var(--border);
}

/* =========================================================
   SECTION 06 — Model performance
   ========================================================= */
.perf__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.perf-card { padding: 24px !important; display: flex; flex-direction: column; gap: 18px; }
.perf-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.perf-card__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
.perf-card__badge {
  font-size: 12px;
  font-weight: 500;
  color: var(--text-secondary);
  background: var(--bg-subtle);
  padding: 4px 10px;
  border-radius: 9999px;
}
.perf-card__metrics {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin: 0;
}
.perf-metric {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  font-size: 13px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border);
}
.perf-metric:last-child { border-bottom: 0; padding-bottom: 0; }
.perf-metric__k { color: var(--text-secondary); font-weight: 500; }
.perf-metric__v { color: var(--text-primary); font-weight: 500; font-variant-numeric: tabular-nums; }
.perf-metric__v.ben-color    { color: var(--color-benign); font-weight: 700; }
.perf-metric__v.brand-color  { color: var(--color-brand);  font-weight: 700; }
.perf-metric__v.muted       { color: var(--text-muted);   font-weight: 500; }
.perf-card__bar { margin-top: 4px; }

.warning-box {
  background: var(--landing-warn-bg);
  border: 1px solid var(--landing-warn-border);
  border-radius: var(--radius-md);
  padding: 14px 16px;
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.warning-box__icon { font-size: 16px; line-height: 1.4; }
.warning-box__text {
  font-size: 13px;
  color: var(--landing-warn-text);
  line-height: 1.6;
  margin: 0;
}

/* =========================================================
   SECTION 07 — Footer
   ========================================================= */
.lfooter {
  background: var(--bg-subtle);
  border-top: 1px solid var(--border);
  padding: 40px 80px;
  margin-top: 0;
}
.lfooter__top {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}
.lfooter__brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  color: var(--text-primary);
}
.lfooter__logo {
  width: 28px;
  height: 28px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-brand);
}
.lfooter__logo svg { width: 24px; height: 24px; }
.lfooter__tagline {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}
.lfooter__links {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}
.lfooter__links a {
  color: var(--text-muted);
  text-decoration: none;
  transition: color 150ms ease-out;
}
.lfooter__links a:hover { color: var(--text-primary); }
.lfooter__sep { color: var(--text-muted); }
.lfooter__copy {
  max-width: 1200px;
  margin: 24px auto 0;
  font-size: 12px;
  color: var(--text-muted);
  text-align: center;
}

/* =========================================================
   Reveal animation (IntersectionObserver)
   ========================================================= */
.reveal {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 400ms ease-out, transform 400ms ease-out;
  transition-delay: var(--reveal-delay, 0ms);
}
.reveal.revealed {
  opacity: 1;
  transform: translateY(0);
}
.feature {
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 400ms ease-out, transform 400ms ease-out, box-shadow 200ms ease-out, border-color 200ms ease-out;
  transition-delay: var(--reveal-delay, 0ms);
}
.features__grid .feature.is-shown {
  opacity: 1;
  transform: translateY(0);
}

/* =========================================================
   Responsive
   ========================================================= */
@media (max-width: 1024px) {
  .hero { padding: 96px 40px 64px; }
  .stats-band { padding: 32px 40px; }
}

@media (max-width: 900px) {
  .hero { padding: 96px 32px 56px; }
  .hero__inner {
    grid-template-columns: 1fr;
    gap: 40px;
  }
  .hero__headline { font-size: 38px; }
  .stats-band__inner { grid-template-columns: repeat(2, 1fr); row-gap: 28px; }
  .stat:nth-child(2n) { border-right: 0; }
  .stat { padding: 4px 16px; }
  .features__grid { grid-template-columns: repeat(2, 1fr); }
  .how__list { grid-template-columns: 1fr; gap: 32px; }
  .how__connector { display: none; }
  .perf__grid { grid-template-columns: 1fr; }
  .lfooter { padding: 32px 24px; }
}

@media (max-width: 767px) {
  .hero { padding: 88px 24px 48px; min-height: auto; }
  .hero__headline { font-size: 32px; }
  .hero__sub { font-size: 16px; }
  .features { padding: 56px 24px; }
  .how { padding: 56px 24px; }
  .perf { padding: 56px 24px; }
  .features__grid { grid-template-columns: 1fr; }
  .stats-band { padding: 32px 24px; }
  .stats-band__inner { grid-template-columns: 1fr 1fr; row-gap: 24px; }
  .stat { padding: 4px 8px; }
  .stat__value { font-size: 30px; }
  .lfooter__top { flex-direction: column; align-items: flex-start; }
}

@media (prefers-reduced-motion: reduce) {
  .reveal,
  .feature {
    transition: opacity 200ms ease-out !important;
    transform: none !important;
  }
  .lnav__logo,
  .login-submit__spinner { animation: none !important; }
}
</style>
