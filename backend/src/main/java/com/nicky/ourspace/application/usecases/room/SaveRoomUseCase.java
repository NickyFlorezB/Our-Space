package com.nicky.ourspace.application.usecases.room;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.RoomPort;

@Service
public class SaveRoomUseCase {

    private final RoomPort roomPort;
    private final RoomMemberPort roomMemberPort;

    public SaveRoomUseCase(RoomPort roomPort, RoomMemberPort roomMemberPort) {
        this.roomPort = roomPort;
        this.roomMemberPort = roomMemberPort;
    }

    @Transactional
    public Room execute(String name, String description, String imageUrl, UUID ownerId) {
        Room room = roomPort.saveRoom(new Room(UUID.randomUUID(), name, description, imageUrl, ownerId));
        roomMemberPort.saveRoomMember(new RoomMember(room.id(), ownerId, RoomMemberRole.OWNER));
        return room;
    }

}
