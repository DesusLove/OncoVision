package com.albert.patientsystem.dto;

import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.entity.SubtypeLabel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Wire-format view of a {@link DiagnosticRecord}.
 *
 * <p>The entity stores enums (uppercase); the API exposes lowercase strings
 * to match the ML service output and the existing Vue comparisons
 * ({@code r.binaryLabel === 'malignant'}). The {@code correctedLabel} is
 * also lowercased on the way out for the same reason.
 */
public class DiagnosticRecordView {

    private final Long id;
    private final Long patientId;
    private final String patientPatientId;
    private final LocalDate testDate;
    private final String imageFilename;
    private final String binaryLabel;
    private final Double binaryProbability;
    private final String subtypeLabel;
    private final Double subtypeConfidence;
    private final Boolean verified;
    private final String correctedLabel;
    private final LocalDateTime createdAt;

    public DiagnosticRecordView(DiagnosticRecord r) {
        this.id = r.getId();
        this.patientId = r.getPatient() != null ? r.getPatient().getId() : null;
        this.patientPatientId = r.getPatient() != null ? r.getPatient().getPatientId() : null;
        this.testDate = r.getTestDate();
        this.imageFilename = r.getImageFilename();
        this.binaryLabel = r.getBinaryLabel() != null ? r.getBinaryLabel().wire() : null;
        this.binaryProbability = r.getBinaryProbability();
        this.subtypeLabel = r.getSubtypeLabel() != null ? r.getSubtypeLabel().wire() : null;
        this.subtypeConfidence = r.getSubtypeConfidence();
        this.verified = r.getVerified();
        this.correctedLabel = r.getCorrectedLabel() != null
                ? r.getCorrectedLabel().toLowerCase() : null;
        this.createdAt = r.getCreatedAt();
    }

    public Long getId() { return id; }
    public Long getPatientId() { return patientId; }
    @JsonProperty("patientPatientId")
    public String getPatientPatientId() { return patientPatientId; }
    public LocalDate getTestDate() { return testDate; }
    public String getImageFilename() { return imageFilename; }
    public String getBinaryLabel() { return binaryLabel; }
    public Double getBinaryProbability() { return binaryProbability; }
    public String getSubtypeLabel() { return subtypeLabel; }
    public Double getSubtypeConfidence() { return subtypeConfidence; }
    public Boolean getVerified() { return verified; }
    public String getCorrectedLabel() { return correctedLabel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
