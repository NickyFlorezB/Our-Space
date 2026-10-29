package com.nicky.ourspace.application.usecases.user;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class SaveUserUseCase {

    private final UserPort userPort;

    public SaveUserUseCase(UserPort userPort) {
        this.userPort = userPort;
    }

    public void execute(String username, String password, String cellphone, String email) {
        var user = new User(
                java.util.UUID.randomUUID(),
                username,
                password,
                cellphone,
                email);
        userPort.saveUser(user);
    }
}
