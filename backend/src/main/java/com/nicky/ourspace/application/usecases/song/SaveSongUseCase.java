package com.nicky.ourspace.application.usecases.song;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Song;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.SongPort;

@Service
public class SaveSongUseCase {

    private final SongPort songPort;
    private final RoomMemberPort roomMemberPort;

    public SaveSongUseCase(SongPort songPort, RoomMemberPort roomMemberPort) {
        this.songPort = songPort;
        this.roomMemberPort = roomMemberPort;
    }

    public Song execute(UUID roomId, UUID userId, String name,
            String comment,
            String url) {
        this.requireMembership(roomId, userId);

        return this.songPort.save(new Song(
                UUID.randomUUID(),
                roomId,
                name,
                comment,
                url));
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }

}
