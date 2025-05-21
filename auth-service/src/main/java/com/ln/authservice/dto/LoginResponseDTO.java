package com.ln.authservice.dto;

import lombok.Getter;

@Getter
public class LoginResponseDTO {
   final private String token;
    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
