package com.nicky.ourspace.application.usecases.roommember;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class FindRoomMembersByRoomIdUseCase {

    private final RoomMemberPort roomMemberPort;

    public FindRoomMembersByRoomIdUseCase(RoomMemberPort roomMemberPort) {
        this.roomMemberPort = roomMemberPort;
    }

    @Transactional(readOnly = true)
    public List<RoomMember> execute(UUID roomId, UUID actorUserId) {
        if (roomMemberPort.findRoomMember(roomId, actorUserId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can view room members");
        }
        return roomMemberPort.findRoomMembersByRoomId(roomId);
    }
}
