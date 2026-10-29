package com.nicky.ourspace.application.usecases.song;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.SongPort;

@Service
public class DeleteSongByIdUseCase {

    private final SongPort songPort;
    private final RoomMemberPort roomMemberPort;

    public DeleteSongByIdUseCase(SongPort songPort, RoomMemberPort roomMemberPort) {
        this.songPort = songPort;
        this.roomMemberPort = roomMemberPort;
    }

    public void execute(UUID roomId, UUID songId, UUID userId) {
        this.requireMembership(roomId, userId);
        this.songPort.deleteById(roomId, songId);
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }
}
