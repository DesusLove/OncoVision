package com.albert.patientsystem.service;

import com.albert.patientsystem.dto.PatientRequest;
import com.albert.patientsystem.entity.Patient;
import com.albert.patientsystem.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
public class PatientService {

    private final PatientRepository repo;
    public PatientService(PatientRepository repo) { this.repo = repo; }  // constructor injection

    public Patient create(PatientRequest req) {
        if (repo.existsByPatientId(req.getPatientId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "patientId already exists");
        if (repo.existsByPassportNumber(req.getPassportNumber()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "passportNumber already exists");
        Patient p = new Patient();
        apply(req, p);
        return repo.save(p);
    }

    public List<Patient> getAll() { return repo.findAll(); }

    public Patient getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
    }

    public Patient update(Long id, PatientRequest req) {
        Patient p = getById(id);
        if (repo.existsByPatientIdAndIdNot(req.getPatientId(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "patientId already exists");
        if (repo.existsByPassportNumberAndIdNot(req.getPassportNumber(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "passportNumber already exists");
        apply(req, p);
        return repo.save(p);
    }

    public void delete(Long id) {
        if (!repo.existsById(id))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found");
        repo.deleteById(id);
    }
    public Page<Patient> search(String q, Pageable pageable) {
        return repo.search(q, pageable);
    }

    /**
     * Returns up to {@code limit} patients ordered by name, for the
     * diagnose-form dropdown. Callers should over-fetch by one (e.g. ask
     * for cap+1) so they can detect truncation.
     */
    public List<Patient> findOptions(int limit) {
        return repo.findAllOrderedByName(PageRequest.of(0, limit));
    }
    private void apply(PatientRequest req, Patient p) {
        p.setPatientId(req.getPatientId());
        p.setFullName(req.getFullName());
        p.setGender(req.getGender());
        p.setPassportNumber(req.getPassportNumber());
    }
}