package com.albert.patientsystem.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "diagnostic_records")
public class DiagnosticRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")       // foreign key -> patients.id
    private Patient patient;

    private LocalDate testDate;            // day the test was taken
    private String imageFilename;

    @Enumerated(EnumType.STRING)
    private BinaryLabel binaryLabel;       // BENIGN / MALIGNANT  (binary model)

    private Double binaryProbability;      // probability_malignant

    @Enumerated(EnumType.STRING)
    private SubtypeLabel subtypeLabel;     // 8-class subtype model

    private Double subtypeConfidence;

    private LocalDateTime createdAt;

    // Verification (doctor reviewed / corrected the AI result).
    private Boolean verified = false;
    private String correctedLabel;          // raw wire value: "benign" | "malignant" | null
    private String verifiedBy;              // username of the verifier
    private LocalDateTime verifiedAt;

    public DiagnosticRecord() {}

    @PrePersist
    public void onCreate() {               // auto-set timestamp on insert
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
    public LocalDate getTestDate() { return testDate; }
    public void setTestDate(LocalDate testDate) { this.testDate = testDate; }
    public String getImageFilename() { return imageFilename; }
    public void setImageFilename(String imageFilename) { this.imageFilename = imageFilename; }
    public BinaryLabel getBinaryLabel() { return binaryLabel; }
    public void setBinaryLabel(BinaryLabel binaryLabel) { this.binaryLabel = binaryLabel; }
    public Double getBinaryProbability() { return binaryProbability; }
    public void setBinaryProbability(Double binaryProbability) { this.binaryProbability = binaryProbability; }
    public SubtypeLabel getSubtypeLabel() { return subtypeLabel; }
    public void setSubtypeLabel(SubtypeLabel subtypeLabel) { this.subtypeLabel = subtypeLabel; }
    public Double getSubtypeConfidence() { return subtypeConfidence; }
    public void setSubtypeConfidence(Double subtypeConfidence) { this.subtypeConfidence = subtypeConfidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public String getCorrectedLabel() { return correctedLabel; }
    public void setCorrectedLabel(String correctedLabel) { this.correctedLabel = correctedLabel; }
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
}
