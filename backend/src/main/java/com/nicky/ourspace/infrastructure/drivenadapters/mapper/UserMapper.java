package com.nicky.ourspace.infrastructure.drivenadapters.mapper;

import org.springframework.stereotype.Component;

import com.nicky.ourspace.domain.model.User;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.UserDbo;

@Component
public class UserMapper {

    public User toDomain(UserDbo dbo) {
        return new User(dbo.getId(), dbo.getUsername(), dbo.getPassword(), dbo.getCellphone(), dbo.getEmail());
    }

    public UserDbo toDbo(User user) {
        return new UserDbo(user.id(), user.username(), user.email(), user.cellphone(), user.password());
    }
}
