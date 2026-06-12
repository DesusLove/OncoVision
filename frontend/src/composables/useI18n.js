import { ref } from 'vue';
import { I18N } from './i18n.js';

const currentLang = ref(localStorage.getItem('lang') || 'en');
currentLang.value = I18N[currentLang.value] ? currentLang.value : 'en';

export function useI18n() {
  /**
   * Look up a translation key. Falls back to English, then to the raw key
   * itself so a missing translation is at least visible in the UI.
   */
  function t(key) {
    const dict = I18N[currentLang.value] || I18N.en;
    return dict[key] || I18N.en[key] || key;
  }

  function setLang(code) {
    if (!I18N[code]) return;
    currentLang.value = code;
    localStorage.setItem('lang', code);
  }

  return { currentLang, t, setLang };
}
