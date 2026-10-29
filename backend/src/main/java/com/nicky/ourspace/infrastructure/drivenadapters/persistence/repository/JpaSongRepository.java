package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.SongDbo;

public interface JpaSongRepository extends JpaRepository<SongDbo, UUID> {
    List<SongDbo> findAllByRoom_IdOrderByIdDesc(UUID roomId);

    Optional<SongDbo> findByIdAndRoom_Id(UUID songId, UUID roomId);

    void deleteByIdAndRoom_Id(UUID songId, UUID roomId);
}
