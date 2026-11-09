package com.nicky.ourspace.domain.port;

import java.util.UUID;

public interface RoomActivityNotificationPort {

    void notifyRoomMembers(
            UUID roomId,
            UUID actorUserId,
            String title,
            String body,
            String destination);
}
