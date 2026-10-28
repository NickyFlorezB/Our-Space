package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RefreshTokenDbo;

public interface JpaRefreshTokenRepository
        extends JpaRepository<RefreshTokenDbo, UUID> {

    Optional<RefreshTokenDbo> findByTokenHash(
            String tokenHash);

    List<RefreshTokenDbo> findByFamilyId(UUID familyId);
}