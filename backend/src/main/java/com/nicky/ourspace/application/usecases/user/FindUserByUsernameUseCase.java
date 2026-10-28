package com.nicky.ourspace.application.usecases.user;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class FindUserByUsernameUseCase {

    private final UserPort userPort;

    public FindUserByUsernameUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public User execute(String username) {
        return userPort.findUserByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
