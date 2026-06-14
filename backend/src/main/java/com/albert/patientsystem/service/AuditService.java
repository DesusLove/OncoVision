package com.albert.patientsystem.service;

import com.albert.patientsystem.entity.AuditLog;
import com.albert.patientsystem.repository.AuditLogRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Writes audit log entries. Marked @Async so the calling request
 * isn't blocked on the DB write — audit logging must never be on the
 * critical path of a mutating request.
 */
@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Async
    public void log(String username, String action, String resourceType, Long resourceId, Map<String, ?> details) {
        AuditLog e = new AuditLog();
        e.setUsername(username == null ? "anonymous" : username);
        e.setAction(action);
        e.setResourceType(resourceType);
        e.setResourceId(resourceId);
        if (details != null && !details.isEmpty()) {
            // Naive JSON serialisation — fine for flat key/value maps.
            StringBuilder sb = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, ?> entry : details.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(entry.getKey().replace("\"", "\\\"")).append("\":");
                Object v = entry.getValue();
                if (v == null) sb.append("null");
                else if (v instanceof Number || v instanceof Boolean) sb.append(v);
                else sb.append('"').append(v.toString().replace("\"", "\\\"")).append('"');
            }
            sb.append('}');
            e.setDetails(sb.toString());
        }
        repo.save(e);
    }
}
