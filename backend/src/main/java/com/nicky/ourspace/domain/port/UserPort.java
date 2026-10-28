package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.User;

public interface UserPort {
    User saveUser(User user);

    Optional<User> findUserById(UUID id);

    List<User> findAllUsers();

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

    User updateUser(User user, UUID id);

    void deleteUser(UUID id);
}
