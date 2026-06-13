/**
 * Shared formatting helpers.
 *
 * `pct` guards against null/NaN so an empty row renders as "—" instead
 * of "NaN%". It also auto-detects whether the caller passed a probability
 * (0..1) or an already-scaled percent (>1), so both work.
 * `pretty` converts snake_case labels into Title Case so the ML output
 * (e.g. "ductal_carcinoma") reads as "Ductal Carcinoma".
 */
export const pct = (x) => {
  if (x == null || Number.isNaN(x)) return '—';
  // Auto-detect probability (0..1) vs already-scaled percent (>1).
  const value = x <= 1 ? x * 100 : x;
  return value.toFixed(1) + '%';
};
export const pretty = (s) => s ? s.split('_').map((w) => w.charAt(0).toUpperCase() + w.slice(1)).join(' ') : s;
export const pginfo = (t, d) => t('pageinfo').replace('{p}', d.number + 1).replace('{tp}', d.totalPages || 1).replace('{n}', d.totalElements);
