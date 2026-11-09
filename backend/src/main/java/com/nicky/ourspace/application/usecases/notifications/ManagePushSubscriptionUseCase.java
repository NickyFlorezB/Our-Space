package com.nicky.ourspace.application.usecases.notifications;

import java.net.URI;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.dto.PushSettingsResponse;
import com.nicky.ourspace.domain.exception.PushNotificationsUnavailableException;
import com.nicky.ourspace.domain.model.PushSubscription;
import com.nicky.ourspace.domain.port.PushSubscriptionPort;

@Service
public class ManagePushSubscriptionUseCase {

    private static final Set<String> ALLOWED_PUSH_HOST_SUFFIXES = Set.of(
            "fcm.googleapis.com",
            "notify.windows.com",
            "push.apple.com",
            "push.services.mozilla.com");

    private final PushSubscriptionPort pushSubscriptionPort;

    public ManagePushSubscriptionUseCase(PushSubscriptionPort pushSubscriptionPort) {
        this.pushSubscriptionPort = pushSubscriptionPort;
    }

    public PushSettingsResponse settings() {
        boolean enabled = pushSubscriptionPort.isEnabled();
        return new PushSettingsResponse(enabled, enabled ? pushSubscriptionPort.publicKey() : null);
    }

    public void subscribe(UUID userId, String endpoint, String publicKey, String authSecret) {
        if (!pushSubscriptionPort.isEnabled()) {
            throw new PushNotificationsUnavailableException("Push notifications are not configured on this server");
        }
        validateEndpoint(endpoint);
        validateKey(publicKey, 65, "Push public key");
        validateKey(authSecret, 16, "Push authentication secret");
        pushSubscriptionPort.save(new PushSubscription(userId, endpoint, publicKey, authSecret));
    }

    public void unsubscribe(UUID userId, String endpoint) {
        validateEndpoint(endpoint);
        pushSubscriptionPort.delete(userId, endpoint);
    }

    private void validateEndpoint(String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalArgumentException("Push endpoint is not a supported push service");
        }
        try {
            URI uri = URI.create(endpoint);
            String host = uri.getHost();
            boolean allowedHost = host != null && ALLOWED_PUSH_HOST_SUFFIXES.stream()
                    .anyMatch(suffix -> host.equals(suffix) || host.endsWith("." + suffix));
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getUserInfo() != null
                    || (uri.getPort() != -1 && uri.getPort() != 443)
                    || !allowedHost) {
                throw new IllegalArgumentException("Push endpoint is not a supported push service");
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Push endpoint is not a supported push service", exception);
        }
    }

    private void validateKey(String encodedKey, int expectedBytes, String label) {
        if (encodedKey == null || encodedKey.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }

        byte[] decoded;
        try {
            decoded = Base64.getUrlDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(label + " is not valid base64url data", exception);
        }
        if (decoded.length != expectedBytes) {
            throw new IllegalArgumentException(label + " has an invalid length");
        }
    }
}
