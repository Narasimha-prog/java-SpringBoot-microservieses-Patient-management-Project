package com.ln.authservice.service;

import com.ln.authservice.dto.LoginRequestDTO;

import java.util.Optional;

public interface IAuthService {
    Optional<String> authenticate(LoginRequestDTO loginRequestDTO);
    boolean validateToken(String token);
}
