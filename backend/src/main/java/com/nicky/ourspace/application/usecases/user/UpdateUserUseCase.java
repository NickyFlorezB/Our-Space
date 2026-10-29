package com.nicky.ourspace.application.usecases.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class UpdateUserUseCase {

    private final UserPort userPort;

    public UpdateUserUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public User execute(User user, UUID id) {
        return userPort.updateUser(user, id);
    }
}
