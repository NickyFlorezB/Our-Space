package com.nicky.ourspace.application.usecases.room;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.RoomPort;

@Service
public class FindRoomsByUserIdUseCase {

    private final RoomMemberPort roomMemberPort;
    private final RoomPort roomPort;

    public FindRoomsByUserIdUseCase(RoomMemberPort roomMemberPort, RoomPort roomPort) {
        this.roomMemberPort = roomMemberPort;
        this.roomPort = roomPort;
    }

    @Transactional(readOnly = true)
    public List<Room> execute(UUID userId) {
        return roomMemberPort.findRoomMembersByUserId(userId).stream()
                .map(member -> roomPort.findRoomById(member.roomId())
                        .orElseThrow(() -> new RoomNotFoundException(
                                "Room not found with id: " + member.roomId())))
                .toList();
    }
}
