package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomMemberId;

public interface JpaRoomMemberRepository extends JpaRepository<RoomMemberDbo, RoomMemberId> {

    List<RoomMemberDbo> findAllByRoom_Id(UUID roomId);

    List<RoomMemberDbo> findAllByUser_Id(UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO room_members (room_id, user_id, role)
            SELECT id, owner_id, 'OWNER'
            FROM rooms
            ON CONFLICT (room_id, user_id) DO UPDATE SET role = 'OWNER'
            """, nativeQuery = true)
    int synchronizeOwnerMemberships();
}
