package com.nicky.ourspace.domain.model;

import java.util.UUID;

public record PushSubscription(
        UUID userId,
        String endpoint,
        String publicKey,
        String authSecret) {
}
