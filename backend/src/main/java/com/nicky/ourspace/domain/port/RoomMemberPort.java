package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.RoomMember;

public interface RoomMemberPort {

    RoomMember saveRoomMember(RoomMember roomMember);

    Optional<RoomMember> findRoomMember(UUID roomId, UUID userId);

    List<RoomMember> findRoomMembersByRoomId(UUID roomId);

    List<RoomMember> findRoomMembersByUserId(UUID userId);

    void deleteRoomMember(UUID roomId, UUID userId);
}
