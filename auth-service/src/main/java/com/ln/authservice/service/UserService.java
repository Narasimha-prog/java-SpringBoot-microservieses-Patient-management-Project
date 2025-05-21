package com.ln.authservice.service;

import com.ln.authservice.model.User;
import com.ln.authservice.repository.IUserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService implements IUserService {
    final private IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
           return userRepository.findByEmail(email);
    }
}
