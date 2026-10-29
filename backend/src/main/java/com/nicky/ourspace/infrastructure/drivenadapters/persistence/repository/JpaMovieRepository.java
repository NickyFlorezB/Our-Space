package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.MovieDbo;

public interface JpaMovieRepository extends JpaRepository<MovieDbo, UUID> {
    List<MovieDbo> findAllByRoom_IdOrderByIdDesc(UUID roomId);

    Optional<MovieDbo> findByIdAndRoom_Id(UUID movieId, UUID roomId);

    void deleteByIdAndRoom_Id(UUID movieId, UUID roomId);
}
