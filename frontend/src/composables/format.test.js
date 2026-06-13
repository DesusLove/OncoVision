import { describe, it, expect } from 'vitest';
import { pct, pretty, pginfo } from './format.js';

describe('pct', () => {
  it('formats a probability as a percentage with 1 decimal', () => {
    expect(pct(0.956)).toBe('95.6%');
  });

  it('returns em-dash for null/NaN', () => {
    expect(pct(null)).toBe('—');
    expect(pct(NaN)).toBe('—');
    expect(pct(undefined)).toBe('—');
  });

  it('passes through values > 1 (already a percent)', () => {
    expect(pct(95.5)).toBe('95.5%');
  });
});

describe('pretty', () => {
  it('converts snake_case to Title Case', () => {
    expect(pretty('ductal_carcinoma')).toBe('Ductal Carcinoma');
  });

  it('passes through empty strings', () => {
    expect(pretty('')).toBe('');
    expect(pretty(null)).toBe(null);
  });

  it('handles single words', () => {
    expect(pretty('benign')).toBe('Benign');
  });
});

describe('pginfo', () => {
  const t = (key) => {
    const dict = { pageinfo: 'Page {p} of {tp} · {n} total' };
    return dict[key] || key;
  };

  it('substitutes placeholders from a PageInfo object', () => {
    const info = pginfo(t, { number: 0, totalPages: 5, totalElements: 47 });
    expect(info).toBe('Page 1 of 5 · 47 total');
  });

  it('defaults totalPages to 1 when missing', () => {
    const info = pginfo(t, { number: 0, totalElements: 0 });
    expect(info).toContain('of 1');
  });
});
