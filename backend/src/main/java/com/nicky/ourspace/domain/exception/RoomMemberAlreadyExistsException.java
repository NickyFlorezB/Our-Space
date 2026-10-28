package com.nicky.ourspace.domain.exception;

public class RoomMemberAlreadyExistsException extends RuntimeException {

    public RoomMemberAlreadyExistsException(String message) {
        super(message);
    }
}
