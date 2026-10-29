package com.nicky.ourspace.application.usecases.room;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.port.RoomPort;

@Service
public class FindRoomsByOwnerIdUseCase {

    private final RoomPort roomPort;

    public FindRoomsByOwnerIdUseCase(RoomPort roomPort) {
        this.roomPort = roomPort;
    }

    public List<Room> execute(UUID ownerId) {
        return this.roomPort.findRoomsByOwnerId(ownerId);
    }
}
