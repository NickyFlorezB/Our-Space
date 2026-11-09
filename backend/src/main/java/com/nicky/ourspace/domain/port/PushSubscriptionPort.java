package com.nicky.ourspace.domain.port;

import java.util.UUID;

import com.nicky.ourspace.domain.model.PushSubscription;

public interface PushSubscriptionPort {

    boolean isEnabled();

    String publicKey();

    void save(PushSubscription subscription);

    void delete(UUID userId, String endpoint);
}
