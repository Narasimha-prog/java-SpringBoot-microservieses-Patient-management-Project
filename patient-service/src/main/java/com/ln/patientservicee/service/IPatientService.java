package com.ln.patientservicee.service;

import com.ln.patientservicee.dto.PatientRequestDTO;
import com.ln.patientservicee.dto.PatientResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IPatientService {
     List<PatientResponseDTO> getPatients();
     PatientResponseDTO createPatient(PatientRequestDTO requestDTO);
     PatientResponseDTO updatePatient(UUID patientId, PatientRequestDTO requestDTO);
     void deletePatient(UUID patientId);
     PatientResponseDTO getPatientById(UUID patientId);
}
