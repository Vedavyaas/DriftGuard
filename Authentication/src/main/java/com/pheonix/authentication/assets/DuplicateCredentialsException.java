package com.pheonix.authentication.assets;

public class DuplicateCredentialsException extends RuntimeException {
    public DuplicateCredentialsException(String message) {
        super(message);
    }
}
