package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.model.Song;
import com.nicky.ourspace.domain.port.SongPort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.SongMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaSongRepository;

@Service
public class SongAdapter implements SongPort {

    private final JpaSongRepository jpaSongRepository;
    private final JpaRoomRepository jpaRoomRepository;
    private final SongMapper songMapper;

    public SongAdapter(JpaSongRepository jpaSongRepository, JpaRoomRepository jpaRoomRepository,
            SongMapper songMapper) {
        this.jpaSongRepository = jpaSongRepository;
        this.jpaRoomRepository = jpaRoomRepository;
        this.songMapper = songMapper;
    }

    @Override
    @Transactional
    public Song save(Song song) {
        RoomDbo room = jpaRoomRepository.findById(song.roomId())
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + song.roomId()));

        return songMapper.toDomain(jpaSongRepository.save(songMapper.toDbo(song, room)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> findByRoomId(UUID roomId) {
        return jpaSongRepository.findAllByRoom_IdOrderByIdDesc(roomId).stream()
                .map(songMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Song> findById(UUID roomId, UUID songId) {
        return jpaSongRepository.findByIdAndRoom_Id(songId, roomId)
                .map(songMapper::toDomain);
    }

    @Override
    public void deleteById(UUID roomId, UUID songId) {
        jpaSongRepository.deleteByIdAndRoom_Id(songId, roomId);
    }

}
