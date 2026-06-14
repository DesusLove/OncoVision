package com.albert.patientsystem.controller;

import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.repository.DiagnosticRecordRepository;
import com.albert.patientsystem.security.CurrentUser;
import com.albert.patientsystem.service.AuditService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

/**
 * Cross-cutting record actions: detail fetch, bulk operations, CSV
 * export. Kept separate from the records controller so the per-tab
 * list/pagination endpoints stay small.
 */
@RestController
@RequestMapping("/api/records")
public class RecordActionsController {

    private final DiagnosticRecordRepository repo;
    private final AuditService auditService;

    public RecordActionsController(DiagnosticRecordRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    /** GET /api/records/{id} — full record detail for the detail view. */
    @GetMapping("/{id}")
    public DiagnosticRecord detail(@PathVariable Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Record not found"));
    }

    /** POST /api/records/bulk-delete { ids: [...] } */
    @PostMapping("/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestBody Map<String, List<Long>> body) {
        String username = CurrentUser.username();
        List<Long> ids = body.getOrDefault("ids", List.of());
        if (ids.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "ids required"));
        // Permission check: only admins can bulk-delete records.
        // (No role check yet — would integrate with @PreAuthorize once
        // UserDetailsService exposes the role. Left as a TODO for the
        // production hardening pass.)
        List<DiagnosticRecord> found = repo.findAllById(ids);
        repo.deleteAll(found);
        auditService.log(username, "BULK_DELETE_RECORDS", "RECORD", null,
            Map.of("count", found.size(), "ids", ids.toString()));
        return ResponseEntity.ok(Map.of("deleted", found.size()));
    }

    /** POST /api/records/bulk-verify { ids: [...], correctedLabel?: "benign"|"malignant" } */
    @PostMapping("/bulk-verify")
    public ResponseEntity<?> bulkVerify(@RequestBody Map<String, Object> body) {
        String username = CurrentUser.username();
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) body.getOrDefault("ids", List.of());
        String correctedLabel = (String) body.get("correctedLabel");
        if (ids.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "ids required"));
        List<DiagnosticRecord> records = repo.findAllById(ids);
        for (DiagnosticRecord r : records) {
            r.setVerified(true);
            r.setVerifiedBy(username);
            r.setVerifiedAt(java.time.LocalDateTime.now());
            if (correctedLabel != null && !correctedLabel.isBlank()) {
                try {
                    // Store the raw wire string ("benign" | "malignant") so the
                    // column stays portable across DB engines.
                    BinaryLabel parsed = BinaryLabel.fromWire(correctedLabel);
                    r.setCorrectedLabel(parsed.wire());
                } catch (IllegalArgumentException ignored) {}
            }
        }
        repo.saveAll(records);
        auditService.log(username, "BULK_VERIFY_RECORDS", "RECORD", null,
            Map.of("count", records.size(), "correctedLabel", correctedLabel == null ? "" : correctedLabel));
        return ResponseEntity.ok(Map.of("verified", records.size()));
    }

    /**
     * GET /api/records/export.csv?label=&from=&to=
     * Streams a CSV of the current filtered record set. The browser
     * triggers a download via the Content-Disposition header.
     */
    @GetMapping(value = "/export.csv", produces = "text/csv")
    public void exportCsv(
        @RequestParam(required = false) String label,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        HttpServletResponse response
    ) throws IOException {
        String username = CurrentUser.username();
        // Build the same query the list endpoint uses.
        com.albert.patientsystem.entity.BinaryLabel labelEnum = null;
        if (label != null && !label.isBlank()) {
            try { labelEnum = com.albert.patientsystem.entity.BinaryLabel.fromWire(label); }
            catch (IllegalArgumentException ignored) {}
        }
        LocalDate fromDate = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
        LocalDate toDate = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
        List<DiagnosticRecord> all = repo.filter(labelEnum, fromDate, toDate,
            org.springframework.data.domain.PageRequest.of(0, 10_000,
                org.springframework.data.domain.Sort.by(
                    org.springframework.data.domain.Sort.Direction.DESC, "testDate")))
            .getContent();

        String filename = "oncovision-records-" + LocalDate.now() + ".csv";
        response.setContentType(MediaType.parseMediaType("text/csv").toString());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setHeader("Cache-Control", "no-store");
        try (PrintWriter w = response.getWriter()) {
            // Header row
            w.println("id,test_date,patient_id,patient_name,binary_label,binary_probability,subtype_label,verified,verified_by,verified_at,corrected_label,image_filename");
            DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE;
            for (DiagnosticRecord r : all) {
                String pid = r.getPatient() != null ? String.valueOf(r.getPatient().getId()) : "";
                String pname = r.getPatient() != null ? csv(r.getPatient().getFullName()) : "";
                String verBy = r.getVerifiedBy() != null ? csv(r.getVerifiedBy()) : "";
                String verAt = r.getVerifiedAt() != null ? r.getVerifiedAt().toString() : "";
                String corr = r.getCorrectedLabel() != null ? r.getCorrectedLabel() : "";
                w.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    r.getId(),
                    r.getTestDate() != null ? r.getTestDate().format(iso) : "",
                    csv(r.getPatient() != null ? r.getPatient().getPatientId() : ""),
                    pname,
                    r.getBinaryLabel() != null ? r.getBinaryLabel().wire() : "",
                    r.getBinaryProbability() != null ? String.valueOf(r.getBinaryProbability()) : "",
                    r.getSubtypeLabel() != null ? r.getSubtypeLabel().wire() : "",
                    Boolean.TRUE.equals(r.getVerified()) ? "true" : "false",
                    verBy, verAt, corr,
                    csv(r.getImageFilename()));
            }
        }
        auditService.log(username, "EXPORT_RECORDS_CSV", "RECORD", null,
            Map.of("count", all.size(), "label", label == null ? "" : label));
    }

    /** Minimal CSV-escape: wrap in quotes and double internal quotes. */
    private static String csv(String s) {
        if (s == null) return "";
        if (s.indexOf(',') < 0 && s.indexOf('"') < 0 && s.indexOf('\n') < 0) return s;
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
