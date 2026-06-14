package com.albert.patientsystem.controller;

import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.repository.DiagnosticRecordRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Monthly trend data for the Dashboard chart. Returns counts grouped by
 * year-month so the frontend can plot a 12-month rolling line chart.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsTrendsController {

    private final DiagnosticRecordRepository repo;

    public StatsTrendsController(DiagnosticRecordRepository repo) {
        this.repo = repo;
    }

    /**
     * GET /api/stats/trends?months=12
     * Returns: [{ month: "2025-07", total: 12, malignant: 3, benign: 9 }, ...]
     */
    @GetMapping("/trends")
    public List<Map<String, Object>> trends(@RequestParam(defaultValue = "12") int months) {
        // Pull a large page — for a small dataset this is fine. Production
        // would switch to a native aggregate query.
        List<DiagnosticRecord> all = repo.findAll();
        Map<String, int[]> buckets = new TreeMap<>(); // ym -> [total, malignant, benign]
        for (DiagnosticRecord r : all) {
            if (r.getTestDate() == null) continue;
            String ym = r.getTestDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            int[] b = buckets.computeIfAbsent(ym, k -> new int[3]);
            b[0]++;
            if (r.getBinaryLabel() != null) {
                if (r.getBinaryLabel().wire().equals("malignant")) b[1]++;
                else if (r.getBinaryLabel().wire().equals("benign")) b[2]++;
            }
        }
        // Trim to the last N months (keep chronological order).
        List<String> keys = new ArrayList<>(buckets.keySet());
        int start = Math.max(0, keys.size() - months);
        List<Map<String, Object>> out = new ArrayList<>();
        for (int i = start; i < keys.size(); i++) {
            String ym = keys.get(i);
            int[] b = buckets.get(ym);
            out.add(Map.of(
                "month", ym,
                "total", b[0],
                "malignant", b[1],
                "benign", b[2]
            ));
        }
        return out;
    }
}
