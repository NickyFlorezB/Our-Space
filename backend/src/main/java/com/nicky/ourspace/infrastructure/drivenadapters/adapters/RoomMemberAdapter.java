package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.exception.RoomMemberAlreadyExistsException;
import com.nicky.ourspace.domain.exception.RoomNotFoundException;
import com.nicky.ourspace.domain.exception.UserNotFoundException;
import com.nicky.ourspace.domain.model.RoomMember;
import com.nicky.ourspace.domain.model.RoomMemberRole;
import com.nicky.ourspace.domain.port.RoomMemberPort;
import com.nicky.ourspace.infrastructure.drivenadapters.mapper.RoomMemberMapper;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberId;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomMemberRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;

@Service
public class RoomMemberAdapter implements RoomMemberPort {

    private final JpaRoomMemberRepository jpaRoomMemberRepository;
    private final JpaRoomRepository jpaRoomRepository;
    private final JpaUserRepository jpaUserRepository;
    private final RoomMemberMapper roomMemberMapper;

    public RoomMemberAdapter(
            JpaRoomMemberRepository jpaRoomMemberRepository,
            JpaRoomRepository jpaRoomRepository,
            JpaUserRepository jpaUserRepository,
            RoomMemberMapper roomMemberMapper) {
        this.jpaRoomMemberRepository = jpaRoomMemberRepository;
        this.jpaRoomRepository = jpaRoomRepository;
        this.jpaUserRepository = jpaUserRepository;
        this.roomMemberMapper = roomMemberMapper;
    }

    @Override
    @Transactional
    public RoomMember saveRoomMember(RoomMember roomMember) {
        RoomDbo room = jpaRoomRepository.findById(roomMember.roomId())
                .orElseThrow(() -> new RoomNotFoundException(
                        "Room not found with id: " + roomMember.roomId()));
        UserDbo user = jpaUserRepository.findById(roomMember.userId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + roomMember.userId()));

        if (roomMember.role() == RoomMemberRole.OWNER
                && !room.getOwner().getId().equals(roomMember.userId())) {
            throw new IllegalArgumentException("Only the room owner can have the OWNER role");
        }

        RoomMemberId id = new RoomMemberId(roomMember.roomId(), roomMember.userId());
        if (jpaRoomMemberRepository.existsById(id)) {
            throw new RoomMemberAlreadyExistsException(
                    "User is already a member of room: " + roomMember.roomId());
        }

        return roomMemberMapper.toDomain(jpaRoomMemberRepository.save(
                roomMemberMapper.toDbo(roomMember, room, user)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomMember> findRoomMember(UUID roomId, UUID userId) {
        return jpaRoomMemberRepository.findById(new RoomMemberId(roomId, userId))
                .map(roomMemberMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomMember> findRoomMembersByRoomId(UUID roomId) {
        return jpaRoomMemberRepository.findAllByRoom_Id(roomId).stream()
                .map(roomMemberMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomMember> findRoomMembersByUserId(UUID userId) {
        return jpaRoomMemberRepository.findAllByUser_Id(userId).stream()
                .map(roomMemberMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public void deleteRoomMember(UUID roomId, UUID userId) {
        jpaRoomMemberRepository.deleteById(new RoomMemberId(roomId, userId));
    }
}
