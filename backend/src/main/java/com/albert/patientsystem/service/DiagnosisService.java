package com.albert.patientsystem.service;

import com.albert.patientsystem.dto.DiagnosisResult;
import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.entity.Patient;
import com.albert.patientsystem.entity.SubtypeLabel;
import com.albert.patientsystem.repository.DiagnosticRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class DiagnosisService {

    private final PatientService patientService;          // reuse existing lookup
    private final DiagnosticRecordRepository recordRepo;
    private final RestClient rest;

    public DiagnosisService(PatientService patientService,
                            DiagnosticRecordRepository recordRepo,
                            @Value("${ml.service.url}") String mlUrl) {
        this.patientService = patientService;
        this.recordRepo = recordRepo;
        // FastAPI inference is light, but a hung / black-holed port should not block
        // the diagnose endpoint indefinitely. 5s connect, 60s read is generous.
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofSeconds(5));
        rf.setReadTimeout(Duration.ofSeconds(60));
        this.rest = RestClient.builder()
                .baseUrl(mlUrl)
                .requestFactory(rf)  // HTTP/1.1 only — no upgrade attempt
                .build();
    }

    public DiagnosticRecord diagnose(Long patientId, MultipartFile image, LocalDate testDate) {
        Patient patient = patientService.getById(patientId);   // 404 if missing

        // forward the image to FastAPI as multipart, field name "file"
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", toResource(image));

        DiagnosisResult result;
        try {
            result = rest.post()
                    .uri("/predict")
                    .body(body)
                    .retrieve()
                    .body(DiagnosisResult.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "ML service call failed: " + e.getMessage());
        }
        if (result == null || result.binary == null)
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "ML service returned no result");

        // persist as a DiagnosticRecord linked to the patient
        DiagnosticRecord rec = new DiagnosticRecord();
        rec.setPatient(patient);
        rec.setTestDate(testDate != null ? testDate : LocalDate.now());
        rec.setImageFilename(image.getOriginalFilename());
        rec.setBinaryLabel(BinaryLabel.fromWire(result.binary.label));
        rec.setBinaryProbability(result.binary.probabilityMalignant);
        if (result.subtype != null && result.subtype.label != null) {
            rec.setSubtypeLabel(SubtypeLabel.fromWire(result.subtype.label));
            rec.setSubtypeConfidence(result.subtype.confidence);
        }
        return recordRepo.save(rec);
    }

    private ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override public String getFilename() { return file.getOriginalFilename(); }
            };
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read uploaded image");
        }
    }
    public Page<DiagnosticRecord> searchRecords(String label, LocalDate from, LocalDate to, Pageable pageable) {
        return recordRepo.filter(label, from, to, pageable);
    }


}
