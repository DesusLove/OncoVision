package com.albert.patientsystem;

import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.entity.SubtypeLabel;
import com.albert.patientsystem.repository.DiagnosticRecordRepository;
import com.albert.patientsystem.service.DiagnosisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for the diagnosis filter — the same flow that broke
 * with "Argument [malignant] of type [String] did not match parameter
 * type BinaryLabel" in production. Boots a real Spring context against
 * the file-backed H2 DB (./data/test-patientdb.mv.db) so the JPQL
 * parameter binding is actually exercised.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
})
class RecordFilterTest {

    @Autowired DiagnosticRecordRepository repo;
    @Autowired DiagnosisService diagnosisService;

    @BeforeEach
    void seed() {
        repo.deleteAll();
        repo.save(record("malignant", "2026-01-15"));
        repo.save(record("benign",    "2026-02-15"));
        repo.save(record("benign",    "2026-03-15"));
        repo.save(record("malignant", "2026-04-15"));
    }

    private DiagnosticRecord record(String label, String date) {
        DiagnosticRecord r = new DiagnosticRecord();
        r.setBinaryLabel(BinaryLabel.fromWire(label));
        r.setSubtypeLabel(SubtypeLabel.DUCTAL_CARCINOMA);
        r.setBinaryProbability(0.5);
        r.setTestDate(LocalDate.parse(date));
        return r;
    }

    @Test
    void filterByMalignantLabelReturnsOnlyMalignant() {
        Page<DiagnosticRecord> result = diagnosisService.searchRecords(
            "malignant", null, null,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
        result.forEach(r -> assertEquals(BinaryLabel.MALIGNANT, r.getBinaryLabel()));
    }

    @Test
    void filterByBenignLabelReturnsOnlyBenign() {
        Page<DiagnosticRecord> result = diagnosisService.searchRecords(
            "benign", null, null,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
        result.forEach(r -> assertEquals(BinaryLabel.BENIGN, r.getBinaryLabel()));
    }

    @Test
    void filterByUnknownLabelReturnsEverything() {
        Page<DiagnosticRecord> result = diagnosisService.searchRecords(
            "garbage", null, null,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(4, result.getTotalElements());
    }

    @Test
    void filterByDateRangeWorks() {
        Page<DiagnosticRecord> result = diagnosisService.searchRecords(
            null, LocalDate.parse("2026-02-01"), LocalDate.parse("2026-03-31"),
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void emptyFilterReturnsEverything() {
        Page<DiagnosticRecord> result = diagnosisService.searchRecords(
            null, null, null,
            org.springframework.data.domain.PageRequest.of(0, 10));
        assertEquals(4, result.getTotalElements());
    }
}
