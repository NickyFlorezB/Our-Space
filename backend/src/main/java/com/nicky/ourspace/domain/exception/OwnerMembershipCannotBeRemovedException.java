package com.nicky.ourspace.domain.exception;

public class OwnerMembershipCannotBeRemovedException extends RuntimeException {

    public OwnerMembershipCannotBeRemovedException(String message) {
        super(message);
    }
}
