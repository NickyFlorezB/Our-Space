package com.nicky.ourspace.application.usecases.room;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.port.RoomPort;

@Service
public class SaveRoomUseCase {

    private final RoomPort roomPort;

    public SaveRoomUseCase(RoomPort roomPort) {
        this.roomPort = roomPort;
    }

    public Room execute(String name, String description, String imageUrl, UUID ownerId) {
        return this.roomPort.saveRoom(new Room(UUID.randomUUID(), name, description, imageUrl, ownerId));
    }

}
