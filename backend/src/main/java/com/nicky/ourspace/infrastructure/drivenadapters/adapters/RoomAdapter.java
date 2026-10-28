package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.model.Room;
import com.nicky.ourspace.domain.port.RoomPort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.RoomMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;

@Service
public class RoomAdapter implements RoomPort {

    private final JpaRoomRepository jpaRoomRepository;
    private final JpaUserRepository jpaUserRepository;
    private final RoomMapper roomMapper;

    public RoomAdapter(
            JpaRoomRepository jpaRoomRepository,
            JpaUserRepository jpaUserRepository,
            RoomMapper roomMapper) {
        this.jpaRoomRepository = jpaRoomRepository;
        this.jpaUserRepository = jpaUserRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    @Transactional
    public Room saveRoom(Room room) {
        if (room.ownerId() == null) {
            throw new IllegalArgumentException("Room owner id is required");
        }
        UserDbo owner = jpaUserRepository.findById(room.ownerId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + room.ownerId()));
        return roomMapper.toDomain(jpaRoomRepository.save(roomMapper.toDbo(room, owner)));
    }

    @Override
    public Optional<Room> findRoomById(UUID roomId) {
        return jpaRoomRepository.findById(roomId).map(roomMapper::toDomain);
    }

    @Override
    public List<Room> findRoomsByOwnerId(UUID ownerId) {
        return jpaRoomRepository.findByOwner_Id(ownerId).stream()
                .map(roomMapper::toDomain)
                .toList();
    }
}
