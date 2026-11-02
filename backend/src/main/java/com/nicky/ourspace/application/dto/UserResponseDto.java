package com.nicky.ourspace.application.dto;

import java.util.UUID;

import com.nicky.ourspace.domain.model.User;

public record UserResponseDto(UUID id, String username, String cellphone, String email) {

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user.id(), user.username(), user.cellphone(), user.email());
    }
}
