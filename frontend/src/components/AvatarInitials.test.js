import { mount } from '@vue/test-utils';
import { describe, it, expect } from 'vitest';
import AvatarInitials from './AvatarInitials.vue';

describe('AvatarInitials', () => {
  it('renders the first two initials for a single name', () => {
    const wrapper = mount(AvatarInitials, { props: { name: 'Ada Lovelace' } });
    expect(wrapper.text()).toBe('AL');
  });

  it('renders first letter twice for a single-word name', () => {
    const wrapper = mount(AvatarInitials, { props: { name: 'Plato' } });
    expect(wrapper.text()).toBe('PL');
  });

  it('falls back to "?" when name is empty', () => {
    const wrapper = mount(AvatarInitials, { props: { name: '' } });
    expect(wrapper.text()).toBe('?');
  });

  it('is deterministic — same name produces the same background', () => {
    const a = mount(AvatarInitials, { props: { name: 'Grace Hopper' } });
    const b = mount(AvatarInitials, { props: { name: 'Grace Hopper' } });
    expect(a.attributes('style')).toContain(b.attributes('style').split('background:')[1].split(';')[0]);
  });

  it('respects the size prop for circle dimensions', () => {
    const wrapper = mount(AvatarInitials, { props: { name: 'X', size: 48 } });
    expect(wrapper.attributes('style')).toContain('width: 48px');
    expect(wrapper.attributes('style')).toContain('height: 48px');
  });
});
