package com.ln.patientservicee.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name ="ID")
    private UUID pid;

    @NotNull
    @Column(name ="NAME")
    private String patientName;

     @NotNull
     @Email
     @Column(name ="EMAIL", unique = true)
    private String patientEmail;

     @NotNull
     @Column(name ="ADDRESS")
    private String patientAddress;

     @NotNull
     @Column(name ="DATE_OF_BIRTH")
    private LocalDate patientDateOfBirth;

    @NotNull
    @Column(name ="REGISTERED_DATE")
    private LocalDate patientRegisterDate;


}
