package com.albert.patientsystem.service;

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
        stats.put("malignant", recordRepo.countByBinaryLabel("malignant"));
        stats.put("benign", recordRepo.countByBinaryLabel("benign"));

        Map<String, Long> subtypes = new LinkedHashMap<>();
        for (Object[] row : recordRepo.countBySubtype()) {
            String raw = (String) row[0];
            // Stored as enum name (DUCTAL_CARCINOMA); expose as wire form (ductal_carcinoma)
            // so the Vue donut's pretty() helper does the same title-casing as for the ML result.
            String label = raw == null
                    ? "unknown"
                    : (isSubtypeName(raw) ? raw.toLowerCase(Locale.ROOT) : raw);
            subtypes.put(label, (Long) row[1]);
        }
        stats.put("subtypeDistribution", subtypes);
        return stats;
    }

    private static boolean isSubtypeName(String raw) {
        for (SubtypeLabel s : SubtypeLabel.values()) {
            if (s.name().equals(raw)) return true;
        }
        return false;
    }
}