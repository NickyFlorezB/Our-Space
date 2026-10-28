package com.nicky.ourspace.infrastructure.drivenadapters.security.exception;

public class RefreshTokenExpiredException extends RuntimeException {

    public RefreshTokenExpiredException(String message) {
        super(message);
    }

}
