package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.Song;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.SongDbo;

@Component
public class SongMapper {

    public Song toDomain(SongDbo dbo) {
        return new Song(dbo.getId(), dbo.getRoom().getId(), dbo.getName(), dbo.getComment(), dbo.getUrl());
    }

    public SongDbo toDbo(Song song, RoomDbo room) {
        return new SongDbo(song.id(), room, song.name(), song.comment(), song.url());
    }
}
