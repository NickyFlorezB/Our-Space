package com.nicky.ourspace.domain.model;

import java.util.UUID;

public record Room(
                UUID id,
                String name,
                String description,
                String imageUrl,
                UUID ownerId) {

}
