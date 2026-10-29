package com.nicky.ourspace.domain.model;

import java.util.UUID;

public record Song(
        UUID id,
        UUID roomId,
        String name,
        String comment,
        String url) {

}
