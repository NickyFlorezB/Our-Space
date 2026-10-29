package com.nicky.ourspace.application.usecases.song;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Song;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.SongPort;

@Service
public class FindSongByRoomId {

    private final SongPort songPort;
    private final RoomMemberPort roomMemberPort;

    public FindSongByRoomId(SongPort songPort, RoomMemberPort roomMemberPort) {
        this.songPort = songPort;
        this.roomMemberPort = roomMemberPort;
    }

    public List<Song> execute(UUID roomId, UUID userId) {
        this.requireMembership(roomId, userId);

        return this.songPort.findByRoomId(roomId);
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }
}
