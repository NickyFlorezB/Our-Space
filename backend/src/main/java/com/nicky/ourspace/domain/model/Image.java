package com.nicky.ourspace.domain.model;

import java.util.UUID;

public record Image(
        UUID id,
        UUID roomId,
        String url,
        String contentType,
        Long size) {
}