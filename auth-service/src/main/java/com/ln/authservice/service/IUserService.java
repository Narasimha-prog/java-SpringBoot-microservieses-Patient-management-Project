package com.ln.authservice.service;

import com.ln.authservice.model.User;

import java.util.Optional;

public interface IUserService {
    Optional<User> findByEmail(String email);
}
