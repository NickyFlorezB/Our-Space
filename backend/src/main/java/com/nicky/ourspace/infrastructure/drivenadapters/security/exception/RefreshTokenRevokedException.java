package com.nicky.ourspace.infrastructure.drivenadapters.security.exception;

public class RefreshTokenRevokedException extends RuntimeException {

    public RefreshTokenRevokedException(String message) {
        super(message);
    }

}
