import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import AppSearchSelect from './AppSearchSelect.vue';

describe('AppSearchSelect', () => {
    beforeEach(() => {
        // Click-outside handler reads document, so jsdom needs a body.
        document.body.innerHTML = '';
    });

    const options = [
        { id: 1, label: 'Alice Anderson' },
        { id: 2, label: 'Bob Brown' },
        { id: 3, label: 'Cathy Chen' },
    ];

    it('renders the placeholder when no selection', () => {
        const w = mount(AppSearchSelect, {
            props: { options, placeholder: 'Pick one…' },
        });
        expect(w.text()).toContain('Pick one…');
    });

    it('shows the selected option label', () => {
        const w = mount(AppSearchSelect, {
            props: { modelValue: 2, options, valueKey: 'id', labelKey: 'label' },
        });
        expect(w.text()).toContain('Bob Brown');
    });

    it('opens the dropdown on click and filters options by query', async () => {
        const w = mount(AppSearchSelect, {
            props: { options, valueKey: 'id', labelKey: 'label' },
        });
        await w.find('button.app-search-select__trigger').trigger('click');
        expect(w.find('.app-search-select__panel').exists()).toBe(true);
        expect(w.findAll('.app-search-select__option')).toHaveLength(3);
        await w.find('input').setValue('bob');
        expect(w.findAll('.app-search-select__option')).toHaveLength(1);
        expect(w.text()).toContain('Bob Brown');
    });

    it('emits update:modelValue with the option value on select', async () => {
        const w = mount(AppSearchSelect, {
            props: { options, valueKey: 'id', labelKey: 'label' },
        });
        await w.find('button.app-search-select__trigger').trigger('click');
        await w.findAll('.app-search-select__option')[1].trigger('mousedown');
        expect(w.emitted('update:modelValue')[0]).toEqual([2]);
        expect(w.emitted('change')[0][0]).toEqual(options[1]);
    });
});
