package com.nicky.ourspace.infrastructure.drivenadapters.adapters;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.domain.model.PushSubscription;
import com.nicky.ourspace.domain.port.PushSubscriptionPort;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.PushSubscriptionDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaPushSubscriptionRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaUserRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.storage.PushProperties;

@Service
public class PushSubscriptionAdapter implements PushSubscriptionPort {

    private final PushProperties pushProperties;
    private final JpaPushSubscriptionRepository pushSubscriptionRepository;
    private final JpaUserRepository userRepository;

    public PushSubscriptionAdapter(
            PushProperties pushProperties,
            JpaPushSubscriptionRepository pushSubscriptionRepository,
            JpaUserRepository userRepository) {
        this.pushProperties = pushProperties;
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean isEnabled() {
        return pushProperties.isEnabled();
    }

    @Override
    public String publicKey() {
        return pushProperties.getPublicKey();
    }

    @Override
    @Transactional
    public void save(PushSubscription subscription) {
        if (!userRepository.existsById(subscription.userId())) {
            throw new IllegalArgumentException("User does not exist");
        }

        PushSubscriptionDbo record = pushSubscriptionRepository.findByEndpoint(subscription.endpoint())
                .orElse(null);
        if (record == null) {
            record = PushSubscriptionDbo.create(
                    subscription.userId(),
                    subscription.endpoint(),
                    subscription.publicKey(),
                    subscription.authSecret());
        } else {
            if (!record.getUserId().equals(subscription.userId())) {
                throw new IllegalArgumentException("Push subscription already belongs to another user");
            }
            record.setPublicKey(subscription.publicKey());
            record.setAuthSecret(subscription.authSecret());
        }
        pushSubscriptionRepository.save(record);
    }

    @Override
    @Transactional
    public void delete(UUID userId, String endpoint) {
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
    }
}
