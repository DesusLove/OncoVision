package com.albert.patientsystem.controller;

import com.albert.patientsystem.entity.AuditLog;
import com.albert.patientsystem.repository.AuditLogRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-only audit log view. Returns the most recent entries first.
 * No role check yet — needs the admin guard added in the production
 * hardening pass (Spring Security @PreAuthorize).
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogRepository repo;

    public AuditController(AuditLogRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public Object list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "50") int size) {
        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, Math.min(size, 200)));
    }
}
