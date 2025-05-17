package com.ln.patientservicee.excption;

public class EmailAlreadyExistedException extends RuntimeException {
    public EmailAlreadyExistedException(String message) {
        super(message);
    }
}
