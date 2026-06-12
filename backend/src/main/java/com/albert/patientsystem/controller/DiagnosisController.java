package com.albert.patientsystem.controller;

import com.albert.patientsystem.dto.DiagnosticRecordView;
import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.SubtypeLabel;
import com.albert.patientsystem.service.DiagnosisService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    // Upload an image for a patient -> predict -> save.
    @PostMapping(value = "/patients/{patientId}/diagnose", consumes = "multipart/form-data")
    public DiagnosticRecordView diagnose(
            @PathVariable Long patientId,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "testDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate testDate) {
        var rec = diagnosisService.diagnose(patientId, image, testDate);
        return new DiagnosticRecordView(rec);
    }
}
