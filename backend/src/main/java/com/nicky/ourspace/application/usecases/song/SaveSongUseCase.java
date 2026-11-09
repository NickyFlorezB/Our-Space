package com.nicky.ourspace.application.usecases.song;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.events.RoomActivityCreatedEvent;
import com.nicky.ourspace.domain.exception.RoomMemberAccessDeniedException;
import com.nicky.ourspace.domain.model.Song;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.domain.port.SongPort;

@Service
public class SaveSongUseCase {

    private final SongPort songPort;
    private final RoomMemberPort roomMemberPort;
    private final ApplicationEventPublisher eventPublisher;

    public SaveSongUseCase(
            SongPort songPort,
            RoomMemberPort roomMemberPort,
            ApplicationEventPublisher eventPublisher) {
        this.songPort = songPort;
        this.roomMemberPort = roomMemberPort;
        this.eventPublisher = eventPublisher;
    }

    public Song execute(UUID roomId, UUID userId, String name,
            String comment,
            String url) {
        this.requireMembership(roomId, userId);

        Song savedSong = this.songPort.save(new Song(
                UUID.randomUUID(),
                roomId,
                name,
                comment,
                url));
        eventPublisher.publishEvent(new RoomActivityCreatedEvent(
                roomId, userId, "Nueva canción", savedSong.name(), "/songs?roomId=" + roomId));
        return savedSong;
    }

    private void requireMembership(UUID roomId, UUID userId) {
        if (roomMemberPort.findRoomMember(roomId, userId).isEmpty()) {
            throw new RoomMemberAccessDeniedException("Only room members can upload images");
        }
    }

}
