package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;

public interface JpaUserRepository extends JpaRepository<UserDbo, UUID> {
    Optional<UserDbo> findByUsername(String username);

    Optional<UserDbo> findByEmail(String email);

}
