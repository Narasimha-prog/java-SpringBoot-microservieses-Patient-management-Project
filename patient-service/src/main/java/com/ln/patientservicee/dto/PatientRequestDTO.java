package com.ln.patientservicee.dto;

import com.ln.patientservicee.dto.validators.ICreatePatientValidationGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientRequestDTO {
    @NotBlank(message = "Name is required")
    @Size(max = 100,message ="Name Cannot Exceeds 100 characters")
    private String patientName;
    @NotBlank(message = "Email is required")
    @Size(message = "Email Should be Valid")
    private String patientEmail;
    @NotBlank(message = "Address is required")
    private String patientAddress;
    @NotBlank(message = "Date Of Birth is required")
    private String patientDateOfBirth;
    @NotBlank(groups = ICreatePatientValidationGroup.class,message ="Registered Date is required" )
    private String patientRegisteredDate;

}
