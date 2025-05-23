package com.ln.authservice.service;

import com.ln.authservice.dto.LoginRequestDTO;
import com.ln.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService implements IAuthService{


    final  private IUserService userService;
 final private PasswordEncoder passwordEncoder;
 final  private JwtUtil jwtUtil;

    public AuthService(IUserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        return userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u->passwordEncoder.matches(loginRequestDTO.getPassword(),u.getPassword()))
                .map(u->jwtUtil.generateToken(u.getEmail(),u.getRole()));
    }
    @Override
    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }


}
