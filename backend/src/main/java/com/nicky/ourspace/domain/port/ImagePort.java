package com.nicky.ourspace.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nicky.ourspace.domain.model.Image;

public interface ImagePort {
    Image save(Image image);

    List<Image> findByRoomId(UUID roomId);

    Optional<Image> findById(UUID roomId, UUID imageId);

    void deleteById(UUID roomId, UUID imageId);
}
