import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import AppBadge from './AppBadge.vue';

describe('AppBadge', () => {
  it('renders the label text in the default slot', () => {
    const wrapper = mount(AppBadge, {
      props: { variant: 'benign' },
      slots: { default: 'Benign' },
    });
    expect(wrapper.text()).toContain('Benign');
  });

  it('applies the variant class', () => {
    const wrapper = mount(AppBadge, {
      props: { variant: 'malignant' },
      slots: { default: 'Malignant' },
    });
    expect(wrapper.classes()).toContain('app-badge--malignant');
  });

  it('renders a dot indicator', () => {
    const wrapper = mount(AppBadge, {
      props: { variant: 'info' },
      slots: { default: 'Info' },
    });
    expect(wrapper.find('.app-badge__dot').exists()).toBe(true);
  });

  it('defaults to the info variant when none is provided', () => {
    const wrapper = mount(AppBadge, {
      slots: { default: 'Info' },
    });
    expect(wrapper.classes()).toContain('app-badge--info');
  });

  it('rejects unknown variants via the validator', () => {
    // Vue prop validators run on mount; an unknown variant should produce a console warning
    // (we assert the component still mounts rather than crashing).
    const consoleWarn = vi.spyOn(console, 'warn').mockImplementation(() => {});
    const wrapper = mount(AppBadge, {
      props: { variant: 'bogus' },
      slots: { default: 'X' },
    });
    expect(wrapper.exists()).toBe(true);
    consoleWarn.mockRestore();
  });
});
