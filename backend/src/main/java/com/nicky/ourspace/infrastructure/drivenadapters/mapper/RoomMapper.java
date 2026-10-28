package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;

@Component
public class RoomMapper {

    public Room toDomain(RoomDbo dbo) {
        return new Room(
                dbo.getId(),
                dbo.getName(),
                dbo.getDescription(),
                dbo.getImageUrl(),
                dbo.getOwner().getId());
    }

    public RoomDbo toDbo(Room room, UserDbo owner) {
        RoomDbo dbo = new RoomDbo(
                room.id(),
                room.name(),
                room.description(),
                room.imageUrl());
        dbo.setOwner(owner);
        return dbo;
    }
}
