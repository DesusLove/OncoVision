package com.albert.patientsystem.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Append-only audit trail. Every mutating API call writes a row here
 * with the acting user, the resource they touched, and a JSON details
 * blob. Surfaced via {@code GET /api/audit} for the admin view.
 */
@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String username;

    @Column(nullable = false, length = 32)
    private String action; // CREATE_PATIENT | DELETE_PATIENT | VERIFY_RECORD | LOGIN | ...

    @Column(name = "resource_type", length = 32)
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    /** Free-form JSON. Kept as TEXT so we don't need a JSON column type. */
    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
