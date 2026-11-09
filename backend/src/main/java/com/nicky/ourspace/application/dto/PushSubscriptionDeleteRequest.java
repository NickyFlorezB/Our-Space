package com.nicky.ourspace.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PushSubscriptionDeleteRequest(
        @NotBlank @Size(max = 2048) String endpoint) {
}
