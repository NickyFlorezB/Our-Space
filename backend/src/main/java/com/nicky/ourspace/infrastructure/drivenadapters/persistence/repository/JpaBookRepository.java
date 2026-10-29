package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.BookDbo;

public interface JpaBookRepository extends JpaRepository<BookDbo, UUID> {
    List<BookDbo> findAllByRoom_IdOrderByIdDesc(UUID roomId);

    Optional<BookDbo> findByIdAndRoom_Id(UUID bookId, UUID roomId);

    void deleteByIdAndRoom_Id(UUID bookId, UUID roomId);
}
