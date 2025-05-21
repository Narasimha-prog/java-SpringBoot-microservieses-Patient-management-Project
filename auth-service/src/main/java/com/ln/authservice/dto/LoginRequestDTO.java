package com.ln.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid Email Address")
    private String email;

    @NotBlank(message = "Password is Required")
    @Size(min = 8,message = "Password must contain 8 characters")
    private String password;
}
