/**
 * Shared formatting helpers.
 *
 * `pct` guards against null/NaN so an empty row renders as "—" instead
 * of "NaN%". `pretty` converts snake_case labels into Title Case so the
 * ML output (e.g. "ductal_carcinoma") reads as "Ductal Carcinoma".
 */
export const pct = (x) => (x == null || Number.isNaN(x)) ? '—' : (x * 100).toFixed(1) + '%';
export const pretty = (s) => s ? s.split('_').map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(' ') : s;
export const pginfo = (t, d) => t('pageinfo').replace('{p}', d.number + 1).replace('{tp}', d.totalPages || 1).replace('{n}', d.totalElements);
