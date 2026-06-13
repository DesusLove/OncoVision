/**
 * Plain-language descriptions for the 8 histological subtypes the model
 * can return. Wire format is lowercase (e.g. "ductal_carcinoma"); we
 * normalize to uppercase here so the lookup is consistent regardless
 * of how the ML service formats its output.
 *
 * The descriptions are intentionally short (1 sentence) and clinically
 * neutral — they appear next to an AI result and must not be read as
 * a diagnosis.
 */
const DESCRIPTIONS = {
  ADENOSIS:            'Benign overgrowth of glandular tissue, generally harmless and not associated with cancer risk.',
  FIBROADENOMA:        'A common benign breast lump composed of glandular and connective tissue, most often in young women.',
  PHYLLODES_TUMOR:     'A rare fibroepithelial tumor that can range from benign to malignant and may grow rapidly.',
  TUBULAR_ADENOMA:     'A benign tumor made up of well-organized tubular structures with low risk of progression.',
  DUCTAL_CARCINOMA:    'The most common breast cancer type, originating in the milk ducts and capable of spreading.',
  LOBULAR_CARCINOMA:   'Cancer arising in the milk-producing lobules, often multifocal and bilateral.',
  MUCINOUS_CARCINOMA:  'A rare, slower-growing cancer surrounded by mucin, generally carrying a better prognosis.',
  PAPILLARY_CARCINOMA: 'An uncommon cancer with finger-like projections, often low-grade with favorable outcomes.',
};

/**
 * @param {string} wire  e.g. "ductal_carcinoma" or "DUCTAL_CARCINOMA"
 * @returns {string} plain-language description, or empty string if unknown
 */
export const describeSubtype = (wire) => {
  if (!wire) return '';
  return DESCRIPTIONS[wire.toUpperCase()] || '';
};

export const SUBTYPES = Object.keys(DESCRIPTIONS);
