package com.nicky.ourspace.infrastructure.drivenadapters.security.exception;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }

}
