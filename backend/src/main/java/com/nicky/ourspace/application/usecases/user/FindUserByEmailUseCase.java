package com.nicky.ourspace.application.usecases.user;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class FindUserByEmailUseCase {

    private final UserPort userPort;

    public FindUserByEmailUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public User execute(String email) {
        return userPort.findUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
