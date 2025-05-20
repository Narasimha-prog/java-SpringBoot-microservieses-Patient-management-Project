package com.ln.patientservicee.service;

import com.ln.patientservicee.dto.PatientRequestDTO;
import com.ln.patientservicee.dto.PatientResponseDTO;
import com.ln.patientservicee.excption.EmailAlreadyExistedException;
import com.ln.patientservicee.excption.PatientNotFoundException;
import com.ln.patientservicee.grpc.BillingServiceGrpcClient;
import com.ln.patientservicee.kafka.KafkaProducer;
import com.ln.patientservicee.mapper.PatientMapper;
import com.ln.patientservicee.model.Patient;
import com.ln.patientservicee.repository.IPatientRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService implements IPatientService {


    private final IPatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(IPatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient, KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    @Override
    public PatientResponseDTO createPatient(PatientRequestDTO requestDTO) {
        if (patientRepository.existsByPatientEmail(requestDTO.getPatientEmail())) {
            throw new EmailAlreadyExistedException("A Patient with this Email is already existed " + requestDTO.getPatientEmail());
        }
        Patient newPatient = patientRepository.save(PatientMapper.toModel(requestDTO));
        billingServiceGrpcClient.createBillingAccount(newPatient.getPid().toString(), newPatient.getPatientName(), newPatient.getPatientEmail());

        kafkaProducer.sendEvent(newPatient);
        return PatientMapper.toDTO(newPatient);
    }

    @Override
    public PatientResponseDTO updatePatient(UUID patientId, PatientRequestDTO requestDTO) {
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Patient is not found with this ID: " + patientId));
        if (patientRepository.existsByPatientEmailAndPidNot(requestDTO.getPatientEmail(), patientId)) {
            throw new EmailAlreadyExistedException("A Patient with this Email is already existed " + requestDTO.getPatientEmail());
        }
        //once a patient is registered with date cannot be changed so ,No update of Registered date
        patient.setPatientName(requestDTO.getPatientName());
        patient.setPatientEmail(requestDTO.getPatientEmail());
        patient.setPatientAddress(requestDTO.getPatientAddress());
        patient.setPatientDateOfBirth(LocalDate.parse(requestDTO.getPatientDateOfBirth()));
        Patient updatedPatient = patientRepository.save(patient);
        return PatientMapper.toDTO(updatedPatient);
    }

    @Override
    public void deletePatient(UUID patientId) {
        patientRepository.deleteById(patientId);
    }

    @Override
    public PatientResponseDTO getPatientById(UUID patientId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new PatientNotFoundException("Patient is not found with this ID: " + patientId));
        return PatientMapper.toDTO(patient);
    }
}


