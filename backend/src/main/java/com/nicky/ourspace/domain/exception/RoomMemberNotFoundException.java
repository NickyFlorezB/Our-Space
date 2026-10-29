package com.nicky.ourspace.domain.exception;

public class RoomMemberNotFoundException extends RuntimeException {

    public RoomMemberNotFoundException(String message) {
        super(message);
    }
}
