package com.nicky.ourspace.application.usecases.roommember;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class FindRoomMembersByUserIdUseCase {

    private final RoomMemberPort roomMemberPort;

    public FindRoomMembersByUserIdUseCase(RoomMemberPort roomMemberPort) {
        this.roomMemberPort = roomMemberPort;
    }

    @Transactional(readOnly = true)
    public List<RoomMember> execute(UUID userId, UUID actorUserId) {
        if (!userId.equals(actorUserId)) {
            throw new RoomMemberAccessDeniedException("Users can only view their own room memberships");
        }
        return roomMemberPort.findRoomMembersByUserId(userId);
    }
}
