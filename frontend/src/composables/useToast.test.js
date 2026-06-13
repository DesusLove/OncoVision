import { describe, it, expect, beforeEach } from 'vitest';
import { useToast, toasts, dismiss, clearAll } from './useToast.js';

describe('useToast', () => {
  beforeEach(() => clearAll());

  it('pushes a success toast with a generated id', () => {
    const id = useToast().success('Saved');
    const t = toasts.value[0];
    expect(t.id).toBe(id);
    expect(t.variant).toBe('success');
    expect(t.text).toBe('Saved');
  });

  it('pushes error and info toasts with the right variant', () => {
    useToast().error('Boom');
    useToast().info('FYI');
    expect(toasts.value.map((t) => t.variant)).toEqual(['error', 'info']);
  });

  it('dismiss removes the toast by id', () => {
    const id = useToast().success('X');
    expect(toasts.value.length).toBe(1);
    dismiss(id);
    expect(toasts.value.length).toBe(0);
  });

  it('clearAll empties the queue', () => {
    useToast().success('A');
    useToast().error('B');
    clearAll();
    expect(toasts.value.length).toBe(0);
  });
});
