package com.nicky.ourspace.application.usecases.notifications;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.nicky.ourspace.domain.exception.PushNotificationsUnavailableException;
import com.nicky.ourspace.domain.model.PushSubscription;
import com.nicky.ourspace.domain.port.PushSubscriptionPort;

class ManagePushSubscriptionUseCaseTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String PUBLIC_KEY = key(65);
    private static final String AUTH_SECRET = key(16);

    @Test
    void acceptsValidSubscriptionFromSupportedPushProvider() {
        TestPushSubscriptionPort port = new TestPushSubscriptionPort(true);
        ManagePushSubscriptionUseCase useCase = new ManagePushSubscriptionUseCase(port);

        useCase.subscribe(USER_ID, "https://fcm.googleapis.com/push/token", PUBLIC_KEY, AUTH_SECRET);

        assertEquals(USER_ID, port.savedSubscription.userId());
        assertEquals(PUBLIC_KEY, port.savedSubscription.publicKey());
    }

    @Test
    void rejectsEndpointsThatCouldTargetUntrustedHosts() {
        ManagePushSubscriptionUseCase useCase = new ManagePushSubscriptionUseCase(
                new TestPushSubscriptionPort(true));

        assertThrows(IllegalArgumentException.class, () -> useCase.subscribe(
                USER_ID, "https://fcm.googleapis.com.attacker.example/token", PUBLIC_KEY, AUTH_SECRET));
        assertThrows(IllegalArgumentException.class, () -> useCase.subscribe(
                USER_ID, "http://fcm.googleapis.com/token", PUBLIC_KEY, AUTH_SECRET));
        assertThrows(IllegalArgumentException.class, () -> useCase.subscribe(
                USER_ID, "https://fcm.googleapis.com/push/token", key(64), AUTH_SECRET));
    }

    @Test
    void reportsDisabledSettingsAndRejectsSubscriptionWhenKeysAreNotConfigured() {
        TestPushSubscriptionPort port = new TestPushSubscriptionPort(false);
        ManagePushSubscriptionUseCase useCase = new ManagePushSubscriptionUseCase(port);

        assertFalse(useCase.settings().enabled());
        assertThrows(PushNotificationsUnavailableException.class, () -> useCase.subscribe(
                USER_ID, "https://fcm.googleapis.com/push/token", PUBLIC_KEY, AUTH_SECRET));
        assertTrue(port.savedSubscription == null);
    }

    @Test
    void allowsDeletingAnExistingSubscriptionWhilePushIsDisabled() {
        TestPushSubscriptionPort port = new TestPushSubscriptionPort(false);
        ManagePushSubscriptionUseCase useCase = new ManagePushSubscriptionUseCase(port);

        useCase.unsubscribe(USER_ID, "https://fcm.googleapis.com/push/token");

        assertEquals(USER_ID, port.deletedUserId);
    }

    private static String key(int length) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[length]);
    }

    private static final class TestPushSubscriptionPort implements PushSubscriptionPort {

        private final boolean enabled;
        private PushSubscription savedSubscription;
        private UUID deletedUserId;

        private TestPushSubscriptionPort(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public String publicKey() {
            return enabled ? PUBLIC_KEY : null;
        }

        @Override
        public void save(PushSubscription subscription) {
            savedSubscription = subscription;
        }

        @Override
        public void delete(UUID userId, String endpoint) {
            savedSubscription = null;
            deletedUserId = userId;
        }
    }
}
