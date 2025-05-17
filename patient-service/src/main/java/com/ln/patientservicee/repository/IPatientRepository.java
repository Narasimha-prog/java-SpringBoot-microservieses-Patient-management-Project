package com.ln.patientservicee.repository;

import com.ln.patientservicee.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IPatientRepository extends JpaRepository<Patient, UUID> {
    boolean existsByPatientEmail(String patientEmail);
    boolean existsByPatientEmailAndPidNot(String patientEmail, UUID pid);
}
