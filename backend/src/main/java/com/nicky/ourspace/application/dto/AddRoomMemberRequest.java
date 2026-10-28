package com.nicky.ourspace.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AddRoomMemberRequest(@NotNull UUID userId) {
}
