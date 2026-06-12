package com.albert.patientsystem.entity;

/**
 * Patient sex. Persisted as the enum NAME. The Vue form already sends
 * "MALE" / "FEMALE" / "OTHER" (uppercase), so no wire-format conversion needed.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER
}
