package com.nicky.ourspace.application.usecases.roommember;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class AddRoomMemberUseCase {

    private final RoomMemberPort roomMemberPort;

    public AddRoomMemberUseCase(RoomMemberPort roomMemberPort) {
        this.roomMemberPort = roomMemberPort;
    }

    @Transactional
    public RoomMember execute(UUID roomId, UUID actorUserId, UUID memberUserId) {
        if (!actorUserId.equals(memberUserId)) {
            RoomMember actor = roomMemberPort.findRoomMember(roomId, actorUserId)
                    .orElseThrow(() -> new RoomMemberAccessDeniedException(
                            "Only a room owner can add another user"));
            if (actor.role() != RoomMemberRole.OWNER) {
                throw new RoomMemberAccessDeniedException(
                        "Only a room owner can add another user");
            }
        }

        return roomMemberPort.saveRoomMember(
                new RoomMember(roomId, memberUserId, RoomMemberRole.MEMBER));
    }
}
