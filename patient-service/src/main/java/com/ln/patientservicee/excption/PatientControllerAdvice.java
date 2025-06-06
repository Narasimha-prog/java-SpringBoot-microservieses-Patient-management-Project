package com.ln.patientservicee.excption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class PatientControllerAdvice {
    private static final Logger log = LoggerFactory.getLogger(PatientControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleInvalidArgumentException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(EmailAlreadyExistedException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistedException(EmailAlreadyExistedException ex) {
        log.warn("Email Address is already existed {}",ex.getMessage());
      Map<String, String> errors = new HashMap<>();
      errors.put("message","Email Address is already existed");
      return ResponseEntity.badRequest().body(errors);
    }
    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(PatientNotFoundException ex) {
        log.warn("Patient not found {}",ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        errors.put("message","Patient not found");
        return ResponseEntity.badRequest().body(errors);
    }
}
