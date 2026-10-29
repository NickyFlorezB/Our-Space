package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.Song;

public interface SongPort {
    Song save(Song song);

    List<Song> findByRoomId(UUID roomId);

    Optional<Song> findById(UUID roomId, UUID songId);

    void deleteById(UUID roomId, UUID songId);
}
