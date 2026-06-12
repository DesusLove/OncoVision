package com.albert.patientsystem.entity;

/**
 * Result of the binary detector (benign vs malignant).
 * Persisted as the enum NAME (e.g. "MALIGNANT") in the column, but the
 * wire format exposed by {@code DiagnosticRecordView} is the lowercase
 * string returned by the ML service so the existing Vue comparisons
 * ({@code r.binaryLabel === 'malignant'}) keep working unchanged.
 */
public enum BinaryLabel {
    BENIGN,
    MALIGNANT;

    public String wire() { return name().toLowerCase(); }

    public static BinaryLabel fromWire(String wire) {
        if (wire == null) return null;
        return BinaryLabel.valueOf(wire.toUpperCase());
    }
}
