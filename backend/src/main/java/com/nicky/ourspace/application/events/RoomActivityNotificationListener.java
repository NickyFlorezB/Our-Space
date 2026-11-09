package com.nicky.ourspace.application.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.port.RoomActivityNotificationPort;

@Component
public class RoomActivityNotificationListener {

    private final RoomActivityNotificationPort notificationPort;

    public RoomActivityNotificationListener(RoomActivityNotificationPort notificationPort) {
        this.notificationPort = notificationPort;
    }

    @EventListener
    public void onRoomActivityCreated(RoomActivityCreatedEvent event) {
        notificationPort.notifyRoomMembers(
                event.roomId(),
                event.actorUserId(),
                event.title(),
                event.body(),
                event.destination());
    }
}
