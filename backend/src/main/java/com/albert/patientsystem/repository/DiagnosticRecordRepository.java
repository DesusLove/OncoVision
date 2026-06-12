package com.albert.patientsystem.repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import com.albert.patientsystem.entity.BinaryLabel;
import com.albert.patientsystem.entity.DiagnosticRecord;
import com.albert.patientsystem.entity.SubtypeLabel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface DiagnosticRecordRepository extends JpaRepository<DiagnosticRecord, Long> {
    // accept BinaryLabel so callers can pass the enum directly; wire() converts back to lowercase for the count query
    long countByBinaryLabel(BinaryLabel binaryLabel);

    @Query("SELECT r.subtypeLabel, COUNT(r) FROM DiagnosticRecord r GROUP BY r.subtypeLabel")
    List<Object[]> countBySubtype();
    // the underscore navigates the relationship: record -> patient -> id
    List<DiagnosticRecord> findByPatient_Id(Long patientId);
    List<DiagnosticRecord> findByPatient_PatientId(String patientId);
    @Query("""
    SELECT r FROM DiagnosticRecord r
    WHERE (:label IS NULL OR r.binaryLabel = :label)
      AND (:from  IS NULL OR r.testDate >= :from)
      AND (:to    IS NULL OR r.testDate <= :to)
    """)
    Page<DiagnosticRecord> filter(@Param("label") String label,
                                  @Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  Pageable pageable);
}