package com.nicky.ourspace.infrastructure.entrypoints.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenExpiredException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenNotFoundException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenRevokedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<String> handleRefreshTokenExpiredException(RefreshTokenExpiredException ex) {
        return ResponseEntity.status(401).body("Refresh token has expired");
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<String> handleRefreshTokenNotFoundException(RefreshTokenNotFoundException ex) {
        return ResponseEntity.status(404).body("Refresh token not found");
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<String> handleRefreshTokenRevokedException(RefreshTokenRevokedException ex) {
        return ResponseEntity.status(401).body("Refresh token has been revoked");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(500).body("An unexpected error occurred: " + ex.getMessage());
    }

}
