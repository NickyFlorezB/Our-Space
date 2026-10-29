package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.ImageDbo;

public interface JpaImageRepository extends JpaRepository<ImageDbo, UUID> {

    List<ImageDbo> findAllByRoom_IdOrderByIdDesc(UUID roomId);

    Optional<ImageDbo> findByIdAndRoom_Id(UUID imageId, UUID roomId);

    void deleteByIdAndRoom_Id(UUID imageId, UUID roomId);
}
