package com.nicky.ourspace.application.usecases.room;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.RoomPort;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class EnsureUserRoomsUseCase {

    private final RoomMemberPort roomMemberPort;
    private final RoomPort roomPort;
    private final UserPort userPort;

    public EnsureUserRoomsUseCase(
            RoomMemberPort roomMemberPort,
            RoomPort roomPort,
            UserPort userPort) {
        this.roomMemberPort = roomMemberPort;
        this.roomPort = roomPort;
        this.userPort = userPort;
    }

    @Transactional
    public void ensureRoom(User user) {
        if (!roomMemberPort.findRoomMembersByUserId(user.id()).isEmpty()) {
            return;
        }

        String roomName = roomName(user);
        Room room = roomPort.saveRoom(new Room(UUID.randomUUID(), roomName, null, null, user.id()));
        roomMemberPort.saveRoomMember(new RoomMember(room.id(), user.id(), RoomMemberRole.OWNER));
    }

    @Transactional
    public void ensureRoomsForAllUsers() {
        roomMemberPort.synchronizeOwnerMemberships();
        userPort.findAllUsers().forEach(this::ensureRoom);
    }

    private String roomName(User user) {
        String username = user.username() == null || user.username().isBlank()
                ? "usuario"
                : user.username();
        if (username.length() > 100) {
            username = username.substring(0, 100);
        }
        return "Habitación de " + username + " (" + user.id().toString().substring(0, 8) + ")";
    }
}
