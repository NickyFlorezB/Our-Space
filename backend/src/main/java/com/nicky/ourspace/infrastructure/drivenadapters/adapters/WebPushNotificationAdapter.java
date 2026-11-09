package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.nicky.ourspace.domain.port.RoomActivityNotificationPort;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.PushSubscriptionDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaPushSubscriptionRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomMemberRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.storage.PushProperties;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;

@Service
public class WebPushNotificationAdapter implements RoomActivityNotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebPushNotificationAdapter.class);

    private final PushProperties pushProperties;
    private final JpaRoomMemberRepository roomMemberRepository;
    private final JpaPushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;
    private final PushService pushService;

    public WebPushNotificationAdapter(
            PushProperties pushProperties,
            JpaRoomMemberRepository roomMemberRepository,
            JpaPushSubscriptionRepository pushSubscriptionRepository,
            ObjectMapper objectMapper) {
        this.pushProperties = pushProperties;
        this.roomMemberRepository = roomMemberRepository;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.objectMapper = objectMapper;
        if (pushProperties.isPartiallyConfigured() && !pushProperties.isEnabled()) {
            throw new IllegalStateException(
                    "Configure all APP_PUSH_PUBLIC_KEY, APP_PUSH_PRIVATE_KEY, and APP_PUSH_SUBJECT values together");
        }
        if (pushProperties.isEnabled()) {
            Security.addProvider(new BouncyCastleProvider());
            try {
                this.pushService = new PushService(
                        pushProperties.getPublicKey(),
                        pushProperties.getPrivateKey(),
                        pushProperties.getSubject());
            } catch (GeneralSecurityException exception) {
                throw new IllegalStateException("Could not initialize Web Push using the configured VAPID keys",
                        exception);
            }
        } else {
            this.pushService = null;
        }
    }

    @Override
    @Async
    public void notifyRoomMembers(
            UUID roomId,
            UUID actorUserId,
            String title,
            String body,
            String destination) {
        if (!pushProperties.isEnabled()) {
            return;
        }

        List<UUID> recipientIds = roomMemberRepository.findAllByRoom_Id(roomId).stream()
                .map(membership -> membership.getId().getUserId())
                .filter(userId -> !userId.equals(actorUserId))
                .distinct()
                .toList();
        if (recipientIds.isEmpty()) {
            return;
        }

        List<PushSubscriptionDbo> subscriptions = pushSubscriptionRepository.findAllByUserIdIn(recipientIds);
        for (PushSubscriptionDbo subscription : subscriptions) {
            send(subscription, title, body, destination, "room-" + roomId);
        }
    }

    private void send(
            PushSubscriptionDbo subscription,
            String title,
            String body,
            String destination,
            String tag) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(new NotificationPayload(title, body, destination, tag));
        } catch (JacksonException exception) {
            LOGGER.error("Could not serialize a room activity push notification", exception);
            return;
        }

        try {
            Notification notification = new Notification(
                    subscription.getEndpoint(),
                    subscription.getPublicKey(),
                    subscription.getAuthSecret(),
                    payload);
            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 404 || statusCode == 410) {
                pushSubscriptionRepository.deleteByEndpoint(subscription.getEndpoint());
            } else if (statusCode < 200 || statusCode >= 300) {
                LOGGER.warn("Push provider rejected a notification with HTTP {}", statusCode);
            }
        } catch (GeneralSecurityException | IOException | JoseException | ExecutionException exception) {
            LOGGER.warn("Could not deliver a room activity push notification", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LOGGER.warn("Push notification delivery was interrupted", exception);
        }
    }

    private record NotificationPayload(String title, String body, String url, String tag) {
    }
}
