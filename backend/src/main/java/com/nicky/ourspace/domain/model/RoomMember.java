package com.nicky.ourspace.domain.model;

import java.util.UUID;

public record RoomMember(UUID roomId, UUID userId, RoomMemberRole role) {
}
