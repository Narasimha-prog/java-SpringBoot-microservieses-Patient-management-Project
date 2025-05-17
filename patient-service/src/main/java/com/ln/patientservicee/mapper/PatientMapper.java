package com.ln.patientservicee.mapper;

import com.ln.patientservicee.dto.PatientRequestDTO;
import com.ln.patientservicee.dto.PatientResponseDTO;
import com.ln.patientservicee.model.Patient;

import java.time.LocalDate;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient patient) {
        return PatientResponseDTO.builder()
                .pid(patient.getPid().toString())
                .patientName(patient.getPatientName())
                .patientEmail(patient.getPatientEmail())
                .patientAddress(patient.getPatientAddress())
                .patientDateOfBirth(patient.getPatientDateOfBirth().toString())
                .build();
    }
    public static Patient toModel(PatientRequestDTO patientRequestDTO) {
        return Patient.builder()
                .patientName(patientRequestDTO.getPatientName())
                .patientEmail(patientRequestDTO.getPatientEmail())
                .patientAddress(patientRequestDTO.getPatientAddress())
                .patientRegisterDate(LocalDate.parse(patientRequestDTO.getPatientRegisteredDate()))
                .patientDateOfBirth(LocalDate.parse(patientRequestDTO.getPatientDateOfBirth()))
                .build();

    }
}
