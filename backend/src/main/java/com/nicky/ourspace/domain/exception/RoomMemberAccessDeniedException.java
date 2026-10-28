package com.nicky.ourspace.domain.exception;

public class RoomMemberAccessDeniedException extends RuntimeException {

    public RoomMemberAccessDeniedException(String message) {
        super(message);
    }
}
