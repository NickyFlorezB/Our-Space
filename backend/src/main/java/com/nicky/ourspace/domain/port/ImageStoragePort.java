package com.nicky.ourspace.domain.port;

import java.io.InputStream;
import java.util.UUID;

import com.nicky.ourspace.domain.model.ImageUpload;
import com.nicky.ourspace.domain.model.StoredImage;

public interface ImageStoragePort {
    StoredImage saveRoomImage(UUID roomId, UUID imageId, ImageUpload image);

    InputStream readRoomImage(UUID roomId, UUID imageId);

    void deleteRoomImage(UUID roomId, UUID imageId);

    StoredImage saveMovieCover(UUID movieId, ImageUpload image);

    InputStream readMovieCover(UUID movieId);

    void deleteMovieCover(UUID movieId);

    StoredImage saveBookCover(UUID bookId, ImageUpload image);

    InputStream readBookCover(UUID bookId);

    void deleteBookCover(UUID bookId);
}
