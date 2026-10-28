package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.Room;

public interface RoomPort {
    Room saveRoom(Room room);

    Optional<Room> findRoomById(UUID roomId);

    List<Room> findRoomsByOwnerId(UUID ownerId);
}
