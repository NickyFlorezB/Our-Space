package com.nicky.ourspace.application.usecases.roommember;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.OwnerMembershipCannotBeRemovedException;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.exception.RoomMemberNotFoundException;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.domain.port.RoomMemberPort;

@Service
public class RemoveRoomMemberUseCase {

    private final RoomMemberPort roomMemberPort;

    public RemoveRoomMemberUseCase(RoomMemberPort roomMemberPort) {
        this.roomMemberPort = roomMemberPort;
    }

    @Transactional
    public void execute(UUID roomId, UUID actorUserId, UUID memberUserId) {
        RoomMember member = roomMemberPort.findRoomMember(roomId, memberUserId)
                .orElseThrow(() -> new RoomMemberNotFoundException(
                        "Membership not found for user " + memberUserId + " in room " + roomId));

        if (member.role() == RoomMemberRole.OWNER) {
            throw new OwnerMembershipCannotBeRemovedException("The room owner membership cannot be removed");
        }

        if (!actorUserId.equals(memberUserId)) {
            RoomMember actor = roomMemberPort.findRoomMember(roomId, actorUserId)
                    .orElseThrow(() -> new RoomMemberAccessDeniedException(
                            "Only a room owner can remove another user"));
            if (actor.role() != RoomMemberRole.OWNER) {
                throw new RoomMemberAccessDeniedException(
                        "Only a room owner can remove another user");
            }
        }

        roomMemberPort.deleteRoomMember(roomId, memberUserId);
    }
}
