package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberId;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;

@Component
public class RoomMemberMapper {

    public RoomMember toDomain(RoomMemberDbo dbo) {
        return new RoomMember(dbo.getId().getRoomId(), dbo.getId().getUserId(), dbo.getRole());
    }

    public RoomMemberDbo toDbo(RoomMember roomMember, RoomDbo room, UserDbo user) {
        return new RoomMemberDbo(
                new RoomMemberId(roomMember.roomId(), roomMember.userId()),
                room,
                user,
                roomMember.role());
    }
}
