package com.nicky.ourspace.infrastructure.entrypoints.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.nicky.ourspace.domain.exception.OwnerMembershipCannotBeRemovedException;
import com.nicky.ourspace.domain.exception.BookNotFoundException;
import com.nicky.ourspace.domain.exception.ImageNotFoundException;
import com.nicky.ourspace.domain.exception.MovieNotFoundException;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.exception.RoomMemberAlreadyExistsException;
import com.nicky.ourspace.domain.exception.RoomMemberNotFoundException;
import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.exception.UserNotFoundException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenExpiredException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenNotFoundException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenRevokedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            RoomNotFoundException.class,
            RoomMemberNotFoundException.class,
            UserNotFoundException.class,
            ImageNotFoundException.class,
            BookNotFoundException.class,
            MovieNotFoundException.class
    })
    public ResponseEntity<String> handleRoomMemberNotFoundException(RuntimeException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(RoomMemberAccessDeniedException.class)
    public ResponseEntity<String> handleRoomMemberAccessDeniedException(RoomMemberAccessDeniedException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler({
            RoomMemberAlreadyExistsException.class,
            OwnerMembershipCannotBeRemovedException.class
    })
    public ResponseEntity<String> handleRoomMemberConflictException(RuntimeException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<String> handleBadRequestException(Exception ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(413).body("Image upload exceeds the maximum allowed size");
    }

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
