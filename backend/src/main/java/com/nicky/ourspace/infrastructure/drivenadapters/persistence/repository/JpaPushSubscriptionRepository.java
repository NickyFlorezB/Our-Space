package com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.PushSubscriptionDbo;

public interface JpaPushSubscriptionRepository extends JpaRepository<PushSubscriptionDbo, UUID> {

    Optional<PushSubscriptionDbo> findByEndpoint(String endpoint);

    List<PushSubscriptionDbo> findAllByUserIdIn(List<UUID> userIds);

    void deleteByUserIdAndEndpoint(UUID userId, String endpoint);

    @Transactional
    void deleteByEndpoint(String endpoint);
}
