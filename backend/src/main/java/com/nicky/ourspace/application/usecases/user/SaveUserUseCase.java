package com.nicky.ourspace.application.usecases.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nicky.ourspace.application.usecases.room.EnsureUserRoomsUseCase;
import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.domain.port.UserPort;

@Service
public class SaveUserUseCase {

    private final UserPort userPort;
    private final EnsureUserRoomsUseCase ensureUserRoomsUseCase;

    public SaveUserUseCase(UserPort userPort, EnsureUserRoomsUseCase ensureUserRoomsUseCase) {
        this.userPort = userPort;
        this.ensureUserRoomsUseCase = ensureUserRoomsUseCase;
    }

    @Transactional
    public void execute(String username, String password, String cellphone, String email) {
        var user = new User(
                java.util.UUID.randomUUID(),
                username,
                password,
                cellphone,
                email);
        User savedUser = userPort.saveUser(user);
        ensureUserRoomsUseCase.ensureRoom(savedUser);
    }
}
