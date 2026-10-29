package com.nicky.ourspace.application.usecases.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class FindAllUsersUseCase {

    private final UserPort userPort;

    public FindAllUsersUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public List<User> execute() {
        return userPort.findAllUsers();
    }

}
