package com.nicky.ourspace.application.usecases.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.port.UserPort;

@Service
public class DeleteUserUseCase {

    private final UserPort userPort;

    public DeleteUserUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public void execute(UUID id) {
        userPort.deleteUser(id);
    }
}
