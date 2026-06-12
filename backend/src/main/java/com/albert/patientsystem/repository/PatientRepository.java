package com.albert.patientsystem.repository;

import com.albert.patientsystem.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // "derived queries" — Spring builds the SQL from the method name
    Optional<Patient> findByPatientId(String patientId);
    boolean existsByPatientId(String patientId);
    boolean existsByPassportNumber(String passportNumber);
    boolean existsByPatientIdAndIdNot(String patientId, Long id);
    boolean existsByPassportNumberAndIdNot(String passportNumber, Long id);
    @Query("""
    SELECT p FROM Patient p
    WHERE :q IS NULL OR :q = ''
       OR LOWER(p.fullName)       LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(p.patientId)      LIKE LOWER(CONCAT('%', :q, '%'))
       OR LOWER(p.passportNumber) LIKE LOWER(CONCAT('%', :q, '%'))
    """)
    Page<Patient> search(@Param("q") String q, Pageable pageable);

    @Query("SELECT p FROM Patient p ORDER BY p.fullName ASC")
    List<Patient> findAllOrderedByName(Pageable pageable);
}