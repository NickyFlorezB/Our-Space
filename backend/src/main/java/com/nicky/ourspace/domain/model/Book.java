package com.nicky.ourspace.domain.model;

import java.util.List;
import java.util.UUID;

public record Book(
        UUID id,
        UUID roomId,
        String isbn_13,
        String title,
        String weight,
        String fullTitle,
        Integer latestRevision,
        String createdAt,
        List<String> publishers,
        String imageUrl) {

}
