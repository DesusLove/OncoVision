<template>
  <header class="nav" :class="{ 'nav--landing': isLanding }">
    <div class="nav__inner">
      <!-- =========================================================
           BRAND (logo + wordmark) — the ONLY place this markup lives.
           Both the app navbar and the landing navbar share it.
           ========================================================= -->
      <component
        :is="brandIsRouter ? RouterLink : 'a'"
        :to="brandIsRouter ? { name: 'dashboard' } : undefined"
        :href="brandIsRouter ? undefined : '#hero'"
        @click="onBrandClick"
        class="nav-brand"
      >
        <span class="nav-brand__logo" aria-hidden="true">
          <svg viewBox="0 0 32 34" fill="none" stroke="currentColor" stroke-width="3.1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M16 5 C 11 10 11 17 16 20 C 21 23 21 28 18 31"/>
            <path d="M16 5 C 21 10 21 17 16 20 C 11 23 11 28 14 31"/>
          </svg>
        </span>
        <span class="nav-brand__text">
          <span class="nav-brand__name">OncoVision</span>
          <span v-if="!isLanding && tagline" class="nav-brand__tag">{{ tagline }}</span>
        </span>
      </component>

      <!-- App mode only: hamburger for the mobile drawer -->
      <button
        v-if="!isLanding"
        class="nav-hamburger"
        type="button"
        @click="$emit('toggle-mobile')"
        :aria-label="mobileOpen ? 'Close menu' : 'Open menu'"
        :aria-expanded="!!mobileOpen"
      >
        <span></span><span></span><span></span>
      </button>

      <!-- App mode only: nav links with active state -->
      <nav v-if="!isLanding" class="nav-links" :class="{ 'is-open': mobileOpen }" aria-label="Primary">
        <RouterLink
          v-for="link in navLinks"
          :key="link.name"
          :to="{ name: link.name }"
          class="nav-link"
          active-class="nav-active"
          @click="$emit('navigate', link.name)"
        >
          {{ link.label }}
          <span v-if="link.badge" class="nav-link__badge">{{ link.badge }}</span>
        </RouterLink>
      </nav>

      <!-- Right-side actions -->
      <div class="nav-actions">
        <!-- App mode: ⌘K hint (desktop only) -->
        <span v-if="!isLanding" class="nav-kbd" aria-hidden="true">⌘K</span>

        <!-- Dark mode toggle. Sun and moon are mutually exclusive via
             v-if/v-else so only one is in the DOM at any time. -->
        <button
          type="button"
          class="nav-theme"
          @click="onToggleTheme"
          :title="isDark ? t('dm_light') : t('dm_dark')"
          :aria-label="isDark ? t('dm_light') : t('dm_dark')"
          :aria-pressed="isDark"
        >
          <svg
            v-if="isDark"
            class="nav-theme__icon nav-theme__icon--moon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
          </svg>
          <svg
            v-else
            class="nav-theme__icon nav-theme__icon--sun"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <circle cx="12" cy="12" r="5"/>
            <line x1="12" y1="1"  x2="12" y2="3"/>
            <line x1="12" y1="21" x2="12" y2="23"/>
            <line x1="4.22"  y1="4.22"  x2="5.64"  y2="5.64"/>
            <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
            <line x1="1"  y1="12" x2="3"  y2="12"/>
            <line x1="21" y1="12" x2="23" y2="12"/>
            <line x1="4.22"  y1="19.78" x2="5.64"  y2="18.36"/>
            <line x1="18.36" y1="5.64"  x2="19.78" y2="4.22"/>
          </svg>
        </button>

        <!-- App mode: logout (only when authenticated) -->
        <button
          v-if="!isLanding && authenticated"
          type="button"
          class="nav-icon-btn nav-icon-btn--logout"
          :title="t('login_logout')"
          :aria-label="t('login_logout')"
          @click="$emit('logout')"
        >
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" width="16" height="16" aria-hidden="true">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>

        <!-- App mode: language selector -->
        <div v-if="!isLanding" class="nav-lang">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true">
            <circle cx="12" cy="12" r="9"/>
            <path d="M3 12h18M12 3c2.5 2.7 2.5 15.3 0 18M12 3c-2.5 2.7-2.5 15.3 0 18"/>
          </svg>
          <select
            :value="currentLang"
            @change="$emit('change-lang', $event.target.value)"
            aria-label="Language"
          >
            <option value="en">English</option>
            <option value="zh">中文</option>
          </select>
        </div>

        <!-- App mode: user avatar -->
        <button
          v-if="!isLanding && authenticated"
          type="button"
          class="nav-avatar"
          :title="userDisplayName"
          @click="$emit('open-user-menu')"
        >
          <span>{{ userInitials }}</span>
        </button>

        <!-- Landing mode: sign-in anchor scrolls to #login-card -->
        <a
          v-if="isLanding"
          href="#login-card"
          class="nav-signin"
          @click.prevent="$emit('sign-in')"
        >{{ t('login_submit') }}</a>
      </div>
    </div>
  </header>
</template>

