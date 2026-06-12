package com.albert.patientsystem.service;

import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.SubtypeLabel;
import com.albert.patientsystem.repository.DiagnosticRecordRepository;
import com.albert.patientsystem.repository.PatientRepository;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class StatsService {

    private final PatientRepository patientRepo;
    private final DiagnosticRecordRepository recordRepo;

    public StatsService(PatientRepository patientRepo, DiagnosticRecordRepository recordRepo) {
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPatients", patientRepo.count());
        stats.put("totalDiagnoses", recordRepo.count());
        // countByBinaryLabel now takes the BinaryLabel enum, not a String
        stats.put("malignant", recordRepo.countByBinaryLabel(BinaryLabel.MALIGNANT));
        stats.put("benign", recordRepo.countByBinaryLabel(BinaryLabel.BENIGN));

        Map<String, Long> subtypes = new LinkedHashMap<>();
        for (Object[] row : recordRepo.countBySubtype()) {
            Object raw = row[0];
            // Stored as enum (DUCTAL_CARCINOMA); expose as wire form (ductal_carcinoma)
            // so the Vue donut's pretty() helper does the same title-casing as for the ML result.
            String label;
            if (raw == null) {
                label = "unknown";
            } else if (raw instanceof SubtypeLabel) {
                label = ((SubtypeLabel) raw).wire();
            } else {
                // legacy String data — already lowercase, pass through
                label = raw.toString().toLowerCase(Locale.ROOT);
            }
            subtypes.put(label, (Long) row[1]);
        }
        stats.put("subtypeDistribution", subtypes);
        return stats;
    }
}