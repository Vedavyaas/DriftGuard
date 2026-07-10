package com.pheonix.ingestor.assets;

public class DuplicateCredentialsException extends RuntimeException {
    public DuplicateCredentialsException(String message) {
        super(message);
    }
}