<script setup>
/**
 * Single source of truth for the top navbar. Two visual variants:
 *
 *   variant="app"      — full app chrome: brand + 4 nav links + theme
 *                        + language + (logout + avatar when authed)
 *   variant="landing"  — minimal: brand + theme + "Sign in" anchor
 *
 * The brand markup (logo + wordmark + tagline) lives in exactly ONE
 * place — this component — and is shared between variants via the
 * `tagline` prop. Bug 1 fix: no duplicate logo anywhere.
 *
 * The theme toggle uses v-if/v-else on the icon, so only one of sun
 * /moon is in the DOM at a time. Bug 2 fix: never both visible.
 *
 * Nav links use RouterLink with active-class="nav-active" plus the
 * default router-link-exact-active class — Bug 3 fix.
 */
import { computed, onMounted } from 'vue';
import { RouterLink } from 'vue-router';
import { useI18n } from '../composables/useI18n.js';
import { useTheme } from '../composables/useTheme.js';

const props = defineProps({
  /** 'app' (default) or 'landing' */
  variant: { type: String, default: 'app' },
  /** Tagline shown under the wordmark in app mode */
  tagline: { type: String, default: '' },
  /** Mobile drawer open state (controlled by parent) */
  mobileOpen: { type: Boolean, default: false },
  /** User is signed in (controls logout/avatar visibility) */
  authenticated: { type: Boolean, default: false },
  /** User display name for avatar title */
  userDisplayName: { type: String, default: '' },
  /** Two-letter initials shown in the avatar */
  userInitials: { type: String, default: '?' },
  /** 'en' | 'zh' */
  currentLang: { type: String, default: 'en' },
  /** { patients: Number, records: Number } */
  badges: { type: Object, default: () => ({ patients: 0, records: 0 }) },
});

defineEmits([
  'toggle-mobile',     // (app mode) user tapped the hamburger
  'navigate',          // (app mode) a nav link was clicked — payload: route name
  'logout',            // (app mode) user tapped the logout button
  'open-user-menu',    // (app mode) user tapped the avatar
  'change-lang',       // (app mode) user picked a language
  'toggle-theme',      // user toggled dark mode (parent can sync command palette)
  'sign-in',           // (landing mode) user clicked the sign-in anchor
]);

const { t } = useI18n();
const { isDark, syncFromDom, toggle } = useTheme();

const isLanding = computed(() => props.variant === 'landing');
const brandIsRouter = computed(() => !isLanding.value);

// Always rebuild the navLinks from current i18n so toggling language
// updates the labels without remount.
const navLinks = computed(() => ([
  { name: 'dashboard', label: t('nav_dashboard'), badge: 0 },
  { name: 'patients',  label: t('nav_patients'),  badge: props.badges.patients || 0 },
  { name: 'diagnose',  label: t('nav_diagnose'),  badge: 0 },
  { name: 'records',   label: t('nav_records'),   badge: props.badges.records  || 0 },
]));

function onBrandClick(e) {
  if (isLanding.value) {
    // The landing anchor just scrolls to #hero; the smooth scroll
    // logic lives in LandingLogin. We just emit so the parent can
    // run its own scroll handler if it wants.
    e.preventDefault();
    document.getElementById('hero')?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }
}

function onToggleTheme() {
  toggle();
}

// On mount, re-sync from the DOM in case another component (or the
// boot script) flipped the class between module import and now.
onMounted(() => { syncFromDom(); });
</script>

<style scoped>
/* =========================================================
   NAV — outer shell
   ========================================================= */
.nav {
  height: 56px;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
  transition: background 150ms ease-out, border-color 150ms ease-out;
}
/* Landing variant: transparent at scroll-top, solid after the user
   scrolls. The parent toggles a `.scrolled` class on <.nav> if it
   wants this behaviour; without that class the nav stays solid. */
.nav--landing:not(.is-scrolled) {
  background: transparent;
  border-bottom-color: transparent;
}
.nav--landing.is-scrolled {
  background: var(--bg-card);
  border-bottom-color: var(--border);
}

.nav__inner {
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 24px;
}

/* =========================================================
   BRAND — logo + wordmark (+ optional tagline)
   This is the SINGLE source of truth for the brand markup.
   ========================================================= */
.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--text-primary);
  flex-shrink: 0;
}
.nav-brand__logo {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--color-brand);
  animation: heartbeat 3s ease-in-out infinite;
}
.nav-brand__logo svg { width: 28px; height: 28px; }
.nav-brand__text { display: flex; flex-direction: column; line-height: 1.2; min-width: 0; }
.nav-brand__name {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: -0.01em;
  color: var(--text-primary);
}
.nav-brand__tag {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 1px;
}

@keyframes heartbeat {
  0%, 100% { transform: scale(1); }
  50%      { transform: scale(1.06); }
}

/* =========================================================
   NAV LINKS — with active state (Bug 3 fix)
   ========================================================= */
