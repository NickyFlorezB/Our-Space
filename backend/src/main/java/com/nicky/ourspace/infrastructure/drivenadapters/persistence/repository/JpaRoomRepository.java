package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RoomDbo;

public interface JpaRoomRepository extends JpaRepository<RoomDbo, UUID> {

    List<RoomDbo> findByOwner_Id(UUID ownerId);
}
