package com.nicky.ourspace.application.configuration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.nicky.ourspace.application.usecases.room.EnsureUserRoomsUseCase;

@Component
public class EnsureUserRoomsOnStartup implements ApplicationRunner {

    private final EnsureUserRoomsUseCase ensureUserRoomsUseCase;

    public EnsureUserRoomsOnStartup(EnsureUserRoomsUseCase ensureUserRoomsUseCase) {
        this.ensureUserRoomsUseCase = ensureUserRoomsUseCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureUserRoomsUseCase.ensureRoomsForAllUsers();
    }
}
