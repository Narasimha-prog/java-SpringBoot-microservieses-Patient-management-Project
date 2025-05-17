package com.ln.patientservicee.controller;

import com.ln.patientservicee.dto.PatientRequestDTO;
import com.ln.patientservicee.dto.PatientResponseDTO;
import com.ln.patientservicee.dto.validators.ICreatePatientValidationGroup;
import com.ln.patientservicee.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patient",description = "API for managing patients")
public class PatientController {
    private final PatientService patientService;


    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "get Patients")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        return ResponseEntity.ok().body(patientService.getPatients());
    }

    @GetMapping("/{id}")
    @Operation(summary = "get Patient")
    public ResponseEntity<PatientResponseDTO> getAllPatient(@PathVariable UUID id) {
        return ResponseEntity.ok().body(patientService.getPatientById(id));
    }


    @PostMapping
    @Operation(summary = "create a new Patient")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, ICreatePatientValidationGroup.class}) @RequestBody PatientRequestDTO PatientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.createPatient(PatientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "update a Patient")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id,@Validated({Default.class}) @RequestBody PatientRequestDTO PatientRequestDTO) {
        PatientResponseDTO patientResponseDTO = patientService.updatePatient(id, PatientRequestDTO);
        return ResponseEntity.ok().body(patientResponseDTO);
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a Patient")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
