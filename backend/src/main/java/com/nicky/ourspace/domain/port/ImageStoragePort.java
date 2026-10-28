package com.nicky.ourspace.domain.port;

import java.io.InputStream;
import java.util.UUID;

import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.model.StoredImage;

public interface ImageStoragePort {
    StoredImage saveRoomImage(UUID roomId, UUID imageId, ImageUpload image);

    InputStream readRoomImage(UUID roomId, UUID imageId);

    void deleteRoomImage(UUID roomId, UUID imageId);
}
