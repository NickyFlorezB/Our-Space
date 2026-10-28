package com.nicky.ourspace.infrastructure.drivenadapters.persistence;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRoomMemberRepository;

@Component
public class RoomOwnerMembershipInitializer implements ApplicationRunner {

    private final JpaRoomMemberRepository jpaRoomMemberRepository;

    public RoomOwnerMembershipInitializer(JpaRoomMemberRepository jpaRoomMemberRepository) {
        this.jpaRoomMemberRepository = jpaRoomMemberRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jpaRoomMemberRepository.synchronizeOwnerMemberships();
    }
}