.nav-links {
  display: flex;
  gap: 4px;
  margin-left: 24px;
}
.nav-link {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 400;
  border-radius: var(--radius-sm);
  border-bottom: 2px solid transparent;
  transition: color 150ms ease, background 150ms ease, border-color 150ms ease;
}
.nav-link:hover {
  color: var(--text-primary);
  background: var(--bg-subtle);
}
/* Default router-link-active plus our opt-in `nav-active` class on
   the link itself. Using both gives us exact-match via the router
   AND a safe fallback for any host override. */
.nav-link.router-link-active,
.nav-link.nav-active {
  color: var(--color-brand);
  border-bottom-color: var(--color-brand);
  font-weight: 500;
}
.nav-link.router-link-exact-active {
  color: var(--color-brand);
  border-bottom-color: var(--color-brand);
  font-weight: 500;
}
.nav-link__badge {
  background: var(--bg-subtle);
  color: var(--text-secondary);
  font-size: 11px;
  padding: 1px 7px;
  border-radius: 9999px;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}
.nav-link.nav-active .nav-link__badge,
.nav-link.router-link-exact-active .nav-link__badge {
  background: var(--color-brand-soft);
  color: var(--color-brand);
}

/* =========================================================
   ACTIONS (right side)
   ========================================================= */
.nav-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
}
.nav-kbd {
  font-size: 11px;
  color: var(--text-muted);
  font-weight: 500;
  padding: 3px 8px;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-subtle);
  display: none;
}
@media (min-width: 900px) { .nav-kbd { display: inline-block; } }

/* Theme toggle — only one icon in the DOM at a time (v-if/v-else),
   but we still get a smooth swap because Vue swaps the element on
   the same paint cycle. Adding a brief opacity transition gives
   the swap a fade-in. */
.nav-theme {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 150ms ease-out, border-color 150ms ease-out, color 150ms ease-out;
}
.nav-theme:hover {
  background: var(--bg-subtle);
  border-color: var(--border-strong);
  color: var(--text-primary);
}
.nav-theme__icon {
  width: 16px;
  height: 16px;
  display: block;
  animation: icon-swap 180ms ease-out;
}
.nav-theme__icon--sun  { transform: rotate(0deg); }
.nav-theme__icon--moon { transform: rotate(0deg); }
@keyframes icon-swap {
  from { opacity: 0; transform: rotate(-90deg) scale(0.6); }
  to   { opacity: 1; transform: rotate(0deg)   scale(1); }
}

/* Icon button (logout) */
.nav-icon-btn {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 150ms ease-out, color 150ms ease-out, border-color 150ms ease-out;
}
.nav-icon-btn:hover { background: var(--bg-subtle); }
.nav-icon-btn--logout:hover {
  background: var(--color-malignant-soft);
  color: var(--color-malignant);
  border-color: var(--color-malignant-soft);
}

/* Language selector */
.nav-lang {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-muted);
}
.nav-lang svg { width: 15px; height: 15px; }
.nav-lang select {
  background: transparent;
  color: var(--text-primary);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 6px 8px;
  font: inherit;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 150ms ease-out;
}
.nav-lang select:hover { border-color: var(--border-strong); }
.nav-lang select option { color: var(--text-primary); background: var(--bg-card); }

/* Avatar */
.nav-avatar {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: 9999px;
  background: linear-gradient(135deg, var(--color-brand), var(--color-ai));
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 150ms ease-out;
}
.nav-avatar:hover { transform: translateY(-1px); }

/* Sign-in anchor (landing mode) */
.nav-signin {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-brand);
  text-decoration: none;
  padding: 8px 14px;
  border-radius: var(--radius-sm);
  transition: background 150ms ease-out;
}
.nav-signin:hover { background: var(--color-brand-soft); }

/* Hamburger (mobile) */
.nav-hamburger {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: 0;
  cursor: pointer;
  padding: 6px;
  color: var(--text-primary);
  margin-left: auto;
  order: 3;
}
.nav-hamburger span {
  display: block;
  width: 22px;
  height: 2px;
  background: currentColor;
  border-radius: 99px;
  transition: 200ms ease-out;
}

/* =========================================================
   MOBILE
   ========================================================= */
@media (max-width: 768px) {
  .nav__inner { gap: 12px; }
  .nav-hamburger {
    display: flex;
    order: 4;
    margin-left: 0;
  }
  .nav-actions { order: 3; }
  .nav-brand__tag { display: none; }   /* save space; tagline shows on larger screens */

  /* Drawer for nav links */
  .nav-links {
    display: none;
    position: absolute;
    top: 56px;
    left: 0;
    right: 0;
    flex-direction: column;
    background: var(--bg-card);
    border-bottom: 1px solid var(--border);
    padding: 8px 12px 12px;
    gap: 2px;
    margin-left: 0;
    box-shadow: var(--shadow);
    order: 5;
  }
  .nav-links.is-open { display: flex; }
  .nav-link {
    padding: 10px 12px;
    width: 100%;
    border-bottom: 2px solid transparent;
  }
  .nav-link.nav-active,
  .nav-link.router-link-exact-active {
    background: var(--color-brand-soft);
    border-bottom-color: transparent;
  }
  .nav-kbd { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .nav-brand__logo { animation: none; }
  .nav-theme__icon { animation: none; }
}
</style>
