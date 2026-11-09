package com.nicky.ourspace.application.events;

import java.util.UUID;

public record RoomActivityCreatedEvent(
        UUID roomId,
        UUID actorUserId,
        String title,
        String body,
        String destination) {
}
