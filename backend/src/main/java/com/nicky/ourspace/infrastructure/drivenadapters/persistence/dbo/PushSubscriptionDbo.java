package com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "push_subscriptions")
public class PushSubscriptionDbo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, unique = true, length = 2048)
    private String endpoint;

    @Column(name = "public_key", nullable = false, length = 256)
    private String publicKey;

    @Column(name = "auth_secret", nullable = false, length = 128)
    private String authSecret;

    protected PushSubscriptionDbo() {
    }

    public static PushSubscriptionDbo create(
            UUID userId,
            String endpoint,
            String publicKey,
            String authSecret) {
        PushSubscriptionDbo record = new PushSubscriptionDbo();
        record.userId = userId;
        record.endpoint = endpoint;
        record.publicKey = publicKey;
        record.authSecret = authSecret;
        return record;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAuthSecret() {
        return authSecret;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setAuthSecret(String authSecret) {
        this.authSecret = authSecret;
    }
}
