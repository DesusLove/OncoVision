package com.albert.patientsystem.dto;

/**
 * Slim view of a Patient for dropdowns.
 * Avoids sending the full {@code records} collection (and lazy-load surprises)
 * down to the diagnose form, which only needs id + display name.
 */
public class PatientOption {
    private final Long id;
    private final String patientId;
    private final String fullName;

    public PatientOption(Long id, String patientId, String fullName) {
        this.id = id;
        this.patientId = patientId;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public String getPatientId() { return patientId; }
    public String getFullName() { return fullName; }
}
