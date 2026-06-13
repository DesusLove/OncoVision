import { describe, it, expect } from 'vitest';
import { describeSubtype, SUBTYPES } from './subtypeDescriptions.js';

describe('describeSubtype', () => {
  it('returns the description for a lowercase wire value', () => {
    const desc = describeSubtype('ductal_carcinoma');
    expect(desc).toMatch(/most common breast cancer/i);
  });

  it('accepts uppercase values', () => {
    expect(describeSubtype('ADENOSIS')).toMatch(/benign/i);
  });

  it('returns empty string for unknown types', () => {
    expect(describeSubtype('not_a_real_subtype')).toBe('');
  });

  it('returns empty string for null/undefined', () => {
    expect(describeSubtype(null)).toBe('');
    expect(describeSubtype(undefined)).toBe('');
    expect(describeSubtype('')).toBe('');
  });

  it('has a description for every defined subtype', () => {
    for (const code of SUBTYPES) {
      expect(describeSubtype(code)).not.toBe('');
    }
  });

  it('covers all 8 subtypes from the BreakHis model', () => {
    expect(SUBTYPES.length).toBe(8);
  });
});
