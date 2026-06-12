package com.albert.patientsystem.controller;

import com.albert.patientsystem.dto.DiagnosticRecordView;
import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.repository.DiagnosticRecordRepository;
import com.albert.patientsystem.service.DiagnosisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RecordController {

    private final DiagnosticRecordRepository recordRepo;
    private final DiagnosisService diagnosisService;

    public RecordController(DiagnosticRecordRepository recordRepo, DiagnosisService diagnosisService) {
        this.recordRepo = recordRepo;
        this.diagnosisService = diagnosisService;
    }

    // Doctor validation: mark as verified, optionally with a corrected label.
    @PutMapping("/records/{id}/verify")
    public DiagnosticRecordView verifyRecord(@PathVariable Long id,
                                             @RequestBody(required = false) Map<String, String> payload) {
        DiagnosticRecord record = recordRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found"));
        record.setVerified(true);
        if (payload != null && payload.containsKey("correctedLabel")) {
            String corr = payload.get("correctedLabel");
            // normalise to lowercase wire form so the column matches the binaryLabel convention
            record.setCorrectedLabel(corr != null ? corr.toLowerCase() : null);
        }
        return new DiagnosticRecordView(recordRepo.save(record));
    }

    @GetMapping("/records/{id}")
    public DiagnosticRecordView getRecord(@PathVariable Long id) {
        return new DiagnosticRecordView(recordRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found")));
    }

    @DeleteMapping("/records/{id}")
    public ResponseEntity<Void> deleteRecord(@PathVariable Long id) {
        if (!recordRepo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
        recordRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // List all diagnostic records for a patient.
    @GetMapping("/patients/{patientId}/records")
    public List<DiagnosticRecordView> records(@PathVariable Long patientId) {
        return recordRepo.findByPatient_Id(patientId).stream()
                .map(DiagnosticRecordView::new)
                .collect(Collectors.toList());
    }

    // Paginated, filterable record listing.
    @GetMapping("/records")
    public Page<DiagnosticRecordView> listRecords(
            @RequestParam(required = false) String label,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("testDate").descending());
        // accept both lowercase wire form and legacy uppercase
        BinaryLabel normalised = label == null ? null
                : BinaryLabel.fromWire(label.toLowerCase(Locale.ROOT));
        Page<DiagnosticRecord> entities = diagnosisService.searchRecords(
                normalised == null ? null : normalised.wire(), from, to, pageable);
        return entities.map(DiagnosticRecordView::new);
    }
}
