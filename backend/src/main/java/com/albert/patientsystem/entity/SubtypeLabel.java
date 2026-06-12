package com.albert.patientsystem.entity;

/**
 * The 8 histological subtypes the multiclass model can return.
 * Order matches {@code ml/model/metadata_multiclass_200x.json}.
 * Persisted as enum NAME; wire format is lowercase to match ML output.
 */
public enum SubtypeLabel {
    ADENOSIS,
    DUCTAL_CARCINOMA,
    FIBROADENOMA,
    LOBULAR_CARCINOMA,
    MUCINOUS_CARCINOMA,
    PAPILLARY_CARCINOMA,
    PHYLLODES_TUMOR,
    TUBULAR_ADENOMA;

    public String wire() { return name().toLowerCase(); }

    public static SubtypeLabel fromWire(String wire) {
        if (wire == null) return null;
        return SubtypeLabel.valueOf(wire.toUpperCase());
    }
}
