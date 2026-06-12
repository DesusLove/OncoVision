package com.albert.patientsystem.controller;

import com.albert.patientsystem.dto.PatientOption;
import com.albert.patientsystem.dto.PatientRequest;
import com.albert.patientsystem.entity.Patient;
import com.albert.patientsystem.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/patients")
public class PatientController {

    /** Hard cap on the /options endpoint so a 50k-patient DB doesn't ship its
     *  whole table down to the diagnose form. The frontend shows a warning if
     *  the result is truncated. */
    static final int OPTIONS_MAX = 10_000;

    private final PatientService service;
    public PatientController(PatientService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<Patient> create(@Valid @RequestBody PatientRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }
    @GetMapping
    public Page<Patient> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return service.search(q, pageable);
    }

    /**
     * Slim, unpaginated listing for the diagnose dropdown. Capped at
     * {@link #OPTIONS_MAX}; the response body indicates whether truncation
     * occurred so the UI can surface a warning.
     */
    @GetMapping("/options")
    public Map<String, Object> options() {
        List<Patient> page = service.findOptions(OPTIONS_MAX + 1);
        boolean truncated = page.size() > OPTIONS_MAX;
        List<Patient> trimmed = truncated ? page.subList(0, OPTIONS_MAX) : page;
        List<PatientOption> options = trimmed.stream()
                .map(p -> new PatientOption(p.getId(), p.getPatientId(), p.getFullName()))
                .collect(Collectors.toList());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("options", options);
        body.put("truncated", truncated);
        body.put("cap", OPTIONS_MAX);
        return body;
    }


    @GetMapping("/{id}")
    public Patient getById(@PathVariable Long id) { return service.getById(id); }

    @PutMapping("/{id}")
    public Patient update(@PathVariable Long id, @Valid @RequestBody PatientRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}