package com.nicky.ourspace.domain.exception;

public class PushNotificationsUnavailableException extends RuntimeException {

    public PushNotificationsUnavailableException(String message) {
        super(message);
    }
}
