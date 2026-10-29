package com.nicky.ourspace.application.usecases.user;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class FindUserByIdUseCase {

    private final UserPort userPort;

    public FindUserByIdUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public User execute(UUID id) {
        return userPort.findUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
